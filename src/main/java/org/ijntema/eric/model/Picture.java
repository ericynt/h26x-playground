package org.ijntema.eric.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lombok.Data;
import org.ijntema.eric.bitstream.BigEndianBitOutputStream;

import static org.ijntema.eric.utils.ByteUtil.intToBinaryString;

@Data
public class Picture implements ByteArrayable {

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
        BigEndianBitOutputStream bebaos = new BigEndianBitOutputStream(baos);

        // Write the header
        baos.write(createHeader());

        for (int i = 0; i < GOB_ROWS; i++) {

            for (int j = 0; j < GOB_COLUMNS; j++) {

                GOB gob = gobs[i][j];
                baos.write(gob.toByteArray());
            }
        }

        return baos.toByteArray();
    }

    private byte[] createHeader () {

        byte[] header = new byte[4];
        header[0] = 0b0000_0000; // 8 bit start code part
        header[1] = 0b0000_0001; // 8 bit start code part
        header[2] = (byte) (temporalReference >> 1); // 4 bit start code part + 4 bit temporal reference part
        header[3] = (byte) (temporalReference << 7 | ptype << 1); // 1 bit temporal reference part + 6 bit ptype + 1 bit extra insertion information

        return header;
    }

    @Override
    public String toString () {

        byte[] header = createHeader();

        StringBuilder sb = new StringBuilder();
        sb.append("Picture Header:\n");
        for (byte b : header) {
            sb.append(intToBinaryString(b, 4));
            sb.append(" ");
        }

        return sb.toString();
    }
}
