package org.ijntema.eric.encoders;

import java.util.HashMap;
import java.util.Map;

public class H261Huffman {
    private static final Map<String, String> HUFFMAN_TABLE = new HashMap<>();

    static {
        // Populating Huffman table for TCOEFF Run Level Code
        // The table should be completed based on the H.261 specification.
        HUFFMAN_TABLE.put("EOB", "10");
        HUFFMAN_TABLE.put("0,1", "11s");
        HUFFMAN_TABLE.put("0,2", "0100s");
        HUFFMAN_TABLE.put("0,3", "00101s");
        HUFFMAN_TABLE.put("0,4", "000111s");
        HUFFMAN_TABLE.put("0,5", "0000111s");
        HUFFMAN_TABLE.put("0,6", "00000111s");
        HUFFMAN_TABLE.put("0,7", "000000111s");
        HUFFMAN_TABLE.put("0,8", "0000000111s");
        HUFFMAN_TABLE.put("0,9", "00000000111s");
        HUFFMAN_TABLE.put("1,1", "011s");
        HUFFMAN_TABLE.put("1,2", "00111s");
        HUFFMAN_TABLE.put("1,3", "000110s");
        HUFFMAN_TABLE.put("1,4", "0000101s");
        HUFFMAN_TABLE.put("1,5", "000001011s");
        HUFFMAN_TABLE.put("1,6", "0000001011s");
        HUFFMAN_TABLE.put("1,7", "00000001011s");
        HUFFMAN_TABLE.put("2,1", "00110s");
        HUFFMAN_TABLE.put("2,2", "000101s");
        HUFFMAN_TABLE.put("2,3", "0000110s");
        HUFFMAN_TABLE.put("2,4", "00000110s");
        HUFFMAN_TABLE.put("3,1", "00100s");
        HUFFMAN_TABLE.put("3,2", "000100s");
        HUFFMAN_TABLE.put("3,3", "0000100s");
        HUFFMAN_TABLE.put("4,1", "000110s");
        HUFFMAN_TABLE.put("4,2", "000011s");
        HUFFMAN_TABLE.put("5,1", "000101s");
        HUFFMAN_TABLE.put("6,1", "000100s");
        HUFFMAN_TABLE.put("7,1", "000011s");
        HUFFMAN_TABLE.put("8,1", "000010s");
        HUFFMAN_TABLE.put("9,1", "000001s");
        HUFFMAN_TABLE.put("Escape", "0000000");
        // Additional entries should be added based on the document.
    }

    public void getEncodedData(int[] sequence) {

        StringBuilder encodedBits = new StringBuilder();

        for (int i = 0; i < sequence.length; i += 2) {
            int run = sequence[i];
            int level = sequence[i + 1];
            String key = run + "," + level;
            String huffmanCode = HUFFMAN_TABLE.get(key);

            if (huffmanCode == null) {
                // Escape coding for values not in the table
                huffmanCode = HUFFMAN_TABLE.get("Escape") +
                        String.format("%6s", Integer.toBinaryString(run)).replace(' ', '0') +
                        String.format("%8s", Integer.toBinaryString(Math.abs(level))).replace(' ', '0');
                if (level < 0) {
                    huffmanCode += "1";
                } else {
                    huffmanCode += "0";
                }
            } else {
                if (level < 0) {
                    huffmanCode = huffmanCode.replace('s', '1');
                } else {
                    huffmanCode = huffmanCode.replace('s', '0');
                }
            }

            encodedBits.append(huffmanCode);
        }

        // Convert the bit string to byte array
        int byteCount = (encodedBits.length() + 7) / 8;
        byte[] encodedData = new byte[byteCount];
        for (int i = 0; i < encodedBits.length(); i++) {
            if (encodedBits.charAt(i) == '1') {
                encodedData[i / 8] |= 1 << (7 - (i % 8));
            }
        }
    }
}
