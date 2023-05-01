package com.mkkl.hantekapi.communication.readers;

import com.mkkl.hantekapi.OscilloscopeHandle;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;

import java.io.Closeable;

public abstract class ScopeDataReader implements Closeable {
    protected final OscilloscopeHandle oscilloscopeHandle;
    protected boolean capture = false;
    protected final Endpoint endpoint;
    protected final short defaultSize;

    public ScopeDataReader(OscilloscopeHandle oscilloscopeHandle) {
        this.oscilloscopeHandle = oscilloscopeHandle;
        endpoint = oscilloscopeHandle.getScopeInterface().getEndpoint();
        defaultSize = endpoint.getPacketSize();
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }
}
