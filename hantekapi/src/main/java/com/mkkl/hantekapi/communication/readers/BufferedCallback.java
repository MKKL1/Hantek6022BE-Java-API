package com.mkkl.hantekapi.communication.readers;

import org.usb4java.LibUsb;
import org.usb4java.Transfer;

import java.nio.ByteBuffer;

public abstract class BufferedCallback implements UsbDataListener {
    private boolean freeTransfer = true;
    public BufferedCallback(boolean freeTransfer) {
        this.freeTransfer = freeTransfer;
    }

    public BufferedCallback() {
    }

    @Override
    public void processTransfer(Transfer transfer) {
        onDataReceived(transfer.buffer());
        if(freeTransfer) LibUsb.freeTransfer(transfer);
    }

    public abstract void onDataReceived(ByteBuffer byteBuffer);
}
