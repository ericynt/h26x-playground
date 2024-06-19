package org.ijntema.eric.model.frame.gob.macroblock;

import lombok.Data;
import org.ijntema.eric.model.ToByteArray;
import org.ijntema.eric.model.frame.gob.macroblock.block.Block;

@Data
public class Macroblock implements ToByteArray {

    public static final int Y_BLOCKS_AMOUNT  = 4;
    public static final int CB_BLOCKS_AMOUNT = 1;
    public static final int CR_BLOCKS_AMOUNT = 1;
    public static final int TOTAL_BLOCKS     = Y_BLOCKS_AMOUNT + CB_BLOCKS_AMOUNT + CR_BLOCKS_AMOUNT;

    private boolean different;

    private Block[] blocks;

    public Macroblock () {

        this.blocks = new Block[TOTAL_BLOCKS];
    }

    @Override
    public byte[] toByteArray () {

        return null;
    }
}
