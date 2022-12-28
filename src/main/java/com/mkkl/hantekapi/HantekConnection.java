package com.mkkl.hantekapi;

import com.mkkl.hantekapi.controlrequest.ScopeControlRequest;
import com.mkkl.hantekapi.controlrequest.UsbConnectionConst;
import com.mkkl.hantekapi.endpoints.ScopeInterface;
import com.mkkl.hantekapi.endpoints.ScopeInterfaces;
import com.mkkl.hantekapi.firmware.FirmwareUploader;

import javax.usb.*;
import java.io.PipedInputStream;

public class HantekConnection implements AutoCloseable{
    private final UsbDevice scopeDevice;
    private final FirmwareUploader firmwareUploader;
    private final ScopeInterface scopeInterface;

    public HantekConnection(UsbDevice usbDevice) {
        this.scopeDevice = usbDevice;
        this.firmwareUploader = new FirmwareUploader(scopeDevice);
        scopeInterface = new ScopeInterface(scopeDevice);
    }

    public void setInterface(ScopeInterfaces scopeInterfaces) {
        scopeInterface.setInterface(scopeInterfaces);
    }

    public void open() throws UsbException {
        scopeInterface.claim();
    }

    @Override
    public void close() throws UsbException {
        scopeInterface.close();
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

    public ScopeInterface getScopeInterface() {
        return scopeInterface;
    }
}
