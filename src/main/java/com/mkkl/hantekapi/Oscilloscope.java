package com.mkkl.hantekapi;

import javax.usb.*;
import java.util.List;
import java.util.Optional;

public class Oscilloscope {

    private static final short FIRMWARE_VERSION = 0x0210;
    private static final short NO_FIRMWARE_VENDOR_ID = 0x04B4;
    private static final short FIRMWARE_PRESENT_VENDOR_ID = 0x04B5;


    private UsbDevice scopeDevice = null;
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


        for(Scopes scope : new Scopes[]{Scopes.DSO6022BE, Scopes.DSO6022BL, Scopes.DSO6021}) {
            Optional<UsbDevice> device = usbDevices.stream().filter(x -> {
                UsbDeviceDescriptor desc = x.getUsbDeviceDescriptor();
                return (desc.idVendor() == FIRMWARE_PRESENT_VENDOR_ID || desc.idVendor() == NO_FIRMWARE_VENDOR_ID)&& desc.idProduct() == scope.getProductId();
            }).findFirst();
            if(device.isPresent()) return device.get();
        }
        throw new UsbScopeNotFoundException("Device couldn't be found");

    }

    public UsbDevice getScopeDevice() {
        return scopeDevice;
    }
}
