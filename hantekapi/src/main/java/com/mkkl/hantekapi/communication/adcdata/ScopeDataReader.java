package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.communication.controlcmd.HantekRequest;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;
import com.mkkl.hantekapi.exceptions.UncheckedUsbException;

import java.io.Closeable;
import java.io.IOException;

public abstract class ScopeDataReader implements Closeable {
    protected final Oscilloscope oscilloscope;
    protected boolean capture = false;
    protected final Endpoint endpoint;
    protected final short defaultSize;

    public ScopeDataReader(Oscilloscope oscilloscope) {
        this.oscilloscope = oscilloscope;
        endpoint = oscilloscope.getScopeInterface().getEndpoint();
        defaultSize = endpoint.getPacketSize();
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

}
