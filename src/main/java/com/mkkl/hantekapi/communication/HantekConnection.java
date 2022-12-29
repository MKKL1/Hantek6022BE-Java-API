package com.mkkl.hantekapi.communication;

import com.mkkl.hantekapi.constants.Scopes;
import com.mkkl.hantekapi.communication.controlcmd.ScopeControlRequest;
import com.mkkl.hantekapi.communication.UsbConnectionConst;
import com.mkkl.hantekapi.communication.interfaces.ScopeInterface;
import com.mkkl.hantekapi.communication.interfaces.ScopeInterfaces;
import com.mkkl.hantekapi.firmware.*;

import javax.usb.*;
import java.io.*;
import java.util.Arrays;

public class HantekConnection implements AutoCloseable{
    private final UsbDevice scopeDevice;
    private final ScopeInterface scopeInterface;

    public HantekConnection(UsbDevice usbDevice) {
        this.scopeDevice = usbDevice;
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

    public void flash_firmware() throws IOException, UsbException {
        flash_firmware(
                Arrays.stream(Scopes.values())
                        .filter(x -> x.getProductId() == scopeDevice.getUsbDeviceDescriptor().idProduct())
                        .findFirst()
                        .orElseThrow());
    }

    public void flash_firmware(Scopes scope) throws IOException, UsbException {
        flash_firmware(scope.getFirmwareToFlash());
    }

    public void flash_firmware(SupportedFirmwares supportedFirmwares) throws IOException, UsbException {
        InputStream firmwareInputStream = getClass().getClassLoader().getResourceAsStream(supportedFirmwares.getFilename());
        flash_firmware(firmwareInputStream);
    }

    public void flash_firmware(InputStream firmwareInputStream) throws IOException, UsbException {
        if (firmwareInputStream == null) throw new IOException("No firmware in input stream");
        try(FirmwareReader firmwareReader = new FirmwareReader(new BufferedReader(new InputStreamReader(firmwareInputStream)))) {
            flash_firmware(new ScopeFirmware(firmwareReader.readAll()));
        }
    }

    public void flash_firmware(ScopeFirmware firmware) throws UsbException {
        for(FirmwareControlPacket packet : firmware.getFirmwareData()) {
            ScopeControlRequest.getFirmwareRequest(packet.address(), packet.data()).send(scopeDevice);
        }
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

    public ScopeInterface getScopeInterface() {
        return scopeInterface;
    }
}
