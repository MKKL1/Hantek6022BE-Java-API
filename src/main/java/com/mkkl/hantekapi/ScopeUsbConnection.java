package com.mkkl.hantekapi;

import com.mkkl.hantekapi.constants.Scopes;
import com.mkkl.hantekapi.controlrequest.ScopeControlRequest;
import com.mkkl.hantekapi.controlrequest.UsbConnectionConst;
import com.mkkl.hantekapi.firmware.FirmwareControlPacket;
import com.mkkl.hantekapi.firmware.FirmwareReader;
import com.mkkl.hantekapi.firmware.Firmwares;

import javax.usb.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ScopeUsbConnection {
    private final UsbDevice scopeDevice;
    private UsbConfiguration configuration;
    private UsbInterface iface;

    public ScopeUsbConnection(UsbDevice usbDevice) {
        this.scopeDevice = usbDevice;
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

    public boolean flash_firmware(Scopes scope) throws InterruptedException {
        return flash_firmware(scope.getFirmwareToFlash());
    }

    public boolean flash_firmware(Firmwares firmwares) throws InterruptedException {
        InputStream firmwareInputStream = getClass().getClassLoader().getResourceAsStream(firmwares.getFilename());
        return flash_firmware(firmwareInputStream);
    }

    public boolean flash_firmware(InputStream firmwareInputStream) throws InterruptedException {
        FirmwareReader firmwareReader = null;
        try {
            if (firmwareInputStream == null) throw new IOException("No firmware in input stream");
            firmwareReader = new FirmwareReader(new InputStreamReader(firmwareInputStream));
            FirmwareControlPacket[] firmwareData = firmwareReader.readFirmwareData();

            for(FirmwareControlPacket packet : firmwareData) {
                ScopeControlRequest.getFirmwareRequest(packet.value, packet.data).send(scopeDevice);
            }
        } catch (IOException | UsbException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (firmwareReader != null) {
                try {
                    firmwareReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

//        //TODO set timeout instead
//        //TODO Throw runtime exception when timed out
//        Thread.sleep(500);
//        int i = 0;
//        for (; i < 30; i++) {
//            try {
//                OscilloscopeManager.
//                break;
//            } catch (UsbException e) {
//                Thread.sleep(100);
//            }
//        }
//        if (i == 30) return false;
//        return true;
        return true;
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


    public AdcInputStream readData(short size) throws UsbException {
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
            adcInputStream = new AdcInputStream(data);
        }
        finally
        {
            pipe.close();
            ScopeControlRequest.getStopRequest().send(scopeDevice);
        }
        return adcInputStream;
    }
}
