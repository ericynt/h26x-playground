package org.ijntema.eric.model.frame.gob.macroblock;

import lombok.Data;
import org.ijntema.eric.model.frame.gob.macroblock.block.Block;

@Data
public class Macroblock {

    public static final int Y_BLOCKS_AMOUNT  = 4;
    public static final int CB_BLOCKS_AMOUNT = 1;
    public static final int CR_BLOCKS_AMOUNT = 1;

    private Block[] yBlocks;
    private Block   cbBlock;
    private Block   crBlock;

    public Macroblock () {

        this.yBlocks = new Block[Y_BLOCKS_AMOUNT];
    }

    public byte[] toByteArray () {

        return null;
    }
}
