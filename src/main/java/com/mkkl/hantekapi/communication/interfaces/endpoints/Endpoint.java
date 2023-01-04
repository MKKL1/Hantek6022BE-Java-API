package com.mkkl.hantekapi.communication.interfaces.endpoints;

import javax.usb.*;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public abstract class Endpoint {
    protected byte endpointAddress;
    protected UsbEndpoint usbEndpoint;
    protected UsbPipe pipe;
    protected final UsbEndpointDescriptor descriptor;
    protected final short maxPacketSize;
    protected final short packetSize;
    protected boolean isPipeOpen = false;

    public Endpoint(byte endpointAddress, UsbInterface usbInterface) {
        this.endpointAddress = endpointAddress;
        this.usbEndpoint = usbInterface.getUsbEndpoint(endpointAddress);
        pipe = usbEndpoint.getUsbPipe();
        descriptor = usbEndpoint.getUsbEndpointDescriptor();
        maxPacketSize = descriptor.wMaxPacketSize();
        packetSize = (short) (((maxPacketSize >> 11)+1) * (maxPacketSize & 0x7ff)); //Not sure what it does, copied from python api
    }

    //TODO close outputstream
    protected synchronized UsbIrp createAsyncReader(OutputStream outputStream) throws UsbException, IOException {
        UsbIrp irp = pipe.createUsbIrp();
        pipe.addUsbPipeListener(new UsbPipeListener() {
            @Override
            public void errorEventOccurred(UsbPipeErrorEvent usbPipeErrorEvent) {
                UsbException e = usbPipeErrorEvent.getUsbException();
                e.printStackTrace();
            }

            @Override
            public void dataEventOccurred(UsbPipeDataEvent usbPipeDataEvent) {
                byte[] data = usbPipeDataEvent.getData();
                try{
                    outputStream.write(data);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        return irp;
    }

    public abstract void asyncReadPipe(OutputStream outputStream, short size) throws UsbException, IOException;

    public abstract void syncReadPipe(OutputStream outputStream, short size) throws UsbException, IOException;

    public UsbEndpoint getUsbEndpoint() {
        return usbEndpoint;
    }

    public UsbEndpointDescriptor getDescriptor() {
        return descriptor;
    }

    public short getMaxPacketSize() {
        return maxPacketSize;
    }

    public short getPacketSize() {
        return packetSize;
    }

    public void openPipe() throws UsbException {
        pipe.open();
        isPipeOpen = true;
    }

    public void close() throws UsbException {
        pipe.close();
        isPipeOpen = false;
    }
}
