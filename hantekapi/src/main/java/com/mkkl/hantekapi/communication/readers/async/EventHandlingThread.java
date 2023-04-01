package com.mkkl.hantekapi.communication.readers.async;

import org.usb4java.Context;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

class EventHandlingThread extends Thread {
    /**
     * If thread should abort.
     */
    private volatile boolean abort = false;
    private final Context context;

    public EventHandlingThread(Context context) {
        this.context = context;
    }

    /**
     * Aborts the event handling thread.
     */
    public void abort() {
        this.abort = true;
    }

    @Override
    public void run() {
        while (!abort) {
            int result = LibUsb.handleEventsTimeout(context, 250000);
            //TODO implement stopping mechanism if too many events failed to be processed
            if (result < 0)
                throw new LibUsbException("Unable to handle events", result);
        }
    }
}
