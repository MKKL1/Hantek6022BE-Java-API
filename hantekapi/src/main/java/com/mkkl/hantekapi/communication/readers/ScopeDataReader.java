package com.mkkl.hantekapi.communication.readers;

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

    public Endpoint getEndpoint() {
        return endpoint;
    }
}
