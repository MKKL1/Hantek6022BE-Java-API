package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.communication.controlcmd.HantekRequest;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;
import com.mkkl.hantekapi.exceptions.UncheckedUsbException;

import javax.usb.UsbException;
import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Class used for reading data from usb endpoints.
 * Synchronous, as well as asychnchronous solution is
 * provided with {@link ScopeDataReader#syncRead(short)} and {@link ScopeDataReader#asyncRead(short, Consumer)} respectively.
 * Remember to call {@link ScopeDataReader#startCapture()} before capturing data to trigger oscilloscope's capture mode
 * and {@link ScopeDataReader#stopCapture()} when data capturing is finished, to let it rest.
 *
 */
public class ScopeDataReader implements AutoCloseable, AdcDataReader{

    private final Oscilloscope oscilloscope;
    private boolean capture = false;
    private final Endpoint endpoint;

    public ScopeDataReader(Oscilloscope oscilloscope) throws UsbException {
        this.oscilloscope = oscilloscope;
        endpoint = oscilloscope.getScopeInterface().getEndpoint();
        endpoint.openPipe();
    }

    /**
     * Makes control request to device to tell it to start capturing samples.
     * Use before reading data from ADC.
     */
    public void startCapture() {
        oscilloscope.patch(HantekRequest.getStartRequest())
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to start capture", ex))
                .onSuccess(() -> capture = true);
    }

    /**
     * Makes control request to device to tell it to stop capturing samples.
     * Supported only by custom openhantek firmware.
     * @see Oscilloscope#flash_firmware()
     */
    public void stopCapture() {
        oscilloscope.patch(HantekRequest.getStopRequest())
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to stop capture", ex))
                .onSuccess(() -> capture = false);
    }

    /**
     * Synchronous reading from oscilloscope's ADC.
     * @param size The number of data points for both channels to retrieve. size/2 samples per channel.
     * @return raw ADC data. Use {@link AdcInputStream} to format this output
     */
    public byte[] syncRead(short size) throws IOException, UsbException {
        if(!capture) startCapture();
        return endpoint.syncReadPipe(size);
    }

    //TODO process events in queue to ensure data correct order
    /**
     * Asynchronous reading from oscilloscope's ADC.
     * @param size The number of data points for both channels to retrieve. size/2 samples per channel.
     * @param packetConsumer consumer of packet data
     * @return CompletableFuture of finished reading data
     */
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
