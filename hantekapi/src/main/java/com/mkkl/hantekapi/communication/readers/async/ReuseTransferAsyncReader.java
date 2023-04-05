package com.mkkl.hantekapi.communication.readers.async;

import com.mkkl.hantekapi.Oscilloscope;
import org.usb4java.Transfer;

import java.nio.ByteBuffer;

/**
 * Oscilloscope sample data reader that extends {@link AsyncScopeDataReader}.
 * It's main purpose is to reuse transfers to not have to initialize and free them,
 * saving a lot of processing power in high-speed applications
 * while sacrificing some memory for buffer overhead.
 * Main downside is that you cannot change size of data to be read after initialization.
 */
public class ReuseTransferAsyncReader extends AsyncScopeDataReader{
    private final ByteBuffer[] availableBuffers;
    private final Transfer[] availableTransfers;
    private final int savedLength;
    private int position = 0;

    public ReuseTransferAsyncReader(Oscilloscope oscilloscope, int bufferSize, int savedTransfers, int outstandingTransfers) {
        super(oscilloscope, outstandingTransfers);
        this.savedLength = savedTransfers;
        availableBuffers = new ByteBuffer[savedTransfers];
        availableTransfers = new Transfer[savedTransfers];
        for(int i = 0; i < savedTransfers; i++) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
            availableBuffers[i] = buffer;
            availableTransfers[i] = endpoint.getTransfer(buffer, transferCallback);
        }
    }

    @Override
    public void read() throws InterruptedException {
        oscilloscope.ensureCaptureStarted();
        transferQueue.put(getNextTransfer());
    }

    /**
     * Used internally for accessing next, ideally not used, transfer in array.
     * @return Transfer in {@link #availableTransfers} of {@link #position}+1
     */
    public synchronized Transfer getNextTransfer() {
        if(position >= savedLength) resetPosition();
        availableBuffers[position].clear();
        Transfer transfer = availableTransfers[position];
        position++;
        return transfer;
    }

    public synchronized void resetPosition() {
        position = 0;
    }

}
