package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;
import org.usb4java.*;

import java.io.Closeable;
import java.io.IOException;

public class AsyncScopeDataReader extends ScopeDataReader {
    private final EventHandlingThread eventHandlingThread;

    public AsyncScopeDataReader(Oscilloscope oscilloscope) {
        super(oscilloscope);
        eventHandlingThread = new EventHandlingThread(oscilloscope.getUsbContext());
        eventHandlingThread.start();
    }

    //TODO implement outstanding requests
    public void read(TransferCallback transferCallback) {
        read(defaultSize, transferCallback);
    }

    public void read(short size, TransferCallback transferCallback) {
        endpoint.asyncReadPipe(size, transferCallback);
    }

    @Override
    public void close() throws IOException {
        eventHandlingThread.abort();
        try {
            eventHandlingThread.join();
        } catch (InterruptedException e) {
            throw new IOException("Failed to join EventHandlingThread, action was interrupted");
        }
    }
}

class EventHandlingThread extends Thread
{
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
            if (result != LibUsb.SUCCESS)
                throw new LibUsbException("Unable to handle events", result);
        }
    }
}