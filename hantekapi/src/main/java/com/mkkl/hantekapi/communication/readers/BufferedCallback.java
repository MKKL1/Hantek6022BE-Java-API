package com.mkkl.hantekapi.communication.readers;

import org.usb4java.LibUsb;
import org.usb4java.Transfer;

import java.nio.ByteBuffer;

public abstract class BufferedCallback implements UsbDataListener {

    @Override
    public void processTransfer(Transfer transfer) {
        onDataReceived(transfer.buffer());
    }

    public abstract void onDataReceived(ByteBuffer byteBuffer);
}
