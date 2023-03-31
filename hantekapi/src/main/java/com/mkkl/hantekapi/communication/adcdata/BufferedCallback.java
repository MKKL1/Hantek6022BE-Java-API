package com.mkkl.hantekapi.communication.adcdata;

import org.usb4java.LibUsb;
import org.usb4java.Transfer;
import org.usb4java.TransferCallback;

import java.nio.ByteBuffer;

public abstract class BufferedCallback implements UsbDataListener {
    @Override
    public void processTransfer(Transfer transfer) {
        onDataReceived(transfer.buffer());
        LibUsb.freeTransfer(transfer);
    }

    public abstract void onDataReceived(ByteBuffer byteBuffer);
}
