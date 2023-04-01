package com.mkkl.hantekapi.communication.readers.sync;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.communication.adcdata.AdcInputStream;
import com.mkkl.hantekapi.communication.readers.ScopeDataReader;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Class used for reading data from usb endpoints.
 */
public class SyncScopeDataReader extends ScopeDataReader {

    public SyncScopeDataReader(Oscilloscope oscilloscope) {
        super(oscilloscope);
    }

    /**
     * Synchronous reading from oscilloscope's ADC.
     * @param size The number of data points for both channels to retrieve. size/2 samples per channel.
     * @return raw ADC data. Use {@link AdcInputStream} to format this output
     */
    public ByteBuffer readToBuffer(short size) {
        oscilloscope.ensureCaptureStarted();
        return endpoint.syncReadPipe(size);
    }

    public ByteBuffer readToBuffer() {
        return readToBuffer(defaultSize);
    }

    public byte[] readToByteArray(short size) {
        ByteBuffer byteBuffer = readToBuffer(size);
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes, 0, bytes.length);
        return bytes;
    }

    public byte[] readToByteArray() {
        return readToByteArray(defaultSize);
    }


    @Override
    public void close() throws IOException {

    }
}
