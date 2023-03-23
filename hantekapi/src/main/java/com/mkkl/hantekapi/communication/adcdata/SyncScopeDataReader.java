package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.communication.controlcmd.HantekRequest;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;
import com.mkkl.hantekapi.exceptions.UncheckedUsbException;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.function.Consumer;

/**
 * Class used for reading data from usb endpoints.
 * Remember to call {@link SyncScopeDataReader#startCapture()} before capturing data to trigger oscilloscope's capture mode
 * and {@link SyncScopeDataReader#stopCapture()} when data capturing is finished, to let it rest.
 *
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
        if(!capture) startCapture();
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