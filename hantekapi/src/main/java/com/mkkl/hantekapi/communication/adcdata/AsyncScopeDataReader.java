package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.LibUsbInstance;
import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;
import org.usb4java.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class AsyncScopeDataReader extends ScopeDataReader {
    private final EventHandlingThread eventHandlingThread;
    private final DataRequestProcessorThread dataRequestProcessorThread;
    private final BlockingQueue<DataRequest> dataRequestQueue;
    private final List<UsbDataListener> listenerList = new ArrayList<UsbDataListener>();
    private final TransferCallback transferCallback;

    public AsyncScopeDataReader(Oscilloscope oscilloscope, int outstandingPackets) {
        super(oscilloscope);
        dataRequestQueue = new LinkedBlockingQueue<>();
        eventHandlingThread = new EventHandlingThread(LibUsbInstance.getContext());
        eventHandlingThread.start();
        dataRequestProcessorThread = new DataRequestProcessorThread(endpoint, dataRequestQueue, outstandingPackets);
        dataRequestProcessorThread.start();

        transferCallback = transfer -> {
            dataRequestProcessorThread.notifyReceivedPacket();
            for(UsbDataListener usbDataListener : listenerList)
                usbDataListener.processTransfer(transfer);
        };
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

    //TODO implement outstanding requests
    public void read() throws InterruptedException {
        read(defaultSize);
    }

    public void read(short size) throws InterruptedException {
        oscilloscope.ensureCaptureStarted();
        dataRequestQueue.put(new DataRequest(size, transferCallback));
    }

    public int getQueueSize() {
        return dataRequestQueue.size();
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

class DataRequestProcessorThread extends Thread {
    private volatile boolean abort = false;
    private volatile boolean finish = false;
    private final Endpoint endpoint;
    private final BlockingQueue<DataRequest> dataRequestQueue;
    private final int outstandingTransfers;
    private final AtomicInteger transfersInKernel = new AtomicInteger(0);

    public DataRequestProcessorThread(Endpoint endpoint, BlockingQueue<DataRequest> dataRequestQueue, int outstandingTransfers) {
        this.endpoint = endpoint;
        this.dataRequestQueue = dataRequestQueue;
        this.outstandingTransfers = outstandingTransfers;
    }

    public synchronized void notifyReceivedPacket() {
        transfersInKernel.decrementAndGet();
        notifyAll();
    }

    /**
     * Aborts the event handling thread.
     */
    public void abort()
    {
        this.abort = true;
    }

    public void finish() {
        if(dataRequestQueue.isEmpty()) interrupt();
        this.finish = true;
    }

    @Override
    public void run()
    {
        while (!Thread.currentThread().isInterrupted() && !abort)
        {
            try {
                //Waiting for transfers in kernel to be smaller than required value
                while(transfersInKernel.get() >= outstandingTransfers) {
                    synchronized (this) {
                        wait();
                    }
                }

                if(finish && dataRequestQueue.isEmpty()) {
                    while(transfersInKernel.get() > 0) {
                        synchronized (this) {
                            wait();
                        }
                    }
                    interrupt();
                }

                DataRequest dataRequest = dataRequestQueue.take();
                //Submitting transfer
                endpoint.asyncReadPipe(dataRequest.size(), dataRequest.transferCallback());
                //Incrementing transfers in kernel local value
                transfersInKernel.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        }
    }
}

record DataRequest(short size, TransferCallback transferCallback) {

}

class EventHandlingThread extends Thread {
    /** If thread should abort. */
    private volatile boolean abort = false;
    private final Context context;

    public EventHandlingThread(Context context) {
        this.context = context;
    }

    /**
     * Aborts the event handling thread.
     */
    public void abort()
    {
        this.abort = true;
    }

    @Override
    public void run()
    {
        while (!abort)
        {
            int result = LibUsb.handleEventsTimeout(context, 250000);
            //TODO implement stopping mechanism if too many events failed to be processed
            if (result < 0)
                throw new LibUsbException("Unable to handle events", result);
        }
    }
}