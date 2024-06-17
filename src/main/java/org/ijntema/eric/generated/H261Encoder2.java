package org.ijntema.eric.generated;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class H261Encoder2 {

    private static final int WIDTH      = 352;
    private static final int HEIGHT     = 288;
    private static final int BLOCK_SIZE = 8;

    public void preprocessFrame (int[][] frame) {
        int paddedWidth = (WIDTH % BLOCK_SIZE == 0) ? WIDTH : WIDTH + BLOCK_SIZE - (WIDTH % BLOCK_SIZE);
        int paddedHeight = (HEIGHT % BLOCK_SIZE == 0) ? HEIGHT : HEIGHT + BLOCK_SIZE - (HEIGHT % BLOCK_SIZE);
        int[][] paddedFrame = new int[paddedHeight][paddedWidth];

        for (int y = 0; y < HEIGHT; y++) {
            System.arraycopy(frame[y], 0, paddedFrame[y], 0, WIDTH);
        }

        for (int y = HEIGHT; y < paddedHeight; y++) {
            Arrays.fill(paddedFrame[y], 0);
        }

        frame = paddedFrame;
    }

    public int[][][] splitIntoBlocks (int[][] frame) {
        int paddedWidth = frame[0].length;
        int paddedHeight = frame.length;
        int blocksPerRow = paddedWidth / BLOCK_SIZE;
        int blocksPerCol = paddedHeight / BLOCK_SIZE;
        int[][][] blocks = new int[blocksPerCol][blocksPerRow][BLOCK_SIZE * BLOCK_SIZE];

        for (int y = 0; y < blocksPerCol; y++) {
            for (int x = 0; x < blocksPerRow; x++) {
                for (int blockY = 0; blockY < BLOCK_SIZE; blockY++) {
                    System.arraycopy(frame[y * BLOCK_SIZE + blockY], x * BLOCK_SIZE, blocks[y][x], blockY * BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }

        return blocks;
    }

    public double[][][] applyDCT (int[][][] blocks) {
        int blockRows = blocks.length;
        int blockCols = blocks[0].length;
        double[][][] dctBlocks = new double[blockRows][blockCols][BLOCK_SIZE * BLOCK_SIZE];

        for (int i = 0; i < blockRows; i++) {
            for (int j = 0; j < blockCols; j++) {
                dctBlocks[i][j] = applyDCTToBlock(blocks[i][j]);
            }
        }

        return dctBlocks;
    }

    private double[] applyDCTToBlock (int[] block) {
        int N = BLOCK_SIZE;
        double[] dctBlock = new double[N * N];
        double[][] block2D = new double[N][N];

        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                block2D[i][j] = block[i * N + j];
            }
        }

        for (int u = 0; u < N; u++) {
            for (int v = 0; v < N; v++) {
                double sum = 0.0;

                for (int x = 0; x < N; x++) {
                    for (int y = 0; y < N; y++) {
                        sum += block2D[x][y] *
                                Math.cos((2 * x + 1) * u * Math.PI / (2 * N)) *
                                Math.cos((2 * y + 1) * v * Math.PI / (2 * N));
                    }
                }

                double alphaU = (u == 0) ? Math.sqrt(1.0 / N) : Math.sqrt(2.0 / N);
                double alphaV = (v == 0) ? Math.sqrt(1.0 / N) : Math.sqrt(2.0 / N);
                dctBlock[u * N + v] = alphaU * alphaV * sum;
            }
        }

        return dctBlock;
    }

    public int[][][] quantize (double[][][] dctBlocks) {
        int blockRows = dctBlocks.length;
        int blockCols = dctBlocks[0].length;
        int[][][] quantizedBlocks = new int[blockRows][blockCols][BLOCK_SIZE * BLOCK_SIZE];

        // Example quantization matrix
        int[][] quantizationMatrix = {
                {16, 11, 10, 16, 24, 40, 51, 61},
                {12, 12, 14, 19, 26, 58, 60, 55},
                {14, 13, 16, 24, 40, 57, 69, 56},
                {14, 17, 22, 29, 51, 87, 80, 62},
                {18, 22, 37, 56, 68, 109, 103, 77},
                {24, 35, 55, 64, 81, 104, 113, 92},
                {49, 64, 78, 87, 103, 121, 120, 101},
                {72, 92, 95, 98, 112, 100, 103, 99}
        };

        for (int i = 0; i < blockRows; i++) {
            for (int j = 0; j < blockCols; j++) {
                for (int u = 0; u < BLOCK_SIZE; u++) {
                    for (int v = 0; v < BLOCK_SIZE; v++) {
                        int index = u * BLOCK_SIZE + v;
                        quantizedBlocks[i][j][index] = (int) Math.round(dctBlocks[i][j][index] / quantizationMatrix[u][v]);
                    }
                }
            }
        }

        return quantizedBlocks;
    }

    public int[] runLengthEncode (int[][][] quantizedBlocks) {
        List<Integer> rleList = new ArrayList<>();

        for (int[][] block : quantizedBlocks) {
            for (int[] coefficient : block) {
                int runLength = 0;
                for (int value : coefficient) {
                    if (value == 0) {
                        runLength++;
                    } else {
                        if (runLength > 0) {
                            rleList.add(0); // Add zero to indicate a run of zeros
                            rleList.add(runLength);
                            runLength = 0;
                        }
                        rleList.add(value);
                    }
                }
                if (runLength > 0) {
                    rleList.add(0);
                    rleList.add(runLength);
                }
            }
        }

        int[] rleArray = new int[rleList.size()];
        for (int i = 0; i < rleList.size(); i++) {
            rleArray[i] = rleList.get(i);
        }

        return rleArray;
    }

    public byte[] huffmanEncode (int[] rleData) {
        Map<Integer, Integer> freqMap = new HashMap<>();
        for (int num : rleData) {
            freqMap.put(num, freqMap.getOrDefault
                    (num, 0) + 1);
        }

        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
            pq.offer(new Node(entry.getKey(), entry.getValue()));
        }
        while (pq.size() > 1) {
            Node left = pq.poll();
            Node right = pq.poll();
            Node parent = new Node(left, right);
            pq.offer(parent);
        }

        Map<Integer, String> huffmanCodes = new HashMap<>();
        buildHuffmanCodes(pq.peek(), "", huffmanCodes);

        StringBuilder encodedData = new StringBuilder();
        for (int num : rleData) {
            encodedData.append(huffmanCodes.get(num));
        }

        int len = (encodedData.length() + 7) / 8;
        byte[] result = new byte[len];
        for (int i = 0; i < encodedData.length(); i += 8) {
            int end = Math.min(i + 8, encodedData.length());
            String chunk = encodedData.substring(i, end);
            result[i / 8] = (byte) Integer.parseInt(chunk, 2);
        }

        return result;
    }

    private void buildHuffmanCodes (Node node, String code, Map<Integer, String> huffmanCodes) {
        if (node != null) {
            if (node.left == null && node.right == null) {
                huffmanCodes.put(node.data, code);
            } else {
                buildHuffmanCodes(node.left, code + "0", huffmanCodes);
                buildHuffmanCodes(node.right, code + "1", huffmanCodes);
            }
        }
    }

    private static class Node implements Comparable<Node> {
        int  data;
        int  freq;
        Node left;
        Node right;

        public Node (int data, int freq) {
            this.data = data;
            this.freq = freq;
        }

        public Node (Node left, Node right) {
            this.left = left;
            this.right = right;
            this.freq = left.freq + right.freq;
        }

        @Override
        public int compareTo (Node other) {
            return this.freq - other.freq;
        }
    }

    public byte[] encodeFrame (int[][] frame) {
        preprocessFrame(frame);
        int[][][] blocks = splitIntoBlocks(frame);
        double[][][] dctBlocks = applyDCT(blocks);
        int[][][] quantizedBlocks = quantize(dctBlocks);
        int[] rleData = runLengthEncode(quantizedBlocks);
        return huffmanEncode(rleData);
    }

    public int[][] readJPEG (String filename) throws IOException {
        BufferedImage img = ImageIO.read(new File(filename));
        int[][] frame = new int[HEIGHT][WIDTH];

        BufferedImage resized = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        resized.getGraphics().drawImage(img, 0, 0, WIDTH, HEIGHT, null);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                frame[y][x] = resized.getRaster().getSample(x, y, 0);
            }
        }

        return frame;
    }

    public static void main (String[] args) {
        H261Encoder2 encoder = new H261Encoder2();

        if (args.length == 0) {
            System.out.println("Please provide the JPEG filenames as command-line arguments.");
            return;
        }

        for (String filename : args) {
            try {
                int[][] frame = encoder.readJPEG(filename);
                byte[] encodedData = encoder.encodeFrame(frame);
                System.out.println("Encoded frame data length for " + filename + ": " + encodedData.length);
            } catch (IOException e) {
                System.out.println("Error reading file " + filename + ": " + e.getMessage());
            }
        }
    }
}
