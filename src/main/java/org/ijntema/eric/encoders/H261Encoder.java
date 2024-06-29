package org.ijntema.eric.encoders;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.ijntema.eric.bitstream.BigEndianBitOutputStream;
import org.ijntema.eric.frames.SpaceInvaderAnimation;
import org.ijntema.eric.packets.RTPpacket;
import org.ijntema.eric.streamers.UdpStreamer;

@Slf4j
public class H261Encoder {

    private final SpaceInvaderAnimation frameGenerator = new SpaceInvaderAnimation();

    private static final int                                  FRAMES_PER_SECOND            = 20;
    public static final  int                                  PICTURE_WIDTH                = 352;
    public static final  int                                  PICTURE_HEIGHT               = 288;
    private static final int                                  GOB_ROWS                     = 6;
    private static final int                                  GOB_COLUMNS                  = 2;
    private static final int                                  MACROBLOCK_ROWS              = 3;
    private static final int                                  MACROBLOCK_COLUMNS           = 11;
    private static final int                                  Y_BLOCKS_AMOUNT              = 4;
    private static final int                                  CB_BLOCKS_AMOUNT             = 1;
    private static final int                                  CR_BLOCKS_AMOUNT             = 1;
    private static final double                               STEP_SIZE                    = 8.0;
    // YCbCr 4:2:0
    private static final int                                  TOTAL_BLOCKS                 = Y_BLOCKS_AMOUNT + CB_BLOCKS_AMOUNT + CR_BLOCKS_AMOUNT;
    private static final int                                  BLOCK_SIZE                   = 8;
    private static final Map<Integer, Pair<Integer, Integer>> VLC_TABLE_MACROBLOCK_ADDRESS = new HashMap<>();
    private static final int[][]                              ZIGZAG_ORDER                 = {
            {0, 1, 5, 6, 14, 15, 27, 28},
            {2, 4, 7, 13, 16, 26, 29, 42},
            {3, 8, 12, 17, 25, 30, 41, 43},
            {9, 11, 18, 24, 31, 40, 44, 53},
            {10, 19, 23, 32, 39, 45, 52, 54},
            {20, 22, 33, 38, 46, 51, 55, 60},
            {21, 34, 37, 47, 50, 56, 59, 61},
            {35, 36, 48, 49, 57, 58, 62, 63}
    };

    private boolean                        firstStream = true;
    private boolean                        lastGob     = false;
    private List<BigEndianBitOutputStream> streamList  = new ArrayList<>();

    static {

        VLC_TABLE_MACROBLOCK_ADDRESS.put(1, new Pair<>(0b1, 1));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(2, new Pair<>(0b011, 3));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(3, new Pair<>(0b010, 3));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(4, new Pair<>(0b011, 4));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(5, new Pair<>(0b010, 4));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(6, new Pair<>(0b00011, 5));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(7, new Pair<>(0b00010, 5));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(8, new Pair<>(0b0000_111, 7));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(9, new Pair<>(0b0000_110, 7));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(10, new Pair<>(0b0000_1011, 8));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(11, new Pair<>(0b0000_1010, 8));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(12, new Pair<>(0b0000_1001, 8));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(13, new Pair<>(0b0000_1000, 8));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(14, new Pair<>(0b0000_0111, 8));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(15, new Pair<>(0b0000_0110, 8));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(16, new Pair<>(0b0000_0101_11, 10));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(17, new Pair<>(0b0000_0101_10, 10));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(18, new Pair<>(0b0000_0101_01, 10));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(19, new Pair<>(0b0000_0101_00, 10));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(20, new Pair<>(0b0000_0100_11, 10));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(21, new Pair<>(0b0000_0100_10, 10));

        VLC_TABLE_MACROBLOCK_ADDRESS.put(22, new Pair<>(0b0000_0100_011, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(23, new Pair<>(0b0000_0100_010, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(24, new Pair<>(0b0000_0100_001, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(25, new Pair<>(0b0000_0100_000, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(26, new Pair<>(0b0000_0011_111, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(27, new Pair<>(0b0000_0011_110, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(28, new Pair<>(0b0000_0011_101, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(29, new Pair<>(0b0000_0011_100, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(30, new Pair<>(0b0000_0011_011, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(31, new Pair<>(0b0000_0011_010, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(32, new Pair<>(0b0000_0011_001, 11));
        VLC_TABLE_MACROBLOCK_ADDRESS.put(33, new Pair<>(0b0000_0011_000, 11));
    }

    public H261Encoder () {

        streamList.add(new BigEndianBitOutputStream(new ByteArrayOutputStream()));
    }

    public static void main (String[] args) throws IOException {

        log.info("Starting H261Encoder");

        new H261Encoder().encode();
    }

    public void encode () throws IOException {

        int temporalReferenceCount = 0;

        UdpStreamer udpStreamer = new UdpStreamer();
        Thread t = new Thread(udpStreamer);
        t.setDaemon(true);
        t.start();
        int sequence = 0;

        while (true) {

            try {

                this.lastGob = false;

                writeH261Header();

                if (temporalReferenceCount == 32) {

                    temporalReferenceCount = 0;
                }

                writePicture(temporalReferenceCount);

                // Send the packet
                byte[] h261PacketBytes = getH261Packet();
                byte[] rtpPacketBytes;
                rtpPacketBytes = createRtpPacketBytes(sequence, h261PacketBytes);
                int byteCount = 0;
                int partSize = 1024;
                byte[] rtpPacketPart = new byte[partSize];
                for (byte b : rtpPacketBytes) {

                   rtpPacketPart[byteCount] = b;

                   byteCount++;

                   if (byteCount == partSize) {

                       udpStreamer.getPacketQueue().put(rtpPacketPart);
                       byteCount = 0;
                   }
                }

                log.info("Added RTP packet to queue");

                Thread.sleep(1000 / FRAMES_PER_SECOND);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }

            if (sequence == Integer.MAX_VALUE) {

                sequence = 0;
            } else {

                sequence++;
            }

            temporalReferenceCount++;
        }
    }

    private void writeH261Header () throws IOException {

        this.getOutputStream().write(0, 3); // SBIT (3 bits)
        this.getOutputStream().write(0, 3); // EBIT (3 bits)
        this.getOutputStream().write(1, 1); // INTRA (1 bit)
        this.getOutputStream().write(0, 1); // MV flag (1 bit)
        this.getOutputStream().write(0, 4); // GOBN (4 bits)
        this.getOutputStream().write(0, 5); // MBAP (5 bits)
        this.getOutputStream().write(0, 5); // QUANT (5 bits)
        this.getOutputStream().write(0, 5); // HMVD (5 bits)
        this.getOutputStream().write(0, 5); // VMVD (5 bits)
    }

    private void writePicture (int temporalReference) throws IOException {

        this.writePictureHeader(temporalReference);

        int[][][] yCbCrMatrix = rgbToYCbCr(this.frameGenerator.generateFrame());

        for (int i = 0; i < GOB_ROWS; i++) {

            for (int j = 0; j < GOB_COLUMNS; j++) {

                this.writeGobHeader(i, j);
                for (int k = 0; k < MACROBLOCK_ROWS; k++) {

                    for (int l = 0; l < MACROBLOCK_COLUMNS; l++) {

                        Pair<Integer, Integer> marcroblockStartRowAndColumn = getMarcroblockStartRowAndColumn(i, j, k, l);
                        int[][][] blocks = toBlocks(
                                marcroblockStartRowAndColumn,
                                yCbCrMatrix
                        );
                        writeMacroblock(blocks, k, l);
                    }
                }
            }
        }

    }

    private void writePictureHeader (int temporalReference) throws IOException {

        this.getOutputStream().write(0b0000_0000_0000_0001_0000, 20); // PSC (20 bits)
        this.getOutputStream().write(temporalReference, 5); // TR (5 bits)
        this.getOutputStream().write(0b0010_1000, 6); // PTYPE (6 bits), 4th bit is CIF and 6th bit is Spare (Spares should be 1)
        this.getOutputStream().write(0, 1); // PEI (1 bit)
    }

    private int[][][] rgbToYCbCr (BufferedImage image) {

        int[][][] yCbCr = new int[PICTURE_WIDTH][PICTURE_HEIGHT][3];
        for (int x = 0; x < PICTURE_WIDTH; x++) {

            for (int y = 0; y < PICTURE_HEIGHT; y++) {

                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                yCbCr[x][y][0] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                yCbCr[x][y][1] = (int) (-0.1687 * r - 0.3313 * g + 0.5 * b + 128);
                yCbCr[x][y][2] = (int) (0.5 * r - 0.4187 * g - 0.0813 * b + 128);
            }
        }

        return yCbCr;
    }

    private void writeGobHeader (final int row, final int column) throws IOException {

        this.getOutputStream().write(1, 16); // GOB start code (16 bits)
        int groupNumber = (row * GOB_COLUMNS) + column + 1;
        this.getOutputStream().write(groupNumber, 4); // GN (4 bits)
        this.getOutputStream().write(1, 5); // GQUANT (5 bits)
        this.getOutputStream().write(0, 1); // GEI (1 bit)

        if (groupNumber == 12) {

            this.lastGob = true;
        }
    }

    private void writeMacroblockHeader (final int row, final int column) throws IOException {

        int macroblockAddress = (row * MACROBLOCK_COLUMNS) + column + 1;
        Pair<Integer, Integer> vlc = VLC_TABLE_MACROBLOCK_ADDRESS.get(macroblockAddress);
        newStream(); // Switch the stream so later MBA stuffing bits can be added if needed
        this.getOutputStream().write(vlc.getKey(), vlc.getValue()); // MACROBLOCK ADDRESS (variable length)
        this.getOutputStream().write(0b0001, 4); // MTYPE (4 bit)
    }

    private Pair<Integer, Integer> getMarcroblockStartRowAndColumn (
            final int gobRow,
            final int gobColumn,
            final int macroblockRow,
            final int macroblockColumn
    ) {

        return new Pair<>(
                ((gobColumn * MACROBLOCK_COLUMNS) + macroblockColumn) * 16,
                ((gobRow * MACROBLOCK_ROWS) + macroblockRow) * 16
        );
    }

    private int[][][] toBlocks (
            final Pair<Integer, Integer> pixelRowAndColumn,
            final int[][][] yCbCr
    ) {

        int[][][] blocks = new int[TOTAL_BLOCKS][BLOCK_SIZE][BLOCK_SIZE];

        for (int i = pixelRowAndColumn.getKey(); i < pixelRowAndColumn.getKey() + 16; i++) {

            for (int j = pixelRowAndColumn.getValue(); j < pixelRowAndColumn.getValue() + 16; j++) {

                int x = i % 16;
                int y = j % 16;
                // Y
                if (x < 8 && y < 8) { // Bottom left

                    blocks[2][x][y] = yCbCr[i][j][0];
                } else if (x < 8) { // Top left

                    blocks[0][x][y - 8] = yCbCr[i][j][0];
                } else if (y < 8) { // Bottom right

                    blocks[3][x - 8][y] = yCbCr[i][j][0];
                } else { // Top right

                    blocks[1][x - 8][y - 8] = yCbCr[i][j][0];
                }

                // CbCr
                if (x % 2 == 0 && y % 2 == 0) {

                    blocks[4][x / 2][y / 2] = yCbCr[i][j][1];
                    blocks[5][x / 2][y / 2] = yCbCr[i][j][2];
                }
            }
        }

        return blocks;
    }

    private void writeMacroblock (int[][][] blocks, int row, int column) throws IOException {

        this.writeMacroblockHeader(row, column);

        for (int i = 0; i < TOTAL_BLOCKS; i++) {

            int[][] block = blocks[i];
            double[][] dctBlock = this.dct(block);
            int[][] quantizedBlock = this.quantize(dctBlock);
            int[] zigzagOrderSequence = this.zigzag(quantizedBlock);
            int[] runLengthedSequence = this.runLength(zigzagOrderSequence);
            this.writeHuffman(runLengthedSequence);
            this.writeBlockEnd();
        }
    }

    public double[][] dct (int[][] matrix) {

        int n = BLOCK_SIZE, m = BLOCK_SIZE;
        double pi = 3.142857;

        int i, j, k, l;

        // dct will store the discrete cosine transform
        double[][] dctBlock = new double[m][n];

        double ci, cj, dct, sum;

        for (i = 0; i < m; i++) {
            for (j = 0; j < n; j++) {
                // ci and cj depends on frequency as well as
                // number of row and columns of specified matrix
                if (i == 0) {
                    ci = 1 / Math.sqrt(m);
                } else {
                    ci = Math.sqrt(2) / Math.sqrt(m);
                }

                if (j == 0) {
                    cj = 1 / Math.sqrt(n);
                } else {
                    cj = Math.sqrt(2) / Math.sqrt(n);
                }

                // sum will temporarily store the sum of
                // cosine signals
                sum = 0;
                for (k = 0; k < m; k++) {
                    for (l = 0; l < n; l++) {
                        dct = matrix[k][l] *
                                Math.cos((2 * k + 1) * i * pi / (2 * m)) *
                                Math.cos((2 * l + 1) * j * pi / (2 * n));
                        sum = sum + dct;
                    }
                }
                dctBlock[i][j] = ci * cj * sum;
            }
        }

        return dctBlock;
    }

    public int[][] quantize (double[][] matrix) {

        int[][] quantizedBlocks = new int[BLOCK_SIZE][BLOCK_SIZE];
        for (int i = 0; i < BLOCK_SIZE; i++) {

            for (int j = 0; j < BLOCK_SIZE; j++) {

                quantizedBlocks[i][j] =
                        (int) Math.round(
                                (matrix[i][j] + STEP_SIZE / 2) / STEP_SIZE // Simple linear quantization
                        );
            }
        }

        return quantizedBlocks;
    }

    public int[] zigzag (int[][] matrix) {

        int[] zigzag = new int[64];
        for (int i = 0; i < 8; i++) {

            for (int j = 0; j < 8; j++) {

                zigzag[ZIGZAG_ORDER[i][j]] = matrix[i][j];
            }
        }

        return zigzag;
    }

    private int[] runLength (final int[] sequence) {

        List<Integer> encoded = new ArrayList<>();
        int run = 0;

        for (int value : sequence) {

            if (value == 0) {

                run++;
            } else {

                encoded.add(run);
                encoded.add(value);
                run = 0; // Reset the run count after a non-zero value
            }
        }

        int[] result = new int[encoded.size()];
        for (int i = 0; i < encoded.size(); i++) {
            result[i] = encoded.get(i);
        }

        return result;
    }

    private void writeHuffman (final int[] sequence) throws IOException {

        for (int i = 0; i < sequence.length; i += 2) {

            this.getOutputStream().write(0b0000_01, 6); // ESCAPE (6 bits)
            this.getOutputStream().write(sequence[i], 6); // RUN (6 bits)
            this.getOutputStream().write(sequence[i + 1], 8); // LEVEL (8 bits)
        }
    }

    private void writeBlockEnd () throws IOException {


        this.getOutputStream().write(2, 2);
    }

    private BigEndianBitOutputStream getOutputStream () {

        return this.streamList.get(this.streamList.size() - 1);
    }

    private void newStream () {

        this.streamList.add(new BigEndianBitOutputStream(new ByteArrayOutputStream()));
    }

    private byte[] getH261Packet () throws IOException {

        int restCount = 0;
        for (BigEndianBitOutputStream stream : this.streamList) {

            restCount += stream.getBufferBitCount();
        }

        int stuffingAmount = 0;
        while (restCount % 8 != 0) {

            restCount += 11;
            stuffingAmount++;
        }

        int streamIndex = 0;
        while (streamIndex < stuffingAmount) {

            streamList.get(streamIndex).write(0b0000_0001_111, 11); // Write stuffing bits

            streamIndex++;
        }

        // Add all the streams to the first stream
        BigEndianBitOutputStream firstStream = this.streamList.get(0);
        for (int i = 1; i < this.streamList.size(); i++) {

            BigEndianBitOutputStream stream = this.streamList.get(i);
            if (stream.getBufferBitCount() > 0) {

                firstStream.write(stream.getBuffer(), stream.getBufferBitCount());
            }
            byte[] byteArray = ((ByteArrayOutputStream) this.streamList.get(i).getOutputStream()).toByteArray();
            for (byte b : byteArray) {
                firstStream.write(b, 8);
            }
        }

        if (firstStream.getBufferBitCount() != 0) {

            throw new IllegalStateException("The packet should be byte aligned!");
        }

        this.streamList.clear();
        streamList.add(new BigEndianBitOutputStream(new ByteArrayOutputStream()));

        return ((ByteArrayOutputStream) firstStream.getOutputStream()).toByteArray();
    }

    private static byte[] createRtpPacketBytes (final int sequence, final byte[] h261PacketBytes) {

        byte[] rtpPacketBytes;
        RTPpacket rtpPacket =
                new RTPpacket(
                        31, // H.261 payload type
                        sequence,
                        sequence * (1000 / FRAMES_PER_SECOND),
                        h261PacketBytes,
                        h261PacketBytes.length
                );
        int rtpPacketLength = rtpPacket.getlength();
        rtpPacketBytes = new byte[rtpPacketLength];
        rtpPacket.getpacket(rtpPacketBytes);

        return rtpPacketBytes;
    }
}
