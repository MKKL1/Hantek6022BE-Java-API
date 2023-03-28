package com.mkkl.hantekapi.communication.adcdata;

import org.usb4java.Transfer;

public interface UsbDataListener {
    void processTransfer(Transfer transfer);
}
