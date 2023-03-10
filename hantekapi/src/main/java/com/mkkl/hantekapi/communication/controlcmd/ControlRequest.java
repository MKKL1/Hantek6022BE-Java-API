package com.mkkl.hantekapi.communication.controlcmd;

import javax.usb.UsbControlIrp;
import javax.usb.UsbDevice;
import javax.usb.UsbException;
import java.util.Arrays;

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


    public void write(UsbDevice device) throws UsbException {
        UsbControlIrp irp = device.createUsbControlIrp(requestType, address, wValue, wIndex);
        irp.setData(data);
        device.syncSubmit(irp);
    }

    public byte[] read(UsbDevice device) throws UsbException {
        UsbControlIrp irp = device.createUsbControlIrp(requestType, address, wValue, wIndex);
        irp.setData(data);
        device.syncSubmit(irp);
        return irp.getData();
    }
}
