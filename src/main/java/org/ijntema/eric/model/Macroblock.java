package org.ijntema.eric.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    public byte[] toByteArray() throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(address);
        baos.write(type);
        baos.write(quant);

        for (Block block : blocks) {
            baos.write(block.toByteArray());
        }

        return baos.toByteArray();
    }
}
