package com.mkkl.hantekapi;

import com.mkkl.hantekapi.firmware.FirmwareControlPacket;
import com.mkkl.hantekapi.firmware.FirmwareReader;
import com.mkkl.hantekapi.firmware.Firmwares;

import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class EepromConnection {
    private UsbDevice usbDevice;

    public EepromConnection(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
    }

    public byte[] read_eeprom(short offset, short length) throws UsbException {
        UsbControlIrp irp = ScopeControlRequest.getEepromControlRequest(offset, new byte[length]).getUsbControlIrp(usbDevice);
        usbDevice.syncSubmit(irp);
        return irp.getData();
    }

    public void write_eeprom(short offset, byte[] data) throws UsbException {
        UsbControlIrp irp = ScopeControlRequest.getEepromControlRequest(offset, data).getUsbControlIrp(usbDevice);
        usbDevice.syncSubmit(irp);
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
                UsbControlIrp irp = ScopeControlRequest.getFirmwareControlRequest(packet.value, packet.data).getUsbControlIrp(usbDevice);
                usbDevice.syncSubmit(irp);
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
        return read_eeprom((short) 8, (short) 32);
    }

    public byte[] getExtendedCalibration() throws UsbException {
        return read_eeprom((short) 8, (short) 80);
    }
}
