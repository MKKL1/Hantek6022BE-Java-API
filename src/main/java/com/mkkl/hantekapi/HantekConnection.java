package com.mkkl.hantekapi;

import com.mkkl.hantekapi.controlrequest.ScopeControlRequest;
import com.mkkl.hantekapi.controlrequest.UsbConnectionConst;
import com.mkkl.hantekapi.firmware.FirmwareUploader;

import javax.usb.*;

public class HantekConnection {
    private final UsbDevice scopeDevice;
    private UsbConfiguration configuration;
    private final FirmwareUploader firmwareUploader;
    private UsbInterface iface;

    public HantekConnection(UsbDevice usbDevice) {
        this.scopeDevice = usbDevice;
        this.firmwareUploader = new FirmwareUploader(scopeDevice);
    }

    public void open() throws UsbException {
        configuration = scopeDevice.getActiveUsbConfiguration();
        iface = configuration.getUsbInterface((byte) 0);
        iface.claim();
    }

    public void close() throws UsbException {
        iface.release();
    }

    public byte[] read_eeprom(short offset, short length) throws UsbException {
        return ScopeControlRequest.getEepromRequest(offset, new byte[length]).sendget(scopeDevice);
    }

    public void write_eeprom(short offset, byte[] data) throws UsbException {
        ScopeControlRequest.getEepromRequest(offset, data).send(scopeDevice);
    }

    public byte[] getStandardCalibration() throws UsbException {
        return read_eeprom(UsbConnectionConst.CALIBRATION_EEPROM_OFFSET, (short) 32);
    }

    public void setStandardCalibration(byte[] data) throws UsbException {
        write_eeprom(UsbConnectionConst.CALIBRATION_EEPROM_OFFSET, data);
    }

    public byte[] getExtendedCalibration() throws UsbException {
        return read_eeprom(UsbConnectionConst.CALIBRATION_EEPROM_OFFSET, (short) 80);
    }


    public UsbDevice getScopeDevice() {
        return scopeDevice;
    }

    public FirmwareUploader getFirmwareUploader() {
        return firmwareUploader;
    }

    public byte[] readRawData(short size) throws UsbException {
        AdcInputStream adcInputStream = null;

        ScopeControlRequest.getStartRequest().send(scopeDevice);

        UsbEndpoint endpoint = iface.getUsbEndpoint((byte) 0x86);
        UsbPipe pipe = endpoint.getUsbPipe();
        pipe.open();
        try
        {
            //TODO size*number of active channels
            byte[] data = new byte[size*2];
            int received = pipe.syncSubmit(data);
            return data;
        }
        finally
        {
            pipe.close();
            ScopeControlRequest.getStopRequest().send(scopeDevice);
        }
    }
}
