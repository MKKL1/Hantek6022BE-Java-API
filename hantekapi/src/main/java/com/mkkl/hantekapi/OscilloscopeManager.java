package com.mkkl.hantekapi;

import com.mkkl.hantekapi.constants.HantekDevices;
import org.usb4java.*;

import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//Making sure if for some reason you have multiple oscilloscopes connected you can manage them all

/**
 * Provides utilities for finding device with given values and manage all found instances
 */
public class OscilloscopeManager {
    private static final short FIRMWARE_VERSION = 0x0210;
    private static final short NO_FIRMWARE_VENDOR_ID = 0x04B4;
    private static final short FIRMWARE_PRESENT_VENDOR_ID = 0x04B5;

    public static HantekDeviceList findSupportedDevices() throws LibUsbException {
        //Initialize context
        Context context = new Context();
        int result = LibUsb.init(context);
        if(result < 0) throw new LibUsbException("Unable to initialize libusb", result);

        //Initialize device list
        DeviceList devices = new DeviceList();
        result = LibUsb.getDeviceList(context, devices);
        if(result < 0) throw new LibUsbException("Unable to get device list", result);

        //List of hantek devices eg. DSO6021,DSO6022BE,DSO6022BL
        List<HantekDevices> hantekDevices = Stream.of(HantekDevices.values()).toList();
        List<HantekDeviceRecord> deviceList = new ArrayList<>();
        try
        {
            for (Device device: devices)
            {
                try {
                    //Get descriptor of device
                    DeviceDescriptor desc = new DeviceDescriptor();
                    result = LibUsb.getDeviceDescriptor(device, desc);
                    if (result < 0) throw new LibUsbException("Unable to read device descriptor", result);
                    //Check if vendor id matches any of defined values
                    boolean vendorIdGood = (desc.idVendor() == FIRMWARE_PRESENT_VENDOR_ID || desc.idVendor() == NO_FIRMWARE_VENDOR_ID);
                    //Check if product id matches any of DSO6021,DSO6022BE,DSO6022BL
                    Optional<HantekDevices> deviceType = hantekDevices.stream()
                            .filter(x -> x.getProductId() == desc.idProduct())
                            .findFirst();
                    boolean productIdGood = deviceType.isPresent();
                    //Continue if device is not supported
                    if (!vendorIdGood || !productIdGood) continue;
                    boolean isFirmwarePresent = desc.idVendor() == FIRMWARE_PRESENT_VENDOR_ID && desc.bcdDevice() == FIRMWARE_VERSION;
                    Oscilloscope oscilloscope = Oscilloscope.create(device, isFirmwarePresent);
                    deviceList.add(new HantekDeviceRecord(device, oscilloscope, deviceType.get()));
                } catch (LibUsbException e) {
                    //TODO log
                }
            }
        }
        finally
        {
            LibUsb.freeDeviceList(devices, true);
        }
        LibUsb.exit(context);
        return new HantekDeviceList(deviceList);
    }
}
