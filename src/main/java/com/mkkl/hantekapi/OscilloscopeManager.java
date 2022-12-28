package com.mkkl.hantekapi;

import com.mkkl.hantekapi.constants.Scopes;

import javax.usb.*;
import java.util.HashMap;
import java.util.List;

//Making sure if for some reason you have multiple oscilloscopes connected you can manage them all
public class OscilloscopeManager {
    private static final short FIRMWARE_VERSION = 0x0210;
    private static final short NO_FIRMWARE_VENDOR_ID = 0x04B4;
    private static final short FIRMWARE_PRESENT_VENDOR_ID = 0x04B5;

    public static HashMap<UsbDevice, Oscilloscope> connections;

    public static HashMap<UsbDevice, Oscilloscope> findAllDevices() throws UsbException {
        connections = new HashMap<>();

        UsbHub rootUsbHub = UsbHostManager.getUsbServices().getRootUsbHub();
        List<UsbDevice> usbDevices = (List<UsbDevice>) rootUsbHub.getAttachedUsbDevices();

        usbDevices.stream().filter(x -> {
            UsbDeviceDescriptor desc = x.getUsbDeviceDescriptor();
            return desc.idVendor() != FIRMWARE_PRESENT_VENDOR_ID && desc.idVendor() != NO_FIRMWARE_VENDOR_ID &&
                    (desc.idProduct() == Scopes.DSO6022BE.getProductId() ||
                            desc.idProduct() == Scopes.DSO6022BL.getProductId() ||
                            desc.idProduct() == Scopes.DSO6021.getProductId());
        }).forEach(x -> {
            Oscilloscope oscilloscope = new Oscilloscope(x, false);
            connections.put(x, oscilloscope);
        });

        for(Scopes scope : new Scopes[]{Scopes.DSO6022BE, Scopes.DSO6022BL, Scopes.DSO6021}) {
            usbDevices.stream().filter(x -> {
                UsbDeviceDescriptor desc = x.getUsbDeviceDescriptor();
                return (desc.idVendor() == FIRMWARE_PRESENT_VENDOR_ID || desc.idVendor() == NO_FIRMWARE_VENDOR_ID)&& desc.idProduct() == scope.getProductId();
            }).forEach(x -> {
                Oscilloscope oscilloscope = new Oscilloscope(x, true);
                connections.put(x, oscilloscope);
            });
        }

        return connections;
    }

    public static HashMap<UsbDevice, Oscilloscope> getOscilloscope(Scopes scope) throws UsbException {
        UsbHub rootUsbHub = UsbHostManager.getUsbServices().getRootUsbHub();
        List<UsbDevice> usbDevices = (List<UsbDevice>) rootUsbHub.getAttachedUsbDevices();

        usbDevices.stream().filter(x -> {
            UsbDeviceDescriptor desc = x.getUsbDeviceDescriptor();
            return desc.idVendor() != FIRMWARE_PRESENT_VENDOR_ID && desc.idVendor() != NO_FIRMWARE_VENDOR_ID &&
                    (desc.idProduct() == scope.getProductId());
        }).forEach(x -> {
            Oscilloscope oscilloscope = new Oscilloscope(x, false);
            connections.put(x, oscilloscope);
        });
        return connections;
    }

    //TODO throw ScopeNotFoundExc
    public static Oscilloscope getFirstOscilloscope(Scopes scope) throws UsbException {
        return getOscilloscope(scope)
                .entrySet()
                .stream()
                .filter(x -> x.getKey().getUsbDeviceDescriptor().idProduct() == scope.getProductId())
                .findFirst()
                .orElseThrow()
                .getValue();
    }

    //TODO throw ScopeNotFoundExc
    public static Oscilloscope getFirstFound() {
        return connections.entrySet().stream().findFirst().orElseThrow().getValue();
    }

}
