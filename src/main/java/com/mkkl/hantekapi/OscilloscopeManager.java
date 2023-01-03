package com.mkkl.hantekapi;

import com.mkkl.hantekapi.constants.Scopes;

import javax.usb.*;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;

//Making sure if for some reason you have multiple oscilloscopes connected you can manage them all
public class OscilloscopeManager {
    private static final short FIRMWARE_VERSION = 0x0210;
    private static final short NO_FIRMWARE_VENDOR_ID = 0x04B4;
    private static final short FIRMWARE_PRESENT_VENDOR_ID = 0x04B5;

    public static HashMap<UsbDevice, Oscilloscope> connections;

    public static HashMap<UsbDevice, Oscilloscope> findSupportedDevices() throws UsbException {
        connections = new HashMap<>();

        UsbHub rootUsbHub = UsbHostManager.getUsbServices().getRootUsbHub();
        List<UsbDevice> usbDevices = (List<UsbDevice>) rootUsbHub.getAttachedUsbDevices();

//        usbDevices.stream().filter(x -> {
//            UsbDeviceDescriptor desc = x.getUsbDeviceDescriptor();
//            return desc.idVendor() != FIRMWARE_PRESENT_VENDOR_ID && desc.idVendor() != NO_FIRMWARE_VENDOR_ID &&
//                    (desc.idProduct() != Scopes.DSO6022BE.getProductId() ||
//                            desc.idProduct() != Scopes.DSO6022BL.getProductId() ||
//                            desc.idProduct() != Scopes.DSO6021.getProductId());
//        }).forEach(x -> {
//            Oscilloscope oscilloscope = new Oscilloscope(x, false);
//            connections.put(x, oscilloscope);
//        });

        for(Scopes scope : new Scopes[]{Scopes.DSO6022BE, Scopes.DSO6022BL, Scopes.DSO6021}) {
            filterDevices(usbDevices, scope);
        }

        return connections;
    }

    public static HashMap<UsbDevice, Oscilloscope> findDevice(Scopes scope) throws UsbException {
        connections = new HashMap<>();
        UsbHub rootUsbHub = UsbHostManager.getUsbServices().getRootUsbHub();
        List<UsbDevice> usbDevices = (List<UsbDevice>) rootUsbHub.getAttachedUsbDevices();
        filterDevices(usbDevices, scope);

        return connections;
    }

    private static void filterDevices(List<UsbDevice> usbDevices, Scopes scope) {
        usbDevices.stream().filter(x -> {
            UsbDeviceDescriptor desc = x.getUsbDeviceDescriptor();
            return (desc.idVendor() == FIRMWARE_PRESENT_VENDOR_ID || desc.idVendor() == NO_FIRMWARE_VENDOR_ID) &&
                    desc.idProduct() == scope.getProductId();
        }).forEach(x -> {
            UsbDeviceDescriptor desc = x.getUsbDeviceDescriptor();
            boolean isFirmwarePresent = desc.idVendor() == FIRMWARE_PRESENT_VENDOR_ID && desc.bcdDevice() == FIRMWARE_VERSION;
            Oscilloscope oscilloscope = Oscilloscope.create(x, isFirmwarePresent);
            connections.put(x, oscilloscope);
        });
    }

    //TODO throw ScopeNotFoundExc
    public static Oscilloscope findAndGetFirst(Scopes scope) throws UsbException {
        return findDevice(scope).entrySet().stream()
                .findFirst()
                .orElseThrow(() ->
                    new ScopeNotFoundException("Couldn't find device with product id of '0x" +
                            HexFormat.of().toHexDigits(scope.getProductId()) + "'"))
                .getValue();
    }

    public static Oscilloscope getFirstFound() {
        return connections.entrySet().stream().findFirst().orElseThrow(ScopeNotFoundException::new).getValue();
    }

}
