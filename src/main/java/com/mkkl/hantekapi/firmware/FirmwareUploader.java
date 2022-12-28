package com.mkkl.hantekapi.firmware;

import com.mkkl.hantekapi.constants.Scopes;
import com.mkkl.hantekapi.controlrequest.ScopeControlRequest;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class FirmwareUploader {

    private final UsbDevice scopeDevice;

    public FirmwareUploader(UsbDevice scopeDevice) {
        this.scopeDevice = scopeDevice;
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

    public void flash_firmware(Firmwares firmwares) throws IOException, UsbException {
        InputStream firmwareInputStream = getClass().getClassLoader().getResourceAsStream(firmwares.getFilename());
        flash_firmware(firmwareInputStream);
    }

    public void flash_firmware(InputStream firmwareInputStream) throws IOException, UsbException {
        if (firmwareInputStream == null) throw new IOException("No firmware in input stream");
        try(FirmwareReader firmwareReader = new FirmwareReader(new InputStreamReader(firmwareInputStream));) {
            FirmwareControlPacket[] firmwareData = firmwareReader.readFirmwareData();
            for(FirmwareControlPacket packet : firmwareData) {
                ScopeControlRequest.getFirmwareRequest(packet.value, packet.data).send(scopeDevice);
            }
        }
    }
}
