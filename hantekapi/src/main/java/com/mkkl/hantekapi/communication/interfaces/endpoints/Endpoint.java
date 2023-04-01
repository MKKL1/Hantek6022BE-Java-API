package com.mkkl.hantekapi.communication.interfaces.endpoints;

import org.usb4java.*;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class Endpoint {
    public static long timeout = 5000;

    protected byte endpointAddress;
    protected final EndpointDescriptor endpointDescriptor;
    protected final DeviceHandle deviceHandle;
    protected boolean isPipeOpen = false;
    private final short packetSize;

    public Endpoint(byte endpointAddress, DeviceHandle deviceHandle, InterfaceDescriptor interfaceDescriptor) {
        this.endpointAddress = endpointAddress;
        this.deviceHandle = deviceHandle;
        endpointDescriptor = interfaceDescriptor.endpoint()[0];
        packetSize = (short) (((getMaxPacketSize() >> 11)+1) * (getMaxPacketSize() & 0x7ff));
    }

    public abstract void asyncReadPipe(short size, TransferCallback callback) throws LibUsbException;

    public abstract void asyncReadPipe(short size, ByteBuffer byteBuffer,TransferCallback callback) throws LibUsbException;

    public abstract void asyncReadPipe(Transfer transfer) throws LibUsbException;

    public abstract ByteBuffer syncReadPipe(short size) throws LibUsbException;

    public abstract Transfer getTransfer(ByteBuffer byteBuffer, TransferCallback callback);

    public abstract Transfer getTransfer(int size, TransferCallback callback);

    public abstract void syncReadPipe(short size, ByteBuffer byteBuffer) throws LibUsbException;

    public short getMaxPacketSize() {
        return endpointDescriptor.wMaxPacketSize();
    }

    public short getPacketSize() {
        return packetSize;
    }
}
