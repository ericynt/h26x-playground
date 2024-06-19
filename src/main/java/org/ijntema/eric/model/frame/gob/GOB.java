package org.ijntema.eric.model.frame.gob;

import lombok.Data;
import org.ijntema.eric.model.ToByteArray;
import org.ijntema.eric.model.frame.gob.macroblock.Macroblock;

@Data
public class GOB implements ToByteArray {

    public static final int MACROBLOCK_ROWS    = 3;
    public static final int MACROBLOCK_COLUMNS = 11;

    private Macroblock[][] macroblocks;

    public GOB () {

        this.macroblocks = new Macroblock[MACROBLOCK_ROWS][MACROBLOCK_COLUMNS];
    }

    @Override
    public byte[] toByteArray () {

        return null;
    }
}
