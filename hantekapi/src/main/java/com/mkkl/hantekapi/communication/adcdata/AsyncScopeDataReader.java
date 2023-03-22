package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.LibUsbInstance;
import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;
import org.usb4java.*;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class AsyncScopeDataReader extends ScopeDataReader {
    private final EventHandlingThread eventHandlingThread;
    private final DataRequestProcessorThread dataRequestProcessorThread;
    private final BlockingQueue<DataRequest> dataRequestQueue;

    public AsyncScopeDataReader(Oscilloscope oscilloscope, int outstandingPackets) {
        super(oscilloscope);
        dataRequestQueue = new ArrayBlockingQueue<>(outstandingPackets);
        eventHandlingThread = new EventHandlingThread(LibUsbInstance.getContext());
        eventHandlingThread.start();
        dataRequestProcessorThread = new DataRequestProcessorThread(endpoint, dataRequestQueue);
        dataRequestProcessorThread.start();
    }

    public AsyncScopeDataReader(Oscilloscope oscilloscope) {
        this(oscilloscope, 3);
    }


    //TODO implement outstanding requests
    public void read(TransferCallback transferCallback) throws InterruptedException {
        read(defaultSize, transferCallback);
    }

    public void read(short size, TransferCallback transferCallback) throws InterruptedException {
        dataRequestQueue.offer(new DataRequest(size, transferCallback), 25000, TimeUnit.MILLISECONDS);
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

    public DataRequestProcessorThread(Endpoint endpoint, BlockingQueue<DataRequest> dataRequestQueue) {
        this.endpoint = endpoint;
        this.dataRequestQueue = dataRequestQueue;
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
                if(finish && dataRequestQueue.isEmpty()) interrupt();
                DataRequest dataRequest = dataRequestQueue.take();
                endpoint.asyncReadPipe(dataRequest.size(), dataRequest.transferCallback());
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