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

    public UsbControlIrp getUsbControlIrp(UsbDevice device) {
        UsbControlIrp irp = device.createUsbControlIrp(requestType, address, wValue, wIndex);
        irp.setData(data);
        irp.setLength(data.length);
        return irp;
    }

    public void write(UsbDevice device) throws UsbException {
        //System.out.println("controlwrite " + (requestType&0xFF) + " " + (address&0xFF) + " " + (wValue&0xFF) + " " + (wIndex&0xFF) + " " + Arrays.toString(data));
        UsbControlIrp irp = device.createUsbControlIrp(requestType, address, wValue, wIndex);
        irp.setData(data);
        device.syncSubmit(irp);
    }

    public byte[] read(UsbDevice device) throws UsbException {
        //System.out.println("controlread " + (requestType&0xFF) + " " + (address&0xFF) + " " + (wValue&0xFF) + " " + (wIndex&0xFF) + " " + Arrays.toString(data));
        UsbControlIrp irp = device.createUsbControlIrp(requestType, address, wValue, wIndex);
        irp.setData(data);
        device.syncSubmit(irp);
        return irp.getData();
    }
}
