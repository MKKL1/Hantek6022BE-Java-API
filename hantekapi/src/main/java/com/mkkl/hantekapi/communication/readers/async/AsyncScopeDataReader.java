package com.mkkl.hantekapi.communication.readers.async;

import com.mkkl.hantekapi.LibUsbInstance;
import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.communication.readers.ScopeDataReader;
import com.mkkl.hantekapi.communication.readers.UsbDataListener;
import org.usb4java.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AsyncScopeDataReader extends ScopeDataReader {
    private final EventHandlingThread eventHandlingThread;
    private final DataRequestProcessorThread dataRequestProcessorThread;
    protected final BlockingQueue<Transfer> transferQueue;
    protected final List<UsbDataListener> listenerList = new ArrayList<UsbDataListener>();
    protected final TransferCallback transferCallback;

    public AsyncScopeDataReader(Oscilloscope oscilloscope, int outstandingPackets) {
        super(oscilloscope);
        transferQueue = new LinkedBlockingQueue<>();

        eventHandlingThread = new EventHandlingThread(LibUsbInstance.getContext());
        eventHandlingThread.start();

        dataRequestProcessorThread = new DataRequestProcessorThread(endpoint, transferQueue, outstandingPackets);
        dataRequestProcessorThread.start();

        transferCallback = this::initializeCallback;
    }

    protected void initializeCallback(Transfer transfer) {
        dataRequestProcessorThread.notifyReceivedPacket();
        for(UsbDataListener usbDataListener : listenerList)
            usbDataListener.processTransfer(transfer);
    }

    public AsyncScopeDataReader(Oscilloscope oscilloscope) {
        this(oscilloscope, 3);
    }

    public void registerListener(UsbDataListener usbDataListener) {
        listenerList.add(usbDataListener);
    }

    public void unregisterListener(UsbDataListener usbDataListener) {
        listenerList.remove(usbDataListener);
    }

    public void read() throws InterruptedException {
        read(defaultSize);
    }

    public void read(short size) throws InterruptedException {
        oscilloscope.ensureCaptureStarted();
        transferQueue.put(endpoint.getTransfer(size, transferCallback));
    }

    public void read(ByteBuffer byteBuffer) throws InterruptedException {
        oscilloscope.ensureCaptureStarted();
        transferQueue.put(endpoint.getTransfer(byteBuffer, transferCallback));
    }

    public int getQueueSize() {
        return transferQueue.size();
    }

    public void waitToFinish() throws InterruptedException {
        dataRequestProcessorThread.finish();
        dataRequestProcessorThread.join();

        eventHandlingThread.abort();
        eventHandlingThread.join();
    }

    @Override
    public void close() throws IOException {
        eventHandlingThread.abort();
        try {
            eventHandlingThread.join();
        } catch (InterruptedException e) {
            throw new IOException("Failed to join EventHandlingThread, action was interrupted");
        }

        dataRequestProcessorThread.abort();
        try {
            dataRequestProcessorThread.join();
        } catch (InterruptedException e) {
            throw new IOException("Failed to join DataRequestProcessorThread, action was interrupted");
        }
    }
}

