package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.communication.controlcmd.HantekRequest;
import com.mkkl.hantekapi.communication.interfaces.ScopeInterface;
import com.mkkl.hantekapi.communication.interfaces.SupportedInterfaces;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import java.io.IOException;
import java.io.PipedOutputStream;

public class ScopeDataReader implements AutoCloseable{

    private final Oscilloscope oscilloscope;
    private final UsbDevice scopeDevice;
    private final ScopeInterface scopeInterface;
    private boolean capture = false;
    private boolean skipFirstPacket = true;
    private boolean skipNextPacket = true;

    private final AdcInputStream adcInputStream;
    private final PipedOutputStream pipedOutputStream;

    public ScopeDataReader(Oscilloscope oscilloscope, SupportedInterfaces supportedInterfaces) throws UsbException, IOException {
        this.oscilloscope = oscilloscope;
        scopeDevice = oscilloscope.getScopeDevice();
        scopeInterface = new ScopeInterface(scopeDevice);
        scopeInterface.setInterface(supportedInterfaces);
        scopeInterface.claim();

        pipedOutputStream = new PipedOutputStream();
        adcInputStream = new AdcInputStream(pipedOutputStream,
                scopeInterface.getEndpoint().getPacketSize(),
                oscilloscope.getChannelManager().getChannelCount());
    }

    public ScopeDataReader(Oscilloscope oscilloscope) throws UsbException, IOException {
        this(oscilloscope, SupportedInterfaces.BulkTransfer);
    }

    public AdcInputStream getAdcInputStream() {
        return adcInputStream;
    }

    @Override
    public void close() throws UsbException, IOException {
        if(capture) stopCapture();
        scopeInterface.close();
        adcInputStream.close();
        pipedOutputStream.close();
    }


    public void startCapture() throws UsbException {
        capture = true;
        if(skipFirstPacket) skipNextPacket = true;
        HantekRequest.getStartRequest().write(scopeDevice);
    }

    public void stopCapture() throws UsbException {
        capture = false;
        HantekRequest.getStopRequest().write(scopeDevice);
    }

    public boolean isCapturing() {
        return capture;
    }

    public void shouldSkipFirstPacket(boolean skipFirstPacket) {
        this.skipFirstPacket = skipFirstPacket;
        this.skipNextPacket = skipFirstPacket && skipNextPacket;
    }

    private void syncRead(short size) throws IOException, UsbException {
        if(skipNextPacket)
            size += scopeInterface.getEndpoint().getPacketSize();

        scopeInterface.getEndpoint().syncReadPipe(pipedOutputStream, size);

        if(skipNextPacket) {
            adcInputStream.skipPacket();
            skipNextPacket = false;
        }
    }

    private void asyncRead(short size) throws IOException, UsbException {
        if(skipNextPacket)
            size += scopeInterface.getEndpoint().getPacketSize();

        scopeInterface.getEndpoint().asyncReadPipe(pipedOutputStream, size);

        if(skipNextPacket) {
            adcInputStream.skipPacket();
            skipNextPacket = false;
        }
    }

    public void readDataFrame() throws UsbException, IOException {
        readDataFrame((short) 0x400);
    }

    public void readDataFrame(short size) throws UsbException, IOException {
        if(oscilloscope.getChannelManager().getActiveChannelCount() == 0) oscilloscope.setActiveChannels(ActiveChannels.CH1CH2);
        if(!capture) startCapture();
        syncRead(size);
    }

    public void asyncReadDataFrame() throws UsbException, IOException {
        readDataFrame((short) 0x400);
    }

    public void asyncReadDataFrame(short size) throws UsbException, IOException {
        if(oscilloscope.getChannelManager().getActiveChannelCount() == 0) oscilloscope.setActiveChannels(ActiveChannels.CH1CH2);
        if(!capture) startCapture();
        asyncRead(size);
    }


}
