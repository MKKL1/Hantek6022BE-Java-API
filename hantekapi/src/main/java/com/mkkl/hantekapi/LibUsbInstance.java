package com.mkkl.hantekapi;

import org.usb4java.Context;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

public class LibUsbInstance {
    private static Context context;
    private static boolean initialized = false;

    public static void init() {
        context = new Context();
        int result = LibUsb.init(context);
        if(result < 0) throw new LibUsbException("Unable to initialize libusb", result);
        initialized = true;
    }

    public static void exit() {
        LibUsb.exit(context);
        initialized = false;
    }

    public static Context getContext() {
        if(!initialized) init();
        return context;
    }
}
