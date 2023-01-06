package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.communication.controlcmd.HantekRequest;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;
import com.mkkl.hantekapi.exceptions.UncheckedUsbException;
import org.apache.commons.io.input.QueueInputStream;
import org.apache.commons.io.output.QueueOutputStream;

import javax.usb.UsbException;
import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ScopeDataReader implements AutoCloseable{

    private final Oscilloscope oscilloscope;
    private boolean capture = false;
    private final Endpoint endpoint;

    public ScopeDataReader(Oscilloscope oscilloscope) throws UsbException, IOException {
        this.oscilloscope = oscilloscope;
        endpoint = oscilloscope.getScopeInterface().getEndpoint();
        endpoint.openPipe();
    }

    public void startCapture() {
        oscilloscope.patch(HantekRequest.getStartRequest())
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to start capture", ex))
                .onSuccess(() -> capture = true);
    }

    public void stopCapture() {
        oscilloscope.patch(HantekRequest.getStopRequest())
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to stop capture", ex))
                .onSuccess(() -> capture = false);
    }

    public byte[] syncRead(short size) throws IOException, UsbException {
        if(!capture) startCapture();
        return endpoint.syncReadPipe(size);
    }

    public CompletableFuture<Void> asyncRead(short size, Consumer<byte[]> packetConsumer) throws IOException, UsbException {
        CompletableFuture<Void> finishFuture = new CompletableFuture<>();
        endpoint.asyncReadPipe(size, new AdcDataListener() {
            @Override
            public void onDataReceived(byte[] data) {
                packetConsumer.accept(data);
            }

            @Override
            public void onCompleted(int finalSize) {
                if (finalSize == size)
                    finishFuture.complete(null);
                else finishFuture.completeExceptionally(new UsbException("Received data length was too short (Expected " + size + " bytes, received " + finalSize));
            }

            @Override
            public void onError(UsbException e) {
                finishFuture.completeExceptionally(e);
            }
        });
        return finishFuture;
    }

    @Override
    public void close() throws Exception {
        endpoint.close();
    }
}
