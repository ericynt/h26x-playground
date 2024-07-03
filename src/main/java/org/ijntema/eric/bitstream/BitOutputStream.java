/*
 * Copyright 2017, Chaofan. All rights reserved.
 * Use is subject to license terms.
 */
package org.ijntema.eric.bitstream;

import java.io.IOException;
import java.io.OutputStream;

import lombok.Getter;
import lombok.Setter;

// Updated by Eric IJntema

/**
 * Base class of bit output streams.
 *
 * @author Chaofan
 */
public abstract class BitOutputStream extends BitStream {

    @Getter
    protected OutputStream outputStream;
    @Getter
    @Setter
    protected int          buffer;
    @Getter
    @Setter
    protected int bufferBitCount;

    /**
     * Initializes a bit output stream from an OutputStream.
     *
     * @param outputStream the OutputStream.
     */
    public BitOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
        this.bufferBitCount = 0;
    }

    /**
     * Writes some bits.
     *
     * @param data          bits to be written, which are stored in low bits.
     * @param numBits       bits count to be written.
     * @throws IOException  if an I/O error occurs.
     */
    public abstract void write(int data, int numBits) throws IOException;

    /**
     * Writes all bits in buffer to the OutputStream. Then closes it.
     *
     * @throws IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
        if (bufferBitCount > 0) {
            outputStream.write(buffer);
        }
        outputStream.close();
    }
}
