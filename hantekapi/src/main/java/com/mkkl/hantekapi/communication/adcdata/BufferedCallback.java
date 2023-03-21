package com.mkkl.hantekapi.communication.adcdata;

import org.usb4java.Transfer;
import org.usb4java.TransferCallback;

import java.nio.ByteBuffer;

public abstract class BufferedCallback implements TransferCallback {
    @Override
    public void processTransfer(Transfer transfer) {
        onDataReceived(transfer.buffer(), transfer.actualLength());
    }

    public abstract void onDataReceived(ByteBuffer byteBuffer, int actualLength);
}
