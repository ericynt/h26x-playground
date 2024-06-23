package org.ijntema.eric.model;

import java.nio.ByteBuffer;

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

    @Override
    public byte[] toByteArray () {

        ByteBuffer buffer = ByteBuffer.allocate(512);  // Adjust size as needed
        buffer.putShort((short) GBSC);
        buffer.put((byte) (groupNumber << 4 | gquant));

        for (int i = 0; i < MACROBLOCK_ROWS; i++) {

            for (int j = 0; j < MACROBLOCK_COLUMNS; j++) {

                Macroblock macroblock = macroblocks[i][j];
                buffer.put(macroblock.toByteArray());
            }
        }

        return buffer.array();
    }
}
