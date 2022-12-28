package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.ScopeChannel;
import com.mkkl.hantekapi.firmware.FirmwareUploader;

import javax.usb.*;
import java.io.InputStream;
import java.util.Arrays;

public class Oscilloscope {
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
        hantekConnection.open();
    }

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
                if (extcal != 0 && extcal != 255)
                    calibration[i] = calibration[i] + (extcal - 128) / 250f;
            }
            channelManager.getChannel(j).setOffsets(calibration);

            float[] gains = new float[ScopeChannel.voltageRanges.length];
            for (int i = 0; i < 4; i++) {
                byte extcal = extendedcalibration[32+ScopeChannel.calibrationGainOff[i]+j];
                if (extcal != 0 && extcal != 255)
                    gains[i] = gains[i] * (1 + (extcal - 128) / 500f);
            }
            channelManager.getChannel(j).setOffsets(gains);
        }

        return standardcalibration;
    }

    public AdcInputStream getData() throws UsbException {
        return getData((short) 0x400);
    }

    public AdcInputStream getData(short size) throws UsbException {
        return new AdcInputStream(hantekConnection.readRawData(size), channelManager.getChannelCount());
    }

    public FormattedDataStream getFormattedData() throws UsbException {
        return getFormattedData((short) 0x400);
    }

    public FormattedDataStream getFormattedData(short size) throws UsbException {
        return new FormattedDataStream(hantekConnection.readRawData(size), channelManager);
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
