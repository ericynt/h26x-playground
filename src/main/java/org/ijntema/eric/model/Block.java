package org.ijntema.eric.model;

import lombok.Data;

@Data
public class Block implements ByteArrayable {

    private byte[] coefficients;  // Transform coefficients (variable length)

    public static final int BLOCK_SIZE = 8;

    private int[][] pixels;

    @Override
    public byte[] toByteArray () {

        return new byte[0];
    }
}
