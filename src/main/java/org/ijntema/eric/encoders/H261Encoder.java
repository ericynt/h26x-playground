package org.ijntema.eric.encoders;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.util.Pair;
import org.ijntema.eric.model.Block;
import org.ijntema.eric.model.FrameType;
import org.ijntema.eric.model.GOB;
import org.ijntema.eric.model.Macroblock;
import org.ijntema.eric.model.Picture;
import org.ijntema.eric.utils.ByteUtil;
import org.ijntema.eric.utils.H261Util;

public class H261Encoder {

    private final SpaceInvaderAnimation frameGenerator = new SpaceInvaderAnimation();
    private       boolean               iFrameOnlyMode = true;
    private       int[][][]             previousyCbCrMatrix;

    private static final int     I_FRAME_INTERVAL           = 24;
    private static final int     FRAMES_PER_SECOND          = 20;
    private static final int[][] ZIGZAG_ORDER               = {
            {0, 1, 5, 6, 14, 15, 27, 28},
            {2, 4, 7, 13, 16, 26, 29, 42},
            {3, 8, 12, 17, 25, 30, 41, 43},
            {9, 11, 18, 24, 31, 40, 44, 53},
            {10, 19, 23, 32, 39, 45, 52, 54},
            {20, 22, 33, 38, 46, 51, 55, 60},
            {21, 34, 37, 47, 50, 56, 59, 61},
            {35, 36, 48, 49, 57, 58, 62, 63}
    };
    private static final double STEP_SIZE = 8.0;

    public static void main (String[] args) throws IOException {

        H261Encoder h261Encoder = new H261Encoder(false);
        h261Encoder.encode();
    }

    public H261Encoder (final boolean mode) {

        this.iFrameOnlyMode = mode;
    }

    public void encode () throws IOException {

        int count = 0;

        while (true) {

            FrameType frameType;
            if (!this.iFrameOnlyMode && count % I_FRAME_INTERVAL == 0) {

                frameType = FrameType.P_FRAME;

                if (count != 0) {

                    count = 0;
                }
            } else {

                frameType = FrameType.I_FRAME;
            }

            try {
                byte[] h261Header = H261Util.createH261Header(0, 0, iFrameOnlyMode, false, 0, 1, 0, 0, 0);
                byte[] h261Stream = createPicture(frameType).toByteArray();
                byte[] h261Packet = ByteUtil.concatenateByteArrays(h261Header, h261Stream);

                // Send the packet

                Thread.sleep(1000 / FRAMES_PER_SECOND);
            } catch (InterruptedException e) {

                e.printStackTrace();
            }

            count++;
        }
    }

    private Picture createPicture (FrameType frametype) throws IOException {

        Picture picture = new Picture();
        picture.setFrameType(frametype);
        BufferedImage bufferedImage = loadImage();
        int[][][] yCbCrMatrix = rgbToYCbCr(bufferedImage);

        for (int i = 0; i < Picture.GOB_ROWS; i++) {

            for (int j = 0; j < Picture.GOB_COLUMNS; j++) {

                picture.getGobs()[i][j] = new GOB();
                for (int k = 0; k < GOB.MACROBLOCK_ROWS; k++) {

                    for (int l = 0; l < GOB.MACROBLOCK_COLUMNS; l++) {

                        // Set constructor params.
                        Macroblock macroblock = picture.getGobs()[i][j].getMacroblocks()[k][l];
                        picture.getGobs()[i][j].getMacroblocks()[k][l] = new Macroblock();
                        Pair<Integer, Integer> marcroblockStartRowAndColumn = getMarcroblockStartRowAndColumn(i, j, k, l);
                        int[][][] blocks = toBlocks(
                                marcroblockStartRowAndColumn,
                                yCbCrMatrix
                        );
                        int[][][] previousBlocks = toBlocks(
                                marcroblockStartRowAndColumn,
                                this.previousyCbCrMatrix
                        );
                        encodeMacroblock(
                                blocks,
                                previousBlocks,
                                macroblock,
                                picture.getFrameType()
                        );
                    }
                }
            }
        }

        this.previousyCbCrMatrix = yCbCrMatrix;

        return picture;
    }

    private int[][][] toBlocks (
            final Pair<Integer, Integer> pixelRowAndColumn,
            final int[][][] yCbCr
    ) {

        int[][][] blocks = new int[Macroblock.TOTAL_BLOCKS][Block.BLOCK_SIZE][Block.BLOCK_SIZE];

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

    private void encodeMacroblock (
            int[][][] blocks,
            int[][][] previousMacroblockBlocks,
            Macroblock macroblock,
            FrameType frameType
    ) {

        if (frameType == FrameType.P_FRAME) {

            blocks = calculateIFrameDiff(blocks, previousMacroblockBlocks, macroblock);
        }

        for (int i = 0; i < Macroblock.TOTAL_BLOCKS; i++) {

            int[][] block = blocks[i];
            double[][] dctBlock = dct(block);
            int[][] quantizedBlock = quantize(dctBlock);
            int[] zigzagOrderSequence = zigzag(quantizedBlock);
            int[] runLengthedSequence = runLength(zigzagOrderSequence);
            byte[] huffmanEncoded = huffman(runLengthedSequence);
            macroblock.getBlocks()[i].setCoefficients(huffmanEncoded);
        }
    }

    public double[][] dct (int[][] matrix) {

        int n = Block.BLOCK_SIZE, m = Block.BLOCK_SIZE;
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

        int[][] quantizedBlocks = new int[Block.BLOCK_SIZE][Block.BLOCK_SIZE];
        for (int i = 0; i < Block.BLOCK_SIZE; i++) {

            for (int j = 0; j < Block.BLOCK_SIZE; j++) {

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

    private byte[] huffman (final int[] sequence) {

        return new H261Huffman().getEncodedData(sequence);
    }


    private static int[][][] calculateIFrameDiff (
            final int[][][] blocks,
            final int[][][] previousBlocks,
            final Macroblock macroblock
    ) {

        // Calculate diff. if it's a P-frame
        for (int i = 0; i < Macroblock.TOTAL_BLOCKS; i++) {

            int[][] block = blocks[i];
            int[][] previousBlock = previousBlocks[i];
            for (int j = 0; j < block.length; j++) {

                for (int k = 0; k < block[j].length; k++) {

                    block[j][k] -= previousBlock[j][k];
                    macroblock.setDifferent(macroblock.isDifferent() || block[j][k] != 0);
                }
            }
        }

        return blocks;
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

    private BufferedImage loadImage () throws IOException {

        return this.frameGenerator.generateFrame();
    }

    private int[][][] rgbToYCbCr (BufferedImage image) {

        int[][][] yCbCr = new int[Picture.HEIGHT][Picture.WIDTH][3];
        for (int y = 0; y < Picture.HEIGHT; y++) {

            for (int x = 0; x < Picture.WIDTH; x++) {

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
