package org.ijntema.eric.model.frame.gob.macroblock;

import lombok.Data;
import org.ijntema.eric.model.ToByteArray;

@Data
public class Macroblock implements ToByteArray {

    public static final int Y_BLOCKS_AMOUNT  = 4;
    public static final int CB_BLOCKS_AMOUNT = 1;
    public static final int CR_BLOCKS_AMOUNT = 1;
    // YCbCr 4:2:0
    public static final int TOTAL_BLOCKS     = Y_BLOCKS_AMOUNT + CB_BLOCKS_AMOUNT + CR_BLOCKS_AMOUNT;
    public static final int BLOCK_SIZE       = 8;

    private boolean different = false;

    private int[][][] blocks;

    public Macroblock () {

        this.blocks = new int[TOTAL_BLOCKS][BLOCK_SIZE][BLOCK_SIZE];
    }

    @Override
    public byte[] toByteArray () {

        return null;
    }
}
