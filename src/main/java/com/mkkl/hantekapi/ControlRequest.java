package com.mkkl.hantekapi;

import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;

public class ControlRequest {
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

    public UsbControlIrp getUsbControlIrp(UsbDevice deivce) {
        UsbControlIrp irp = deivce.createUsbControlIrp(requestType, address, wValue, wValue);
        irp.setData(data);
        irp.setLength(data.length);
        return irp;
    }
}
