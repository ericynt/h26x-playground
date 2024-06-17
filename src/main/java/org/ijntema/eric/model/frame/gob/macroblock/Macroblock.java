package org.ijntema.eric.model.frame.gob.macroblock;

import org.ijntema.eric.model.frame.gob.macroblock.microblock.CbBlock;
import org.ijntema.eric.model.frame.gob.macroblock.microblock.CrBlock;
import org.ijntema.eric.model.frame.gob.macroblock.microblock.YBlock;

public class Macroblock {

    private YBlock[] yBlocks;
    private CrBlock  crBlock;
    private CbBlock  cbBlock;
}
