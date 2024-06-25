package org.ijntema.eric.utils;

public class H261Util {

    // Method to create a byte array for the H.261 header
    public static byte[] createH261Header (
            int sbit,
            int ebit,
            boolean intra,
            boolean mvFlag,
            int gobn,
            int mbap,
            int quant,
            int hmvd,
            int vmvd
    ) {

        byte[] header = new byte[4];

        // Byte 0: SBIT (3 bits), EBIT (3 bits), INTRA (1 bit), MV flag (1 bit)
        header[0] = (byte) ((sbit & 0x07) << 5 |
                (ebit & 0x07) << 2 |
                (intra ? 1 : 0) << 1 |
                (mvFlag ? 1 : 0));

        // Byte 1: GOBN (4 bits), MBAP (5 bits)
        header[1] = (byte) ((gobn & 0x0F) << 4 |
                (mbap & 0x1F) >> 1);

        // Byte 2: MBAP (continued 1 bit), QUANT (5 bits), HMVD (2 bits)
        header[2] = (byte) (((mbap & 0x01) << 7) |
                (quant & 0x1F) << 2 |
                (hmvd & 0x1F) >> 3);

        // Byte 3: HMVD (continued 3 bits), VMVD (5 bits)
        header[3] = (byte) (((hmvd & 0x07) << 5) |
                (vmvd & 0x1F));

        return header;
    }
}
