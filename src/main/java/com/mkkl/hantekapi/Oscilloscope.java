package com.mkkl.hantekapi;

import javax.usb.*;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Oscilloscope {
    private final ScopeUsbConnection scopeUsbConnection;
    private final byte channelCount = 2;
    private ScopeChannel[] scopeChannels;


    private boolean firmwarePresent = false;

    public Oscilloscope(UsbDevice usbDevice) {
        this.scopeUsbConnection = new ScopeUsbConnection(usbDevice);
        scopeChannels = new ScopeChannel[channelCount];
        for (int i = 0; i < channelCount; i++) {
            scopeChannels[i] = new ScopeChannel((byte) i);
        }
    }

    public Oscilloscope(UsbDevice usbDevice, boolean firmwarePresent){
        this(usbDevice);
        this.firmwarePresent = firmwarePresent;
    }

    public void open() throws UsbException {
        scopeUsbConnection.open();
    }


    public byte[] getCalibrationValues() throws UsbException {
        byte[] standardcalibration = scopeUsbConnection.getStandardCalibration();
        byte[] extendedcalibration = scopeUsbConnection.getExtendedCalibration();

        System.out.println(Arrays.toString(standardcalibration));
        System.out.println(Arrays.toString(extendedcalibration));
        for(int j = 0; j < channelCount; j++) {
            float[] calibration = new float[ScopeChannel.voltageRanges.length];
            for (int i = 0; i < 4; i++) {
                calibration[i] = standardcalibration[ScopeChannel.calibrationOffsets[i]+j]-128;
                byte extcal = extendedcalibration[48+ScopeChannel.calibrationOffsets[i]+j];
                //TODO fix byte != 255
                if (extcal != 0 && extcal != 255)
                    calibration[i] = calibration[i] + (extcal - 128) / 250f;
            }
            getChannel(j).setOffsets(calibration);
        }

        for(int j = 0; j < channelCount; j++) {
            float[] gains = new float[ScopeChannel.voltageRanges.length];
            for (int i = 0; i < 4; i++) {
                byte extcal = extendedcalibration[32+ScopeChannel.calibrationGainOff[i]+j];
                if (extcal != 0 && extcal != 255)
                    gains[i] = gains[i] * (1 + (extcal - 128) / 500f);
            }
            getChannel(j).setOffsets(gains);
        }

        return standardcalibration;
    }

    public InputStream channelinput() throws UsbException {
        return scopeUsbConnection.readData((short) 0x400);
    }

    public void setCalibrationValues(byte[] calibrationvalues) throws UsbException {
        scopeUsbConnection.setStandardCalibration(calibrationvalues);
    }

    public UsbDevice getScopeDevice() {
        return scopeUsbConnection.getScopeDevice();
    }

    public ScopeUsbConnection getScopeUsbConnection() {
        return scopeUsbConnection;
    }

    public ScopeChannel[] getChannels() {
        return scopeChannels;
    }

    public ScopeChannel getChannel(int id) {
        return scopeChannels[id];
    }

    public boolean isFirmwarePresent() {
        return firmwarePresent;
    }

    public void close() throws UsbException{
        scopeUsbConnection.close();
    }
}
