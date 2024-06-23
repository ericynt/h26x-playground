package org.ijntema.eric.model;

import java.nio.ByteBuffer;

import lombok.Data;

@Data
public class Macroblock implements ByteArrayable {

    private int address;  // Macroblock address (variable length)
    private int type;  // Macroblock type (variable length)
    private int quant;  // Quantizer (optional, 5 bits)

    public static final int Y_BLOCKS_AMOUNT  = 4;
    public static final int CB_BLOCKS_AMOUNT = 1;
    public static final int CR_BLOCKS_AMOUNT = 1;
    // YCbCr 4:2:0
    public static final int TOTAL_BLOCKS     = Y_BLOCKS_AMOUNT + CB_BLOCKS_AMOUNT + CR_BLOCKS_AMOUNT;

    private boolean different = false;

    private Block[] blocks;

    public Macroblock () {

        this.blocks = new Block[TOTAL_BLOCKS];
    }

    @Override
    public byte[] toByteArray () {

        ByteBuffer buffer = ByteBuffer.allocate(256);  // Adjust size as needed
        buffer.put((byte) address);
        buffer.put((byte) type);
        buffer.put((byte) quant);

        for (Block block : blocks) {
            buffer.put(block.toByteArray());
        }

        return buffer.array();
    }
}
