package com.mkkl.hantekapi;

import org.usb4java.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class UsbDevice {
    private final Device device;
    private final DeviceHandle deviceHandle = new DeviceHandle();
    private boolean open = false;

    public UsbDevice(Device device) {
        this.device = device;
    }

    public DeviceHandle open() {
        int result = LibUsb.open(device, deviceHandle);
        if(result < 0) throw new LibUsbException("Unable to open USB device", result);
        open = true;
        return deviceHandle;
    }

    public void close() {
        LibUsb.close(deviceHandle);
        open = false;
    }

    public DeviceDescriptor getDeviceDescriptor() {
        DeviceDescriptor deviceDescriptor = new DeviceDescriptor();

        int result = LibUsb.getDeviceDescriptor(device, deviceDescriptor);
        if(result < 0) throw new LibUsbException("Unable to read device's descriptor", result);

        return deviceDescriptor;
    }

    public ConfigDescriptor getConfigDescriptor(byte b) {
        ConfigDescriptor configDescriptor = new ConfigDescriptor();

        int result = LibUsb.getConfigDescriptor(device, b, configDescriptor);
        if(result < 0) throw new LibUsbException("Unable to read config descriptor", result);

        return configDescriptor;
    }

    public String getStringDescriptor() {
        final ByteBuffer data = ByteBuffer.allocateDirect(256);

        int result = LibUsb.getStringDescriptor(deviceHandle, getDeviceDescriptor().iProduct(), (byte) 0, data);
        if(result != LibUsb.SUCCESS) throw new LibUsbException("Unable to read device string descriptor", result);

        byte[] bString = new byte[data.get(0)-2];
        data.position(2);
        data.get(bString);

        return new String(bString, StandardCharsets.UTF_16LE);
    }

    public boolean isOpen() {
        return open;
    }

    public Device getDevice() {
        return device;
    }

    public DeviceHandle getDeviceHandle() {
        return deviceHandle;
    }
}
