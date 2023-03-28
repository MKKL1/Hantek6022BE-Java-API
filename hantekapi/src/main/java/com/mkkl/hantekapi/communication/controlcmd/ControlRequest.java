package com.mkkl.hantekapi.communication.controlcmd;

import org.usb4java.Device;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

import java.nio.ByteBuffer;

public class ControlRequest {
    public static long timeout = 0;

    private final byte requestType;
    private final byte address;
    private final short wValue;
    private final short wIndex;

    private final byte[] data;

    public ControlRequest(byte requestType, byte address, short wValue, short wIndex, byte[] data) {
        this.requestType = requestType;
        this.address = address;
        this.wValue = wValue;
        this.wIndex = wIndex;
        this.data = data;
    }

    public byte getRequestType() {
        return requestType;
    }

    public byte getAddress() {
        return address;
    }

    public short getwValue() {
        return wValue;
    }

    public short getwIndex() {
        return wIndex;
    }

    public byte[] getData() {
        return data;
    }


    public void write(DeviceHandle handle) throws LibUsbException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
        buffer.put(data);
        int transfered = LibUsb.controlTransfer(handle, requestType, address, wValue, wIndex, buffer, timeout);
        if(transfered < 0) throw new LibUsbException("Control transfer failed", transfered);
    }

    public byte[] read(DeviceHandle handle) throws LibUsbException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
        int transfered = LibUsb.controlTransfer(handle, requestType, address, wValue, wIndex, buffer, timeout);
        if(transfered < 0) throw new LibUsbException("Control transfer failed", transfered);
        buffer.get(data);
        return data;
    }
}
