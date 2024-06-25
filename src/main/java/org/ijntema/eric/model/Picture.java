package org.ijntema.eric.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lombok.Data;

@Data
public class Picture implements ByteArrayable {

    private static final int PSC = 0x000100;  // Picture Start Code (20 bits)
    private              int temporalReference;  // Temporal reference (5 bits)
    private              int ptype;  // Type information (6 bits)

    public static final int WIDTH       = 352;
    public static final int HEIGHT      = 288;
    public static final int GOB_ROWS    = 6;
    public static final int GOB_COLUMNS = 2;

    private FrameType frameType;
    private GOB[][]   gobs;

    public Picture () {

        this.gobs = new GOB[GOB_ROWS][GOB_COLUMNS];
    }

    public Picture (int temporalReference, int ptype, FrameType frameType) {

        this.temporalReference = temporalReference;
        this.ptype = ptype;
        this.frameType = frameType;
        this.gobs = new GOB[GOB_ROWS][GOB_COLUMNS];
    }

    @Override
    public byte[] toByteArray () throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // Assemble the header
        int header = PSC << 12 | temporalReference << 7 | ptype;
        baos.write((header >> 16) & 0xFF);
        baos.write((header >> 8) & 0xFF);
        baos.write(header & 0xFF);

        for (int i = 0; i < GOB_ROWS; i++) {

            for (int j = 0; j < GOB_COLUMNS; j++) {

                GOB gob = gobs[i][j];
                baos.write(gob.toByteArray());
            }
        }

        return baos.toByteArray();
    }
}
