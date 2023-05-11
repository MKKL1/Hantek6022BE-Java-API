package com.mkkl.hantekapi.communication.readers;

import org.usb4java.LibUsb;
import org.usb4java.Transfer;

import java.nio.ByteBuffer;

public abstract class ByteArrayCallback implements UsbDataListener {

    @Override
    public void processTransfer(Transfer transfer) {
        ByteBuffer byteBuffer = transfer.buffer();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes, 0, bytes.length);
        onDataReceived(bytes);
    }

    public abstract void onDataReceived(byte[] bytes);
}
