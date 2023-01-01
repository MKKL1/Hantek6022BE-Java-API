package com.mkkl.hantekapi.communication.interfaces.endpoints;

import javax.usb.*;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class Endpoint implements AutoCloseable {
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
    protected UsbIrp createReader(PipedInputStream pipedInputStream) throws UsbException, IOException {
        final PipedOutputStream outputStream = new PipedOutputStream(pipedInputStream);

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

    public PipedInputStream asyncReadPipe(short size) throws UsbException, IOException {
        return null;
    }

    public PipedInputStream syncReadPipe(short size) throws UsbException, IOException {
        return null;
    }

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

    @Override
    public void close() throws UsbException {
        pipe.close();
        isPipeOpen = false;
    }
}
