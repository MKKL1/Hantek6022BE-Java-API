package com.mkkl.hantekapi;

import com.mkkl.hantekapi.constants.OscilloscopeDevices;

import javax.usb.*;
import java.util.*;

//Making sure if for some reason you have multiple oscilloscopes connected you can manage them all

/**
 * Provides utilities for finding device with given values and manage all found instances
 */
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

        for(OscilloscopeDevices scope : new OscilloscopeDevices[]{OscilloscopeDevices.DSO6022BE, OscilloscopeDevices.DSO6022BL, OscilloscopeDevices.DSO6021}) {
            filterDevices(usbDevices, scope);
        }

        return connections;
    }

    public static HashMap<UsbDevice, Oscilloscope> findDevice(OscilloscopeDevices scope) throws UsbException {
        connections = new HashMap<>();
        UsbHub rootUsbHub = UsbHostManager.getUsbServices().getRootUsbHub();
        List<UsbDevice> usbDevices = (List<UsbDevice>) rootUsbHub.getAttachedUsbDevices();
        filterDevices(usbDevices, scope);

        return connections;
    }

    private static void filterDevices(List<UsbDevice> usbDevices, OscilloscopeDevices scope) {
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
//.orElseThrow(() ->
//                    new ScopeNotFoundException("Couldn't find device with product id of '0x" +
//                            HexFormat.of().toHexDigits(scope.getProductId()) + "'"))
    public static Oscilloscope findAndGetFirst(OscilloscopeDevices scope) throws UsbException {
        return findDevice(scope).entrySet().stream().findFirst().map(Map.Entry::getValue).orElseThrow(() -> new NoSuchElementException("Device not found"));
    }

    public static Oscilloscope getFirstFound() {
        return connections.entrySet().stream().findFirst().map(Map.Entry::getValue).orElseThrow(() -> new NoSuchElementException("Device not found"));
    }

}
