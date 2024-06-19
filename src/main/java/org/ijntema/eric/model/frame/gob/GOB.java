package org.ijntema.eric.model.frame.gob;

import lombok.Data;
import org.ijntema.eric.model.frame.gob.macroblock.Macroblock;

@Data
public class GOB {

    public static final int MACROBLOCK_ROWS    = 3;
    public static final int MACROBLOCK_COLUMNS = 11;

    private Macroblock[][] macroblocks;

    public GOB () {

        this.macroblocks = new Macroblock[MACROBLOCK_ROWS][MACROBLOCK_COLUMNS];
    }

    public byte[] toByteArray () {

        return null;
    }
}
