package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.ScopeChannel;
import com.mkkl.hantekapi.controlrequest.ControlRequest;
import com.mkkl.hantekapi.controlrequest.ScopeControlRequest;
import com.mkkl.hantekapi.endpoints.ScopeInterfaces;
import com.mkkl.hantekapi.firmware.FirmwareUploader;

import javax.usb.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.util.Arrays;

public class Oscilloscope implements AutoCloseable{
    private final HantekConnection hantekConnection;
    private final ChannelManager channelManager;
    private boolean firmwarePresent = false;

    public Oscilloscope(UsbDevice usbDevice) {
        this.hantekConnection = new HantekConnection(usbDevice);
        this.channelManager = new ChannelManager((byte) 2);
    }

    public Oscilloscope(UsbDevice usbDevice, boolean firmwarePresent){
        this(usbDevice);
        this.firmwarePresent = firmwarePresent;
    }

    public void open() throws UsbException {
        open(ScopeInterfaces.BulkTransfer);
    }

    public void open(ScopeInterfaces scopeInterfaces) throws UsbException {
        hantekConnection.setInterface(scopeInterfaces);
        hantekConnection.open();
    }

    @Override
    public void close() throws UsbException {
        hantekConnection.close();
    }

    public byte[] getCalibrationValues() throws UsbException {
        byte[] standardcalibration = hantekConnection.getStandardCalibration();
        byte[] extendedcalibration = hantekConnection.getExtendedCalibration();

        for(int j = 0; j < channelManager.getChannelCount(); j++) {
            float[] calibration = new float[ScopeChannel.voltageRanges.length];
            for (int i = 0; i < 4; i++) {
                calibration[i] = standardcalibration[ScopeChannel.calibrationOffsets[i]+j]-128;
                byte extcal = extendedcalibration[48+ScopeChannel.calibrationOffsets[i]+j];
                //TODO fix byte != 255
                if (extcal != (byte)0 && extcal != (byte)255)
                    calibration[i] = calibration[i] + (extcal - 128) / 250f;
            }
            channelManager.getChannel(j).setOffsets(calibration);

            float[] gains = new float[ScopeChannel.voltageRanges.length];
            for (int i = 0; i < 4; i++) {
                byte extcal = extendedcalibration[32+ScopeChannel.calibrationGainOff[i]+j];
                if (extcal != 0 && extcal != (byte)255)
                    gains[i] = gains[i] * (1 + (extcal - 128) / 500f);
            }
            channelManager.getChannel(j).setOffsets(gains);
        }

        return standardcalibration;
    }

    public AdcInputStream getData() throws UsbException, IOException {
        return getData((short) 0x400);
    }

    public AdcInputStream getData(short size) throws UsbException, IOException {
        ScopeControlRequest.getStartRequest().send(getScopeDevice());
        AdcInputStream adcInputStream = new AdcInputStream(
                hantekConnection.getScopeInterface().getEndpoint().syncReadPipe(size),
                channelManager.getChannelCount(),
                size);
        ScopeControlRequest.getStopRequest().send(getScopeDevice());
        return adcInputStream;
    }

    public FormattedDataStream getFormattedData() throws UsbException, IOException {
        return getFormattedData((short) 0x400);
    }

    public FormattedDataStream getFormattedData(short size) throws UsbException, IOException {
        ScopeControlRequest.getStartRequest().send(getScopeDevice());
        FormattedDataStream formattedDataStream = new FormattedDataStream(
                hantekConnection.getScopeInterface().getEndpoint().syncReadPipe(size),
                channelManager,
                size);
        ScopeControlRequest.getStopRequest().send(getScopeDevice());
        return formattedDataStream;
    }

    public void setCalibrationValues(byte[] calibrationvalues) throws UsbException {
        hantekConnection.setStandardCalibration(calibrationvalues);
    }

    public UsbDevice getScopeDevice() {
        return hantekConnection.getScopeDevice();
    }

    public HantekConnection getScopeUsbConnection() {
        return hantekConnection;
    }

    public FirmwareUploader getFirmwareUploader() {
        return hantekConnection.getFirmwareUploader();
    }

    public boolean isFirmwarePresent() {
        return firmwarePresent;
    }


}
