package com.mkkl.hantekapi.communication.readers;

import org.usb4java.LibUsb;
import org.usb4java.Transfer;

import java.nio.ByteBuffer;

public abstract class ByteArrayCallback implements UsbDataListener {
    private boolean freeTransfer = true;
    public ByteArrayCallback(boolean freeTransfer) {
        this.freeTransfer = freeTransfer;
    }

    public ByteArrayCallback() {
    }

    @Override
    public void processTransfer(Transfer transfer) {
        ByteBuffer byteBuffer = transfer.buffer();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes, 0, bytes.length);
        onDataReceived(bytes);
        if(freeTransfer) LibUsb.freeTransfer(transfer);
    }

    public abstract void onDataReceived(byte[] bytes);
}
