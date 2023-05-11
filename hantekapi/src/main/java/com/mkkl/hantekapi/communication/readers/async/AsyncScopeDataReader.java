package com.mkkl.hantekapi.communication.readers.async;

import com.mkkl.hantekapi.LibUsbInstance;
import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.OscilloscopeHandle;
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
    protected final EventHandlingThread eventHandlingThread;
    protected final TransferProcessorThread transferProcessorThread;
    protected final BlockingQueue<Transfer> transferQueue;
    protected final List<UsbDataListener> listenerList = new ArrayList<UsbDataListener>();
    protected final TransferCallback transferCallback;

    public AsyncScopeDataReader(OscilloscopeHandle oscilloscopeHandle, int outstandingPackets) {
        super(oscilloscopeHandle);
        transferQueue = new LinkedBlockingQueue<>();

        eventHandlingThread = new EventHandlingThread(LibUsbInstance.getContext());
        eventHandlingThread.start();

        transferProcessorThread = new TransferProcessorThread(endpoint, transferQueue, outstandingPackets);
        transferProcessorThread.start();

        transferCallback = this::initializeCallback;
    }

    protected synchronized void initializeCallback(Transfer transfer) {
        transferProcessorThread.notifyReceivedPacket();
        for(UsbDataListener usbDataListener : listenerList)
            usbDataListener.processTransfer(transfer);
        LibUsb.freeTransfer(transfer);
    }

    public AsyncScopeDataReader(OscilloscopeHandle oscilloscopeHandle) {
        this(oscilloscopeHandle, 3);
    }

    public synchronized void registerListener(UsbDataListener usbDataListener) {
        listenerList.add(usbDataListener);
    }

    public synchronized void unregisterListener(UsbDataListener usbDataListener) {
        listenerList.remove(usbDataListener);
    }

    public void read() throws InterruptedException {
        read(defaultSize);
    }

    public void read(short size) throws InterruptedException {
        oscilloscopeHandle.ensureCaptureStarted();
        transferQueue.put(endpoint.getTransfer(size, transferCallback));
    }

    public void read(ByteBuffer byteBuffer) throws InterruptedException {
        oscilloscopeHandle.ensureCaptureStarted();
        transferQueue.put(endpoint.getTransfer(byteBuffer, transferCallback));
    }

    public int getQueueSize() {
        return transferQueue.size();
    }

    public void waitToFinish() throws InterruptedException {
        transferProcessorThread.finish();
        transferProcessorThread.join();

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

        transferProcessorThread.abort();
        try {
            transferProcessorThread.join();
        } catch (InterruptedException e) {
            throw new IOException("Failed to join DataRequestProcessorThread, action was interrupted");
        }
    }
}

