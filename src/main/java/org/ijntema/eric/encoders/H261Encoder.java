package org.ijntema.eric.encoders;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.util.Pair;
import org.ijntema.eric.bitstream.BigEndianBitOutputStream;

public class H261Encoder {

    private final SpaceInvaderAnimation frameGenerator = new SpaceInvaderAnimation();
    private       boolean               iFrameOnlyMode = true;

    private static final int     FRAMES_PER_SECOND  = 20;
    private static final int[][] ZIGZAG_ORDER       = {
            {0, 1, 5, 6, 14, 15, 27, 28},
            {2, 4, 7, 13, 16, 26, 29, 42},
            {3, 8, 12, 17, 25, 30, 41, 43},
            {9, 11, 18, 24, 31, 40, 44, 53},
            {10, 19, 23, 32, 39, 45, 52, 54},
            {20, 22, 33, 38, 46, 51, 55, 60},
            {21, 34, 37, 47, 50, 56, 59, 61},
            {35, 36, 48, 49, 57, 58, 62, 63}
    };
    public static final  int     PICTURE_WIDTH      = 352;
    public static final  int     PICTURE_HEIGHT     = 288;
    private static final int     GOB_ROWS           = 6;
    private static final int     GOB_COLUMNS        = 2;
    private static final int     MACROBLOCK_ROWS    = 3;
    private static final int     MACROBLOCK_COLUMNS = 11;
    private static final int     Y_BLOCKS_AMOUNT    = 4;
    private static final int     CB_BLOCKS_AMOUNT   = 1;
    private static final int     CR_BLOCKS_AMOUNT   = 1;
    private static final double  STEP_SIZE          = 8.0;
    // YCbCr 4:2:0
    private static final int     TOTAL_BLOCKS       = Y_BLOCKS_AMOUNT + CB_BLOCKS_AMOUNT + CR_BLOCKS_AMOUNT;
    private static final int     BLOCK_SIZE         = 8;

    private BigEndianBitOutputStream bebaos;
    private ByteArrayOutputStream    baos;

    public static void main (String[] args) throws IOException {

        new H261Encoder().encode();
    }

    public H261Encoder () {

        this.baos = new ByteArrayOutputStream();
        this.bebaos = new BigEndianBitOutputStream(this.baos);
    }

    public void encode () throws IOException {

        int temporalReferenceCount = 0;

        while (true) {

            try {

                writeH261Header();

                if (temporalReferenceCount == 32) {

                    temporalReferenceCount = 0;
                }

//                byte[] h261Stream = writePicture(temporalReference, frameType).toByteArray();
                writePicture(temporalReferenceCount);
//                byte[] h261Packet = ByteUtil.concatenateByteArrays(h261Header, h261Stream);

                // Send the packet

                Thread.sleep(1000 / FRAMES_PER_SECOND);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }

            temporalReferenceCount++;
        }
    }

    private void writeH261Header () throws IOException {

        this.bebaos.write(0, 3); // SBIT (3 bits)
        this.bebaos.write(0, 3); // EBIT (3 bits)
        this.bebaos.write(this.iFrameOnlyMode ? 1 : 0, 1); // INTRA (1 bit)
        this.bebaos.write(0, 1); // MV flag (1 bit)
        this.bebaos.write(0, 4); // GOBN (4 bits)
        this.bebaos.write(0, 5); // MBAP (5 bits)
        this.bebaos.write(0, 5); // QUANT (5 bits)
        this.bebaos.write(0, 2); // HMVD (2 bits)
        this.bebaos.write(0, 5); // VMVD (5 bits)
    }

    private void writePicture (int temporalReference) throws IOException {

        this.writePictureHeader(temporalReference);

        int[][][] yCbCrMatrix = rgbToYCbCr(loadImage());

        for (int i = 0; i < GOB_ROWS; i++) {

            for (int j = 0; j < GOB_COLUMNS; j++) {

                this.writeGobHeader(i, j);
                for (int k = 0; k < MACROBLOCK_ROWS; k++) {

                    for (int l = 0; l < MACROBLOCK_COLUMNS; l++) {

//                        // Set constructor params.
//                        Macroblock macroblock = picture.getGobs()[i][j].getMacroblocks()[k][l];
//                        picture.getGobs()[i][j].getMacroblocks()[k][l] = new Macroblock();
                        this.writeMacroblockHeader(k, l);
                        Pair<Integer, Integer> marcroblockStartRowAndColumn = getMarcroblockStartRowAndColumn(i, j, k, l);
                        int[][][] blocks = toBlocks(
                                marcroblockStartRowAndColumn,
                                yCbCrMatrix
                        );
                        writeMacroblock(blocks);
                    }
                }
            }
        }

    }

    private int[][][] toBlocks (
            final Pair<Integer, Integer> pixelRowAndColumn,
            final int[][][] yCbCr
    ) {

        int[][][] blocks = new int[TOTAL_BLOCKS][BLOCK_SIZE][BLOCK_SIZE];

        for (int i = pixelRowAndColumn.getKey(); i < pixelRowAndColumn.getKey() + 16; i++) {

            for (int j = pixelRowAndColumn.getValue(); j < pixelRowAndColumn.getValue() + 16; j++) {

                // Y
                if (i < 8 && j < 8) {

                    blocks[0][i][j] = yCbCr[i][j][0];
                } else if (i < 8) {

                    blocks[1][i][j - 8] = yCbCr[i][j][0];
                } else if (j < 8) {

                    blocks[2][i - 8][j] = yCbCr[i][j][0];
                } else {

                    blocks[3][i - 8][j - 8] = yCbCr[i][j][0];
                }

                // CbCr
                if (i % 2 == 0 && j % 2 == 0) {

                    blocks[4][i / 2][j / 2] = yCbCr[i][j][1];
                    blocks[5][i / 2][j / 2] = yCbCr[i][j][2];
                }
            }
        }

        return blocks;
    }

    private void writePictureHeader (int temporalReference) throws IOException {

        this.bebaos.write(0b0000_0000_0000_0001_0000, 20); // PSC (20 bits)
        this.bebaos.write(temporalReference, 5); // TR (5 bits)
        this.bebaos.write(0b0010_1000, 6); // PTYPE (6 bits), 4th bit is CIF and 6th bit is Spare (Spares should be 1)
        this.bebaos.write(0, 1); // PEI (1 bit)
    }

    private void writeGobHeader (final int i, final int j) {

    }

    private void writeMacroblockHeader (final int k, final int l) {

    }

    private void writeMacroblock (int[][][] blocks) {

        for (int i = 0; i < TOTAL_BLOCKS; i++) {

            int[][] block = blocks[i];
            double[][] dctBlock = dct(block);
            int[][] quantizedBlock = quantize(dctBlock);
            int[] zigzagOrderSequence = zigzag(quantizedBlock);
            int[] runLengthedSequence = runLength(zigzagOrderSequence);
            writeHuffman(runLengthedSequence);

            this.writeBlockEnd();
        }
    }

    private void writeBlockEnd () {

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

        // List to store RLE encoded values
        List<Integer> encodedList = new ArrayList<>();
        int count = 1;

        int previous = sequence[0];

        for (int i = 1; i < sequence.length; i++) {
            int current = sequence[i];
            if (current == previous) {
                count++;
            } else {
                encodedList.add(previous);
                encodedList.add(count);
                previous = current;
                count = 1;
            }
        }
        // Append the last run
        encodedList.add(previous);
        encodedList.add(count);

        // Convert List to int[]
        int[] encodedArray = new int[encodedList.size()];
        for (int i = 0; i < encodedList.size(); i++) {
            encodedArray[i] = encodedList.get(i);
        }

        return encodedArray;
    }

    private void writeHuffman (final int[] sequence) {

        new H261Huffman().getEncodedData(sequence);
    }


    private Pair<Integer, Integer> getMarcroblockStartRowAndColumn (
            final int gobRow,
            final int gobColumn,
            final int macroblockRow,
            final int macroblockColumn
    ) {

        return new Pair<>(
                (gobRow * macroblockRow) + macroblockRow,
                (gobColumn * macroblockColumn) + macroblockColumn
        );
    }

    private BufferedImage loadImage () {

        return this.frameGenerator.generateFrame();
    }

    private int[][][] rgbToYCbCr (BufferedImage image) {

        int[][][] yCbCr = new int[PICTURE_HEIGHT][PICTURE_WIDTH][3];
        for (int y = 0; y < PICTURE_HEIGHT; y++) {

            for (int x = 0; x < PICTURE_WIDTH; x++) {

                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                yCbCr[y][x][0] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
                yCbCr[y][x][1] = (int) (-0.1687 * r - 0.3313 * g + 0.5 * b + 128);
                yCbCr[y][x][2] = (int) (0.5 * r - 0.4187 * g - 0.0813 * b + 128);
            }
        }

        return yCbCr;
    }
}
