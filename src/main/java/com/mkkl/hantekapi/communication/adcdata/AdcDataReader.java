package com.mkkl.hantekapi.communication.adcdata;

import javax.usb.UsbException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface AdcDataReader {
    void startCapture();
    void stopCapture();
    byte[] syncRead(short size) throws IOException, UsbException;
    CompletableFuture<Void> asyncRead(short size, Consumer<byte[]> packetConsumer) throws IOException, UsbException;
}
