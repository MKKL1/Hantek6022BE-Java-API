package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.communication.controlcmd.HantekRequest;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;
import com.mkkl.hantekapi.exceptions.UncheckedUsbException;
import org.apache.commons.io.input.QueueInputStream;
import org.apache.commons.io.output.QueueOutputStream;

import javax.usb.UsbException;
import java.io.IOException;
import java.io.InputStream;

public class ScopeDataReader implements AutoCloseable{

    private final Oscilloscope oscilloscope;
    private boolean capture = false;
    private final QueueOutputStream queueOutputStream;
    private final QueueInputStream queueInputStream;
    private final Endpoint endpoint;

    public ScopeDataReader(Oscilloscope oscilloscope) throws UsbException {
        this.oscilloscope = oscilloscope;
        queueOutputStream = new QueueOutputStream();
        queueInputStream = queueOutputStream.newQueueInputStream();
        endpoint = oscilloscope.getScopeInterface().getEndpoint();
        endpoint.openPipe();
    }

    public void startCapture() {
        oscilloscope.patch(HantekRequest.getStartRequest())
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to start capture", ex))
                .onSuccess((v) -> capture = true);
    }

    public void stopCapture() {
        oscilloscope.patch(HantekRequest.getStopRequest())
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to stop capture", ex))
                .onSuccess((v) -> capture = false);
    }

    public void syncRead(short size) throws IOException, UsbException {
        endpoint.syncReadPipe(queueOutputStream, size);
    }

    //TODO return 'finished reading' event
    public void asyncRead(short size) throws IOException, UsbException {
        endpoint.asyncReadPipe(queueOutputStream, size);
    }

    public InputStream getInputStream() {
        return queueInputStream;
    }

    @Override
    public void close() throws Exception {
        endpoint.close();
        queueInputStream.close();
        queueOutputStream.close();
    }
}
