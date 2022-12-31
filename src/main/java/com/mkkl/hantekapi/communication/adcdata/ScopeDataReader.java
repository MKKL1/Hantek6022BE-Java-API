package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.communication.adcdata.AdcInputStream;
import com.mkkl.hantekapi.communication.adcdata.FormattedDataStream;
import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.communication.controlcmd.ScopeControlRequest;
import com.mkkl.hantekapi.communication.interfaces.ScopeInterface;
import com.mkkl.hantekapi.communication.interfaces.SupportedInterfaces;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import java.io.IOException;

public class ScopeDataReader implements AutoCloseable{

    private final Oscilloscope oscilloscope;
    private final UsbDevice scopeDevice;
    private final ScopeInterface scopeInterface;
    private boolean capture = false;

    public ScopeDataReader(Oscilloscope oscilloscope, SupportedInterfaces supportedInterfaces) throws UsbException {
        this.oscilloscope = oscilloscope;
        scopeDevice = oscilloscope.getScopeDevice();
        scopeInterface = new ScopeInterface(scopeDevice);
        scopeInterface.setInterface(supportedInterfaces);
        scopeInterface.claim();
    }

    public ScopeDataReader(Oscilloscope oscilloscope) throws UsbException {
        this(oscilloscope, SupportedInterfaces.BulkTransfer);
    }

    @Override
    public void close() throws UsbException {
        if(capture) startCapture();
        //Shouldn't UsbException be derived from IOException?
        scopeInterface.close();
    }


    public void startCapture() throws UsbException {
        capture = true;
        ScopeControlRequest.getStartRequest().write(scopeDevice);
    }

    public void stopCapture() throws UsbException {
        capture = false;
        ScopeControlRequest.getStartRequest().write(scopeDevice);
    }

    public boolean isCapturing() {
        return capture;
    }

    public AdcInputStream readDataFrame() throws UsbException, IOException {
        return readDataFrame((short) 0x400);
    }

    public AdcInputStream readDataFrame(short size) throws UsbException, IOException {
        if(oscilloscope.getChannelManager().getActiveChannelCount() == 0) oscilloscope.setActiveChannels(ActiveChannels.CH1CH2);
        startCapture();
        AdcInputStream adcInputStream = new AdcInputStream(
                scopeInterface.getEndpoint().syncReadPipe(size),
                oscilloscope.getChannelManager().getChannelCount(),
                size);
        stopCapture();
        return adcInputStream;
    }

    public FormattedDataStream readFormattedDataFrame() throws UsbException, IOException {
        return readFormattedDataFrame((short) 0x400);
    }

    public FormattedDataStream readFormattedDataFrame(short size) throws UsbException, IOException {
        if(oscilloscope.getChannelManager().getActiveChannelCount() == 0) oscilloscope.setActiveChannels(ActiveChannels.CH1CH2);
        startCapture();
        FormattedDataStream formattedDataStream = new FormattedDataStream(
                scopeInterface.getEndpoint().syncReadPipe(size),
                oscilloscope.getChannelManager(),
                size);
        stopCapture();
        return formattedDataStream;
    }
}
