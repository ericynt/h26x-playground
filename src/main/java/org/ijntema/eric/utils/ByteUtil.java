package org.ijntema.eric.utils;

public class ByteUtil {

    public static byte[] concatenateByteArrays (byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];

        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);

        return result;
    }

    public static String intToBinaryString (byte byteValue, int groupSize) {

        StringBuilder result = new StringBuilder();

        for (int i = 7; i >= 0; i--) {
            int mask = 1 << i;
            result.append((byteValue & mask) != 0 ? "1" : "0");

            if (i % groupSize == 0) {
                result.append(" ");
            }
        }
        result.replace(result.length() - 1, result.length(), "");

        return result.toString();
    }
}
