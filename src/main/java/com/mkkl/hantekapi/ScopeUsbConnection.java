package com.mkkl.hantekapi;

import javax.usb.*;

public class ScopeUsbConnection {
    private final UsbDevice scopeDevice;
    private final EepromConnection eepromConnection;

    public ScopeUsbConnection(UsbDevice usbDevice) {
        this.scopeDevice = usbDevice;
        eepromConnection = new EepromConnection(scopeDevice);
    }


    public UsbDevice getScopeDevice() {
        return scopeDevice;
    }

    public EepromConnection getEepromConnection() {
        return eepromConnection;
    }
}
