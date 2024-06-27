package org.ijntema.eric.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.BitSet;

import lombok.Data;

@Data
public class GOB implements ByteArrayable {

    private static final int GBSC = 0x0001;  // Group of Blocks Start Code (16 bits)
    private              int groupNumber;  // Group number (4 bits)
    private              int gquant;  // Quantizer information (5 bits)

    public static final int MACROBLOCK_ROWS    = 3;
    public static final int MACROBLOCK_COLUMNS = 11;

    private Macroblock[][] macroblocks;

    public GOB () {

        this.macroblocks = new Macroblock[MACROBLOCK_ROWS][MACROBLOCK_COLUMNS];
    }

    public GOB (int groupNumber, int gquant) {

        this.groupNumber = groupNumber;
        this.gquant = gquant;
        this.macroblocks = new Macroblock[MACROBLOCK_ROWS][MACROBLOCK_COLUMNS];
    }

    private byte[] createHeader () {

        byte[] header = new byte[4];
        header[0] = 0b0000_0000; // 8 bit start code part
        header[1] = 0b0000_0001; // 8 bit start code part
        header[3] = (byte) (groupNumber << 4 | gquant >> 1); // 8 bit group number and quantizer
        header[4] = (byte) (gquant << 7); // 1 bit quantizer, 1 bit extra insertion information

        return header;
    }

    @Override
    public byte[] toByteArray () throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write((GBSC >> 8) & 0xFF);
        baos.write(GBSC & 0xFF);
        baos.write((groupNumber << 4 | gquant) & 0xFF);

        for (int i = 0; i < MACROBLOCK_ROWS; i++) {

            for (int j = 0; j < MACROBLOCK_COLUMNS; j++) {

                Macroblock macroblock = macroblocks[i][j];
                baos.write(macroblock.toByteArray());
            }
        }

        BitSet bitSet = new BitSet();
        bitSet.set(0, true);

        return baos.toByteArray();
    }
}