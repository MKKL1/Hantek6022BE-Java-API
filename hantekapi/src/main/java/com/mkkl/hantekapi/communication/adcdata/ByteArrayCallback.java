package com.mkkl.hantekapi.communication.adcdata;

import org.usb4java.LibUsb;
import org.usb4java.Transfer;
import org.usb4java.TransferCallback;

import java.nio.ByteBuffer;

public abstract class ByteArrayCallback implements UsbDataListener {
    @Override
    public void processTransfer(Transfer transfer) {
        ByteBuffer byteBuffer = transfer.buffer();
        byte[] bytes = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytes, 0, bytes.length);
        onDataReceived(bytes);
        byteBuffer.clear();
        LibUsb.freeTransfer(transfer);
    }

    public abstract void onDataReceived(byte[] bytes);
}
