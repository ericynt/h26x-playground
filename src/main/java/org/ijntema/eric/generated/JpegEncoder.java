package org.ijntema.eric.generated;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class JpegEncoder {

    // Quantization matrix for luminance
    private static final int[][] LUMINANCE_QUANT_TABLE = {
        {16, 11, 10, 16, 24, 40, 51, 61},
        {12, 12, 14, 19, 26, 58, 60, 55},
        {14, 13, 16, 24, 40, 57, 69, 56},
        {14, 17, 22, 29, 51, 87, 80, 62},
        {18, 22, 37, 56, 68, 109, 103, 77},
        {24, 35, 55, 64, 81, 104, 113, 92},
        {49, 64, 78, 87, 103, 121, 120, 101},
        {72, 92, 95, 98, 112, 100, 103, 99}
    };

    public static void main(String[] args) throws IOException {
        BufferedImage image = ImageIO.read(new File("input.jpg"));
        int width = image.getWidth();
        int height = image.getHeight();

        // Step 1: Color Space Conversion (RGB to YCbCr)
        double[][][] yCbCr = rgbToYCbCr(image, width, height);

        // Step 2: Block Splitting into 8x8 blocks
        int[][][] blocks = splitIntoBlocks(yCbCr, width, height);

        // Step 3: Apply DCT
        double[][][] dctBlocks = applyDCT(blocks);

        // Step 4: Quantization
        int[][][] quantizedBlocks = quantize(dctBlocks);

        // Step 5: Zigzag Ordering
        int[][] zigzagBlocks = new int[quantizedBlocks.length][64];
        for (int i = 0; i < quantizedBlocks.length; i++) {
            zigzagBlocks[i] = zigzagOrdering(quantizedBlocks[i]);
        }

        // Step 6: Run-Length Encoding (RLE)
        int[][] rleBlocks = new int[zigzagBlocks.length][];
        for (int i = 0; i < zigzagBlocks.length; i++) {
            rleBlocks[i] = runLengthEncoding(zigzagBlocks[i]);
        }

        // Step 7: Huffman Encoding
        byte[] huffmanEncoded = huffmanEncoding(rleBlocks);

        // Step 8: Write to JPEG file format
        writeToFile(huffmanEncoded, "output.jpg");

        System.out.println("JPEG image successfully written to output.jpg");
    }

    // Convert RGB to YCbCr
    private static double[][][] rgbToYCbCr(BufferedImage image, int width, int height) {
        double[][][] yCbCr = new double[height][width][3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                yCbCr[y][x][0] = 0.299 * r + 0.587 * g + 0.114 * b;
                yCbCr[y][x][1] = -0.1687 * r - 0.3313 * g + 0.5 * b + 128;
                yCbCr[y][x][2] = 0.5 * r - 0.4187 * g - 0.0813 * b + 128;
            }
        }
        return yCbCr;
    }

    // Split into 8x8 blocks
    private static int[][][] splitIntoBlocks(double[][][] yCbCr, int width, int height) {
        int blocksPerRow = width / 8;
        int blocksPerColumn = height / 8;
        int[][][] blocks = new int[blocksPerColumn * blocksPerRow][8][8];
        int blockIndex = 0;
        for (int by = 0; by < blocksPerColumn; by++) {
            for (int bx = 0; bx < blocksPerRow; bx++) {
                for (int y = 0; y < 8; y++) {
                    for (int x = 0; x < 8; x++) {
                        blocks[blockIndex][y][x] = (int) yCbCr[by * 8 + y][bx * 8 + x][0];
                    }
                }
                blockIndex++;
            }
        }
        return blocks;
    }

    // Apply DCT (Discrete Cosine Transform)
    private static double[][][] applyDCT(int[][][] blocks) {
        int blockCount = blocks.length;
        double[][][] dctBlocks = new double[blockCount][8][8];
        for (int i = 0; i < blockCount; i++) {
            for (int u = 0; u < 8; u++) {
                for (int v = 0; v < 8; v++) {
                    double sum = 0.0;
                    for (int x = 0; x < 8; x++) {
                        for (int y = 0; y < 8; y++) {
                            sum += blocks[i][x][y] *
                                   Math.cos((2 * x + 1) * u * Math.PI / 16) *
                                   Math.cos((2 * y + 1) * v * Math.PI / 16);
                        }
                    }
                    double cu = (u == 0) ? 1.0 / Math.sqrt(2) : 1.0;
                    double cv = (v == 0) ? 1.0 / Math.sqrt(2) : 1.0;
                    dctBlocks[i][u][v] = 0.25 * cu * cv * sum;
                }
            }
        }
        return dctBlocks;
    }

    // Quantize the DCT coefficients
    private static int[][][] quantize(double[][][] dctBlocks) {
        int blockCount = dctBlocks.length;
        int[][][] quantizedBlocks = new int[blockCount][8][8];
        for (int i = 0; i < blockCount; i++) {
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    quantizedBlocks[i][y][x] = (int) Math.round(dctBlocks[i][y][x] / LUMINANCE_QUANT_TABLE[y][x]);
                }
            }
        }
        return quantizedBlocks;
    }

    // Zigzag ordering
    private static int[] zigzagOrdering(int[][] quantizedBlock) {
        int[] zigzag = new int[64];
        int[][] zigzagOrder = {
            {0, 1, 5, 6, 14, 15, 27, 28},
            {2, 4, 7, 13, 16, 26, 29, 42},
            {3, 8, 12, 17, 25, 30, 41, 43},
            {9, 11, 18, 24, 31, 40, 44, 53},
            {10, 19, 23, 32, 39, 45, 52, 54},
            {20, 22, 33, 38, 46, 51, 55, 60},
            {21, 34, 37, 47, 50, 56, 59, 61},
            {35, 36, 48, 49, 57, 58, 62, 63}
        };
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                zigzag[zigzagOrder[y][x]] = quantizedBlock[y][x];
            }
        }
        return zigzag;
    }

    // Run-Length Encoding (RLE)
    private static int[] runLengthEncoding(int[] zigzag) {
        int[] rle = new int[zigzag.length * 2];
        int rleIndex = 0;
        int zeroCount = 0;
        for (int i = 0; i < zigzag.length; i++) {
            if (zigzag[i] == 0) {
                zeroCount++;
            } else {
                while (zeroCount > 15) {
                    rle[rleIndex++] = 15;
                    rle[rleIndex++] = 0;
                    zeroCount -= 16;
                }
                rle[rleIndex++] = zeroCount;
                rle[rleIndex++] = zigzag[i];
                zeroCount = 0;
            }
        }
        rle[rleIndex++] = 0;  // End of block marker
        return Arrays.copyOf(rle, rleIndex);
    }

    // Huffman Encoding
    private static byte[] huffmanEncoding(int[][] rleBlocks) {
        // Placeholder: Huffman encoding is complex and would require a separate implementation
        // For this example, we will return the RLE data directly
        int length = 0;
        for (int[] block : rleBlocks) {
            length += block.length;
        }
        byte[] huffmanEncoded = new byte[length];
        int index = 0;
        for (int[] block : rleBlocks) {
            for (int value : block) {
                huffmanEncoded[index++] = (byte) value;
            }
        }
        return huffmanEncoded;
    }

    // Write to JPEG file format
    private static void writeToFile(byte[] huffmanEncoded, String fileName) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            // Write JPEG file header (simplified)
            fos.write(new byte[]{(byte) 0xFF, (byte) 0xD8}); // SOI marker
            fos.write(new byte[]{(byte) 0xFF, (byte) 0xE0}); // APP0 marker
            fos.write(new byte[]{0x00, 0x10}); // Length of APP0 segment
            fos.write("JFIF\0".getBytes()); // JFIF identifier
            fos.write(new byte[]{0x01, 0x01}); // JFIF version
            fos.write(new byte[]{0x00}); // No density units
            fos.write(new byte[]{0x00, 0x01, 0x00, 0x01}); // X and Y density
            fos.write(new byte[]{0x00, 0x00}); // Thumbnail width and height

            // Write DQT (Define Quantization Table)
            fos.write(new byte[]{(byte) 0xFF, (byte) 0xDB}); // DQT marker
            fos.write(new byte[]{0x00, 0x43}); // Length of DQT segment
            fos.write(new byte[]{0x00}); // Quantization table identifier
            for (int[] row : LUMINANCE_QUANT_TABLE) {
                for (int value : row) {
                    fos.write(value);
                }
            }

            // Write Start of Frame (SOF0)
            fos.write(new byte[]{(byte) 0xFF, (byte) 0xC0}); // SOF0 marker
            fos.write(new byte[]{0x00, 0x11}); // Length of SOF0 segment
            fos.write(new byte[]{0x08}); // Sample precision
//            fos.write(new byte[]{0x00, (byte) height}); // Image height
//            fos.write(new byte[]{0x00, (byte) width}); // Image width
            fos.write(new byte[]{0x03}); // Number of components
            fos.write(new byte[]{0x01, 0x11, 0x00}); // Y component (ID, sampling factors, Q-table)
            fos.write(new byte[]{0x02, 0x11, 0x01}); // Cb component
            fos.write(new byte[]{0x03, 0x11, 0x01}); // Cr component

            // Write Huffman Tables (simplified for example)
            fos.write(new byte[]{(byte) 0xFF, (byte) 0xC4}); // DHT marker
            fos.write(new byte[]{0x00, 0x1F}); // Length of DHT segment
            fos.write(new byte[]{0x00}); // HT information (class and identifier)
            for (int i = 0; i < 16; i++) {
                fos.write(1); // Number of codes of length i+1
            }
            for (int i = 0; i < 16; i++) {
                fos.write(i); // Example values (this should be replaced with actual values)
            }

            // Write Start of Scan (SOS)
            fos.write(new byte[]{(byte) 0xFF, (byte) 0xDA}); // SOS marker
            fos.write(new byte[]{0x00, 0x0C}); // Length of SOS segment
            fos.write(new byte[]{0x03}); // Number of components
            fos.write(new byte[]{0x01, 0x00}); // Y component (ID and HT)
            fos.write(new byte[]{0x02, 0x11}); // Cb component
            fos.write(new byte[]{0x03, 0x11}); // Cr component
            fos.write(new byte[]{0x00, 0x3F, 0x00}); // Spectral selection and approximation

            // Write compressed image data
            fos.write(huffmanEncoded);

            // Write End of Image (EOI)
            fos.write(new byte[]{(byte) 0xFF, (byte) 0xD9}); // EOI marker
        }
    }
}
