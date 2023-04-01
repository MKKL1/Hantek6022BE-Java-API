package com.mkkl.hantekapi.communication.readers;

import org.usb4java.Transfer;

public interface UsbDataListener {
    void processTransfer(Transfer transfer);
}
