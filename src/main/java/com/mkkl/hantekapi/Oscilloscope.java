package com.mkkl.hantekapi;

import javax.usb.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;

public class Oscilloscope {

    private static final short FIRMWARE_VERSION = 0x0210;
    private static final short NO_FIRMWARE_VENDOR_ID = 0x04B4;
    private static final short FIRMWARE_PRESENT_VENDOR_ID = 0x04B5;

    private static final byte RW_FIRMWARE_REQUEST = (byte) 0xa0;
    private static final byte RW_FIRMWARE_INDEX = (byte) 0x00;


    private UsbDevice scopeDevice = null;
    UsbInterface iface;
    private boolean isFirmwarePresent = false;

    public Oscilloscope() {

    }

    public void setup() throws UsbException, UsbScopeNotFoundException {
        UsbHub rootUsbHub = UsbHostManager.getUsbServices().getRootUsbHub();
        scopeDevice = findDevice(rootUsbHub);
    }


    private UsbDevice findDevice(UsbHub hub) throws UsbScopeNotFoundException {
        List<UsbDevice> usbDevices = (List<UsbDevice>) hub.getAttachedUsbDevices();
        //TODO search user defined devices
//        usbDevices.stream().filter(x -> {
//            UsbDeviceDescriptor desc = x.getUsbDeviceDescriptor();
//            return (desc.idVendor() != NO_FIRMWARE_VENDOR_ID && desc.idVendor() != FIRMWARE_PRESENT_VENDOR_ID) && desc.idProduct() == Scopes.DSO6022BE.getProductId();
//        }).findFirst().get();
//
//        for(UsbDevice de : usbDevices) {
//            UsbDeviceDescriptor desc = de.getUsbDeviceDescriptor();
//            String name = "";
//            try {
//                name = de.getProductString();
//            } catch (UsbException | UnsupportedEncodingException e) {
//            }
//        }
//        System.out.println("");
        for(Scopes scope : new Scopes[]{Scopes.DSO6022BE, Scopes.DSO6022BL, Scopes.DSO6021}) {
            Optional<UsbDevice> device = usbDevices.stream().filter(x -> {
                UsbDeviceDescriptor desc = x.getUsbDeviceDescriptor();
                return (desc.idVendor() == FIRMWARE_PRESENT_VENDOR_ID || desc.idVendor() == NO_FIRMWARE_VENDOR_ID)&& desc.idProduct() == scope.getProductId();
            }).findFirst();
            if(device.isPresent()) return device.get();
        }
        throw new UsbScopeNotFoundException("Device couldn't be found");

    }

    public void open_handle() throws UsbException {
        if (scopeDevice == null) throw new RuntimeException("Device was not found, use Oscilloscope.setup()");

        UsbConfiguration configuration = scopeDevice.getActiveUsbConfiguration();
        iface = configuration.getUsbInterface((byte) 0);
        iface.claim();
    }

    public void close_handle() throws UsbException {
        iface.release();
    }

    public byte[] read_eeprom(short offset, short length) throws UsbException {
        UsbControlIrp irp = scopeDevice.createUsbControlIrp((byte) 64,(byte) 162,offset,(short) 0);
        irp.setData(new byte[length]);
        scopeDevice.syncSubmit(irp);
        return irp.getData();
    }

    public boolean flash_firmware(Firmwares firmwares) throws InterruptedException {
        FirmwareReader firmwareReader = null;
        try {
            InputStream firmwareInputStream = getClass().getClassLoader().getResourceAsStream(firmwares.getFilename());
            if (firmwareInputStream == null) throw new IOException("Firmware given by filename " + firmwares.getFilename() + " not found");
            firmwareReader = new FirmwareReader(new InputStreamReader(firmwareInputStream));
            FirmwareControlPacket[] firmwareData = firmwareReader.readFirmwareData();

            for(FirmwareControlPacket packet : firmwareData) {
                UsbControlIrp irp = scopeDevice.createUsbControlIrp((byte) 64,(byte) 160,packet.value,RW_FIRMWARE_INDEX);

                irp.setData(packet.data);
                irp.setLength(packet.size);
                scopeDevice.syncSubmit(irp);
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

        //TODO set timeout instead
        //TODO Throw runtime exception when timed out
        Thread.sleep(500);
        int i = 0;
        for (; i < 30; i++) {
            try {
                setup();
                break;
            } catch (UsbException e) {
                Thread.sleep(100);
            }
        }
        if (i == 30) return false;
        return true;

    }

    public UsbDevice getScopeDevice() {
        return scopeDevice;
    }
}
