package org.ijntema.eric.encoders;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.ijntema.eric.bitstream.BigEndianBitOutputStream;
import org.ijntema.eric.frames.SpaceInvaderAnimation;
import org.ijntema.eric.streamers.UdpStreamer;

@Slf4j
public class H261Encoder {

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
    private static final int[][]                              ZIGZAG_ORDER                 =
            {
                    {0, 1, 5, 6, 14, 15, 27, 28},
                    {2, 4, 7, 13, 16, 26, 29, 42},
                    {3, 8, 12, 17, 25, 30, 41, 43},
                    {9, 11, 18, 24, 31, 40, 44, 53},
                    {10, 19, 23, 32, 39, 45, 52, 54},
                    {20, 22, 33, 38, 46, 51, 55, 60},
                    {21, 34, 37, 47, 50, 56, 59, 61},
                    {35, 36, 48, 49, 57, 58, 62, 63}
            };

    private final UdpStreamer              udpStreamer;
    private final SpaceInvaderAnimation    frameGenerator = new SpaceInvaderAnimation();
    private final BigEndianBitOutputStream stream         = new BigEndianBitOutputStream(new ByteArrayOutputStream());

    private boolean firstPicture = true;

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

    public H261Encoder () throws SocketException {

        // Start the UDP streamer
        udpStreamer = new UdpStreamer();
        Thread t = new Thread(udpStreamer);
        t.setDaemon(true);
        t.start();
    }

    public static void main (String[] args) throws IOException, InterruptedException {

        log.info("Starting H261Encoder");

        new H261Encoder().encode();
    }

    public void encode () throws IOException, InterruptedException {

        int temporalReferenceCount = 0; // 0 - 31, increment for every Picture

        try {

            while (true) {

                if (temporalReferenceCount == 32) {

                    temporalReferenceCount = 0;
                }

                int[][][] yCbCrMatrix = rgbToYCbCr(this.frameGenerator.generateFrame());

                for (int i = 0; i < GOB_ROWS; i++) {

                    for (int j = 0; j < GOB_COLUMNS; j++) {

                        for (int k = 0; k < MACROBLOCK_ROWS; k++) {

                            for (int l = 0; l < MACROBLOCK_COLUMNS; l++) {

                                int gobN = (k == 0 && l == 0) ? 0 : (i * GOB_COLUMNS) + j + 1; // 2 - 12
                                int mbap = (k == 0 && l == 0) ? 0 : (k * MACROBLOCK_COLUMNS) + l; // Not + 1 because it's the number of the previous MB, 1 - 32
                                int quant = (k == 0 && l == 0) ? 0 : 1;
                                this.writeH261Header(gobN, mbap, quant); // Every packet has a H261 Header

                                if (i == 0 && j == 0 && k == 0 && l == 0) { // First packet for a Picture has a Picture Header

                                    this.writePictureHeader(temporalReferenceCount);
                                }

                                if (k == 0 && l == 0) { // First Macroblock packet has a GOB Header

                                    this.writeGobHeader(i, j);
                                }

                                this.writeMacroblockHeader(k, l);

                                Pair<Integer, Integer> marcroblockStartRowAndColumn =
                                        this.getMarcroblockStartRowAndColumn(i, j, k, l);
                                int[][][] blocks = toBlocks(
                                        marcroblockStartRowAndColumn,
                                        yCbCrMatrix
                                );

                                this.writeMacroblock(blocks);

                                byte[] h261Packet = this.byteAlignStream();
                                this.udpStreamer.getPacketQueue().add(h261Packet);
                                // Reset the stream
                                ((ByteArrayOutputStream) this.stream.getOutputStream()).reset();
                            }
                        }

                    }
                }

                // ~30 fps (spec: frame rate (i.e. 30000/1001 or approx. 29.97 Hz))
                // Creating the packet also takes time, so the frame rate is not exactly 30 fps
                // H.261 supports 176x144 and 352x288 frames at target frame rates of 7.5 to 30 fps
                Thread.sleep(1000/31);

                temporalReferenceCount++;

                firstPicture = false;
            }
        } finally {

            this.stream.getOutputStream().close();
            this.stream.close();
        }
    }

    private int[][][] rgbToYCbCr (BufferedImage image) {

        int[][][] yCbCr = new int[PICTURE_WIDTH][PICTURE_HEIGHT][3];
        for (int x = 0; x < PICTURE_WIDTH; x++) {

            for (int y = 0; y < PICTURE_HEIGHT; y++) {

                int rgb = image.getRGB(x, y);
                Color color = new Color(rgb);
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                yCbCr[x][y][0] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                yCbCr[x][y][1] = (int) (-0.1687 * r - 0.3313 * g + 0.5 * b + 128);
                yCbCr[x][y][2] = (int) (0.5 * r - 0.4187 * g - 0.0813 * b + 128);
            }
        }

        return yCbCr;
    }

    private void writeH261Header (final int gobN, final int mbap, final int quant) throws IOException {

        if (this.firstPicture) {

            log.info("\n");
            log.info("H261 Header");
        }

        // 32 bits
        this.stream.write(0, 3); // SBIT (3 bits) 0 not used currently
        this.stream.write(0, 3); // EBIT (3 bits) Amount of bits at the end of the packet that the decoder should ignore, is updated after the packet is created
        this.stream.write(1, 1); // INTRA (1 bit)
        this.stream.write(0, 1); // MV flag (1 bit)
        this.stream.write(gobN, 4); // GOBN (4 bits)
        this.stream.write(mbap, 5); // MBAP (5 bits)
        this.stream.write(quant, 5); // QUANT (5 bits)
        this.stream.write(0, 5); // HMVD (5 bits) Set to 0 because the MV flag is 0
        this.stream.write(0, 5); // VMVD (5 bits) Set to 0 because the MV flag is 0
    }

    private void writePictureHeader (int temporalReference) throws IOException {

        if (this.firstPicture) {

            log.info("Picture Header");
        }

        // 32 bits
        this.stream.write(0b0000_0000_0000_0001_0000, 20); // PSC (20 bits)
        this.stream.write(temporalReference, 5); // TR (5 bits)
        //        Bit 1 Split screen indicator, “0” off, “1” on;
        //        Bit 2 Document camera indicator, “0” off, “1” on;
        //        Bit 3 Freeze picture release, “0” off, “1” on;
        //        Bit 4 Source format, “0” QCIF, “1” CIF;
        //        Bit 5 Optional still image mode HI_RES defined in Annex D; “0” on, “1” off;
        //        Bit 6 Spare.
        this.stream.write(0b0000_0111, 6); // PTYPE (6 bits), bit 4, 5 and 6 are 1
        this.stream.write(0, 1); // PEI (1 bit)
    }

    private void writeGobHeader (final int row, final int column) throws IOException {

        // 26 bits
        this.stream.write(1, 16); // GOB start code (16 bits)
        int groupNumber = (row * GOB_COLUMNS) + column + 1; // 1 - 12
        this.stream.write(groupNumber, 4); // GN (4 bits)
        this.stream.write(1, 5); // GQUANT (5 bits)
        this.stream.write(0, 1); // GEI (1 bit)

        if (this.firstPicture) {

            log.info("GOB Header, nr: {}", groupNumber);
        }
    }

    private void writeMacroblockHeader (final int row, final int column) throws IOException {

//        this.stream.write(1, 16); // MACROBLOCK start code (16 bits), not clear when this is needed
        int macroblockAddress = (row * MACROBLOCK_COLUMNS) + column + 1;
//        Pair<Integer, Integer> vlc = VLC_TABLE_MACROBLOCK_ADDRESS.get(macroblockAddress);
//        this.stream.write(vlc.getKey(), vlc.getValue()); // MACROBLOCK ADDRESS (variable length)
        this.stream.write(0b1, 1);
        this.stream.write(0b0001, 4); // MTYPE (4 bit)

        if (this.firstPicture) {

            log.info("Macroblock Header, nr: {}", macroblockAddress);
        }
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

    private void writeMacroblock (int[][][] blocks) throws IOException {

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

        int[][] quantized = new int[BLOCK_SIZE][BLOCK_SIZE];
        int scale = 31;

        int DC_step_size = 8;
        int AC_step_size = 2 * scale;

        for (int i = 0; i < BLOCK_SIZE; i++) {

            for (int j = 0; j < BLOCK_SIZE; j++) {

                if (i == 0 && j == 0) {

                    // Apply DC step size for the top-left element.
                    quantized[i][j] = (int) Math.round(matrix[i][j] / DC_step_size);
                } else {

                    // Apply AC step size for the other elements.
                    quantized[i][j] = (int) Math.round(matrix[i][j] / AC_step_size);
                }
            }
        }

        return quantized;
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

            this.stream.write(0b0000_01, 6); // ESCAPE (6 bits)
            this.stream.write(sequence[i], 6); // RUN (6 bits)
            this.stream.write(sequence[i + 1], 8); // LEVEL (8 bits)
        }
    }

    private void writeBlockEnd () throws IOException {

        this.stream.write(2, 2);
    }

    private byte[] byteAlignStream () throws IOException {

        int bufferBitCount = this.stream.getBufferBitCount();
        if (bufferBitCount > 0) {

            int numBits = 8 - bufferBitCount;
            this.stream.write(0, numBits); // Byte align

            if (this.stream.getBufferBitCount() != 0) {

                throw new RuntimeException("Stream is not byte aligned!");
            }

            byte[] byteArray = ((ByteArrayOutputStream) this.stream.getOutputStream()).toByteArray();
            int headerFirstByte = byteArray[0];
            headerFirstByte = headerFirstByte | (numBits << 2); // Set with numBits value
            byteArray[0] = (byte) headerFirstByte;

            return byteArray;
        } else {

            return ((ByteArrayOutputStream) this.stream.getOutputStream()).toByteArray();
        }
    }
}
