package org.ijntema.eric.generated;

import java.util.HashMap;
import java.util.Map;

public class H261HuffmanEncoder {

    private static final Map<Integer, String> FIXED_HUFFMAN_TABLE = new HashMap<>();

    static {
        // Example Huffman codes for illustrative purposes. Replace with actual H.261 codes.
        FIXED_HUFFMAN_TABLE.put(0, "00");
        FIXED_HUFFMAN_TABLE.put(1, "01");
        FIXED_HUFFMAN_TABLE.put(2, "100");
        FIXED_HUFFMAN_TABLE.put(3, "101");
        FIXED_HUFFMAN_TABLE.put(4, "1100");
        FIXED_HUFFMAN_TABLE.put(5, "1101");
        FIXED_HUFFMAN_TABLE.put(6, "1110");
        FIXED_HUFFMAN_TABLE.put(7, "1111");
        // Add all other H.261 codes here
    }

    private byte[] getEncodedData(int[] runLengthed) {
        StringBuilder encodedString = new StringBuilder();

        for (int value : runLengthed) {
            String code = FIXED_HUFFMAN_TABLE.get(value);
            if (code == null) {
                throw new IllegalArgumentException("Value not in Huffman table: " + value);
            }
            encodedString.append(code);
        }

        int byteLength = (encodedString.length() + 7) / 8;
        byte[] encodedData = new byte[byteLength];
        int index = 0;

        for (int i = 0; i < encodedString.length(); i += 8) {
            String byteString = encodedString.substring(i, Math.min(i + 8, encodedString.length()));
            encodedData[index++] = (byte) Integer.parseInt(byteString, 2);
        }

        return encodedData;
    }

    public byte[] huffmanEncode(final int[] runLengthed) {
        return getEncodedData(runLengthed);
    }

    public static void main(String[] args) {
        H261HuffmanEncoder encoder = new H261HuffmanEncoder();
        int[] runLengthed = {1, 3, 2, 3, 1, 1, 1, 3, 2}; // Example data
        byte[] encodedData = encoder.huffmanEncode(runLengthed);
        for (byte b : encodedData) {
            System.out.print(b + " ");
        }
    }
}
