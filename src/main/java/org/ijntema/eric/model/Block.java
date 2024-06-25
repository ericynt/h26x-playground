package org.ijntema.eric.model;

import lombok.Data;

@Data
public class Block implements ByteArrayable {

    private byte[] coefficients;  // Transform coefficients (variable length)

    public static final int BLOCK_SIZE = 8;

    @Override
    public byte[] toByteArray () {

        return coefficients;
    }
}
