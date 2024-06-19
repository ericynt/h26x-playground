package org.ijntema.eric.model.frame;

import lombok.Data;
import org.ijntema.eric.model.frame.gob.GOB;

@Data
public class Picture {

    public static final int WIDTH       = 352;
    public static final int HEIGHT      = 288;
    public static final int GOB_ROWS    = 6;
    public static final int GOB_COLUMNS = 2;

    private FrameType frameType;
    private GOB[][]   gobs;

    public Picture () {

        this.gobs = new GOB[GOB_ROWS][GOB_COLUMNS];
    }

    public byte[] toByteArray () {

        return null;
    }
}
