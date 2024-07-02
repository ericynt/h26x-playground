package org.ijntema.eric.encoders;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.ijntema.eric.bitstream.BigEndianBitOutputStream;
import org.ijntema.eric.frames.SpaceInvaderAnimation;
import org.ijntema.eric.streamers.UdpStreamer;

import static org.ijntema.eric.constants.H261Constants.BLOCK_SIZE;
import static org.ijntema.eric.constants.H261Constants.GOB_COLUMNS;
import static org.ijntema.eric.constants.H261Constants.GOB_ROWS;
import static org.ijntema.eric.constants.H261Constants.MACROBLOCK_COLUMNS;
import static org.ijntema.eric.constants.H261Constants.MACROBLOCK_ROWS;
import static org.ijntema.eric.constants.H261Constants.PICTURE_HEIGHT;
import static org.ijntema.eric.constants.H261Constants.PICTURE_WIDTH;
import static org.ijntema.eric.constants.H261Constants.QUANT;
import static org.ijntema.eric.constants.H261Constants.TOTAL_BLOCKS;
import static org.ijntema.eric.constants.H261Constants.ZIGZAG_ORDER;

@Slf4j
public class H261Encoder {

    private final UdpStreamer              udpStreamer;
    private final SpaceInvaderAnimation    frameGenerator = new SpaceInvaderAnimation();
    private final BigEndianBitOutputStream stream         = new BigEndianBitOutputStream(new ByteArrayOutputStream());

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

        // Only created I-frames at the moment

        // 0 - 31, increment for every Picture
        int temporalReferenceCount = 0;

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

                                createH261PacketBytes(i, j, k, l, temporalReferenceCount, yCbCrMatrix);

                                byte[] h261Packet = ((ByteArrayOutputStream) this.stream.getOutputStream()).toByteArray();
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
                Thread.sleep(1000 / 31);

                temporalReferenceCount++;
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

    private void createH261PacketBytes (
            final int gobRow,
            final int gobColumn,
            final int macroblockRow,
            final int macroblockColumn,
            final int temporalReferenceCount,
            final int[][][] yCbCrMatrix
    ) throws IOException {

        // Add a Picture Header if it's the first packet of the Picture
        if (gobRow == 0 && gobColumn == 0 && macroblockRow == 0 && macroblockColumn == 0) {

            this.writePictureHeader(temporalReferenceCount);
        }

        // Add a GOB Header if it's a packet for the first macroblok in a GOB
        if (macroblockRow == 0 && macroblockColumn == 0) {

            this.writeGobHeader(gobRow, gobColumn);
        }

        Pair<Integer, Integer> marcroblockStartRowAndColumn =
                this.getMarcroblockStartRowAndColumn(gobRow, gobColumn, macroblockRow, macroblockColumn);
        int[][][] blocks = toBlocks(
                marcroblockStartRowAndColumn,
                yCbCrMatrix
        );

        // Every macroblok is sent in a separate packet
        this.writeMacroblockHeader(macroblockRow, macroblockColumn);
        this.writeMacroblock(blocks);
    }

    private void writePictureHeader (int temporalReference) throws IOException {

        // 32 bits
        // PSC (20 bits)
        this.stream.write(0b0000_0000_0000_0001_0000, 20);
        this.stream.write(temporalReference, 5); // TR (5 bits)
        // Bit 1 Split screen indicator, “0” off, “1” on;
        // Bit 2 Document camera indicator, “0” off, “1” on;
        // Bit 3 Freeze picture release, “0” off, “1” on;
        // Bit 4 Source format, “0” QCIF, “1” CIF;
        // Bit 5 Optional still image mode HI_RES defined in Annex D; “0” on, “1” off;
        // Bit 6 Spare.
        // PTYPE (6 bits), bit 4, 5 and 6 are 1
        this.stream.write(0b0000_0111, 6);
        this.stream.write(0, 1); // PEI (1 bit)
    }

    private void writeGobHeader (final int row, final int column) throws IOException {

        // 26 bits
        // GOB start code (16 bits)
        this.stream.write(1, 16);
        // GN 1 - 12 (4 bits)
        int groupNumber = (row * GOB_COLUMNS) + column + 1;
        this.stream.write(groupNumber, 4);
        // GQUANT (5 bits)
        this.stream.write(QUANT, 5);
        // GEI (1 bit)
        this.stream.write(0, 1);
    }

    private void writeMacroblockHeader (final int row, final int column) throws IOException {

        // this.stream.write(1, 16); // MACROBLOCK start code (16 bits), not clear when this is needed
        // int macroblockAddress = (row * MACROBLOCK_COLUMNS) + column + 1;
        // Pair<Integer, Integer> vlc = VLC_TABLE_MACROBLOCK_ADDRESS.get(macroblockAddress);
        //  this.stream.write(vlc.getKey(), vlc.getValue()); // MACROBLOCK ADDRESS (variable length)
        // This sort of works as long as all the packets arrive in order
        this.stream.write(0b1, 1);
        // MTYPE (4 bit)
        this.stream.write(0b0001, 4);
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

        // YCbCr 4:4:4 to 4:2:0
        // and transform 16x16x3 to 6x8x8 (4 Y, 1 Cb, 1 Cr)

        int[][][] blocks = new int[TOTAL_BLOCKS][BLOCK_SIZE][BLOCK_SIZE];
        int[][] cbAccumulators = new int[BLOCK_SIZE][BLOCK_SIZE];
        int[][] crAccumulators = new int[BLOCK_SIZE][BLOCK_SIZE];
        int[][] countAccumulators = new int[BLOCK_SIZE][BLOCK_SIZE];

        for (int i = pixelRowAndColumn.getKey(); i < pixelRowAndColumn.getKey() + 16; i++) {

            for (int j = pixelRowAndColumn.getValue(); j < pixelRowAndColumn.getValue() + 16; j++) {

                int x = i % 16;
                int y = j % 16;

                // Y component
                if (x < 8 && y < 8) {

                    // Bottom left
                    blocks[2][x][y] = yCbCr[i][j][0];
                } else if (x < 8) {

                    // Top left
                    blocks[0][x][y - 8] = yCbCr[i][j][0];
                } else if (y < 8) {

                    // Bottom right
                    blocks[3][x - 8][y] = yCbCr[i][j][0];
                } else {

                    // Top right
                    blocks[1][x - 8][y - 8] = yCbCr[i][j][0];
                }

                // Cb and Cr components
                int cbCrX = x / 2;
                int cbCrY = y / 2;

                cbAccumulators[cbCrX][cbCrY] += yCbCr[i][j][1];
                crAccumulators[cbCrX][cbCrY] += yCbCr[i][j][2];
                countAccumulators[cbCrX][cbCrY]++;
            }
        }

        // Calculate and assign the averages for Cb and Cr
        for (int i = 0; i < 8; i++) {

            for (int j = 0; j < 8; j++) {

                blocks[4][i][j] = cbAccumulators[i][j] / countAccumulators[i][j];
                blocks[5][i][j] = crAccumulators[i][j] / countAccumulators[i][j];
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

        int DC_step_size = 8;

        for (int i = 0; i < BLOCK_SIZE; i++) {

            for (int j = 0; j < BLOCK_SIZE; j++) {

                if (i == 0 && j == 0) {

                    // Apply DC step size for the top-left element.
                    // Not sure if ronding up or down is better
                    quantized[i][j] = (int) Math.round((matrix[i][j] / DC_step_size));
                    //  quantized[i][j] = (int) matrix[i][j] / DC_step_size;
                } else {

                    // rec = quant * (2 * level + 1); level > 0
                    // rec / quant = 2 * level + 1
                    // rec / quant) -1 = 2 * level
                    // rec / quant) -1) / 2 = level
                    //
                    // rec = quant * (2 * level - 1); level < 0
                    // rec / quant = 2 * level - 1
                    // (rec / quant) + 1 = 2 * level
                    // ((rec / quant) + 1) / 2 = level
                    // Apply AC step size for the other elements.
                    double coeff = matrix[i][j];
                    if (coeff < 0) {

                        // quantized[i][j] = (int) (((coeff) / QUANT) + 1) / 2;
                        // Not sure if ronding up or down is better
                        quantized[i][j] = (int) Math.round((((coeff) / QUANT) + 1) / 2);
                    } else {

                        // quantized[i][j] = (int) (((coeff) / QUANT) - 1) / 2;
                        // Not sure if ronding up or down is better
                        quantized[i][j] = (int) Math.round((((coeff) / QUANT) - 1) / 2);
                    }
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
                // Reset the run count after a non-zero value
                run = 0;
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

            int run = sequence[i];
            int level = sequence[i + 1];

            // DC
            if (i == 0) {

                if (level > 254) {

                    level = 254;
                } else if (level < 1) {

                    level = 1;
                }

                if (level == 128) {

                    this.stream.write(0b1111_1111, 8);
                } else {

                    this.stream.write(level, 8);
                }
            } else { // AC

//                boolean foundInVlcTable = false;
//                Map<Integer, Pair<Integer, Integer>> runMap = VLC_TABLE_TCOEFF.get(run);
//                if (runMap != null) {
//
//                    Pair<Integer, Integer> codeAndBitsPair = runMap.get(level);
//                    if (codeAndBitsPair != null) {
//
//                        foundInVlcTable = true;
//                        this.stream.write(codeAndBitsPair.getKey(), codeAndBitsPair.getValue());
//                    }
//                }
//
//                if (!foundInVlcTable) { // Fixed length code

                this.stream.write(0b0000_01, 6); // ESCAPE (6 bits)
                this.stream.write(run, 6); // RUN (6 bits)
                this.stream.write(level, 8); // LEVEL (8 bits)
//                }
            }
        }

        // Check if this is needed
//        if (sequence.length == 0) {
//
//            this.stream.write(1, 8);
//        }
    }

    private void writeBlockEnd () throws IOException {

        this.stream.write(2, 2);
    }
}
