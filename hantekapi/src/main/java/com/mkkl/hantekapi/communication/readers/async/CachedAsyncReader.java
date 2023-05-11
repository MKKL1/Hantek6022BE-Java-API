package com.mkkl.hantekapi.communication.readers.async;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.OscilloscopeHandle;
import com.mkkl.hantekapi.communication.readers.UsbDataListener;
import org.usb4java.LibUsb;
import org.usb4java.Transfer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Oscilloscope sample data reader that extends {@link AsyncScopeDataReader}.
 * It's main purpose is to reuse transfers to not have to initialize and free them,
 * saving a lot of processing power in high-speed applications
 * while sacrificing some memory for buffer overhead.
 * Main downside is that you cannot change size of data to be read after initialization.
 */
public class CachedAsyncReader extends AsyncScopeDataReader{
    private final ByteBuffer[] cachedBuffers;
    private final Transfer[] cachedTransfers;
    private final int savedLength;
    private int position = 0;

    public CachedAsyncReader(OscilloscopeHandle oscilloscopeHandle, int bufferSize, int cachedTransferCount, int outstandingTransfers) {
        super(oscilloscopeHandle, outstandingTransfers);
        this.savedLength = cachedTransferCount;
        cachedBuffers = new ByteBuffer[cachedTransferCount];
        cachedTransfers = new Transfer[cachedTransferCount];
        for(int i = 0; i < cachedTransferCount; i++) {
            ByteBuffer buffer = ByteBuffer.allocateDirect(bufferSize);
            cachedBuffers[i] = buffer;
            cachedTransfers[i] = endpoint.getTransfer(buffer, transferCallback);
        }
    }

    @Override
    protected synchronized void initializeCallback(Transfer transfer) {
        transferProcessorThread.notifyReceivedPacket();
        transfer.buffer().clear();
        for(UsbDataListener usbDataListener : listenerList)
            usbDataListener.processTransfer(transfer);
    }

    //TODO this method should wait so that buffer is not used in different read request
    @Override
    public void read() throws InterruptedException {
        oscilloscopeHandle.ensureCaptureStarted();
        transferQueue.put(getNextTransfer());
    }

    /**
     * Used internally for accessing next, ideally not used, transfer in array.
     * @return Transfer in {@link #cachedTransfers} of {@link #position}+1
     */
    public synchronized Transfer getNextTransfer() {
        if(position >= savedLength) resetPosition();
        Transfer transfer = cachedTransfers[position];
        position++;
        return transfer;
    }

    public synchronized void resetPosition() {
        position = 0;
    }

//    @Override
//    public void close() throws IOException {
//        super.close();
//        for(int i = 0; i < savedLength; i++) {
//            cachedBuffers[i] = null;
//            if(cachedTransfers[i] != null) LibUsb.freeTransfer(cachedTransfers[i]);
//        }
//    }
}
