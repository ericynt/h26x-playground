package org.ijntema.eric.model.frame.gob.macroblock.block;

import lombok.Data;

@Data
public class Block {

    private BlockType blockType;

    private int[][] pixels;

    public static final int PIXEL_ROWS = 8;
    public static final int PIXEL_COLUMNS = 8;

    public Block () {

        this.pixels = new int[PIXEL_ROWS][PIXEL_COLUMNS];
    }

    public byte[] toByteArray () {

        return null;
    }
}