package com.mkkl.hantekapi.communication.interfaces.endpoints;

import org.usb4java.*;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class Endpoint {
    public static long timeout;

    protected byte endpointAddress;
    protected final EndpointDescriptor endpointDescriptor;
    protected final DeviceHandle deviceHandle;
    protected boolean isPipeOpen = false;

    public Endpoint(byte endpointAddress, DeviceHandle deviceHandle, InterfaceDescriptor interfaceDescriptor) {
        this.endpointAddress = endpointAddress;
        this.deviceHandle = deviceHandle;
        endpointDescriptor = interfaceDescriptor.endpoint()[0];
    }

    public abstract void asyncReadPipe(short size, AdcDataListener adcDataListener) throws LibUsbException, IOException;

    public abstract ByteBuffer syncReadPipe(short size) throws LibUsbException, IOException;

    public short getMaxPacketSize() {
        return endpointDescriptor.wMaxPacketSize();
    }

    public short getPacketSize() {
        short maxpacketsize = getMaxPacketSize();
        return (short) (((maxpacketsize >> 11)+1) * (maxpacketsize & 0x7ff)); //Not sure what it does, copied from python api;
    }
}
