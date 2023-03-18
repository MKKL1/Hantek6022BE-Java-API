package com.mkkl.hantekapi.communication.interfaces.endpoints;

import com.mkkl.hantekapi.communication.adcdata.AdcDataListener;

import javax.usb.*;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;
import java.io.IOException;

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

    //TODO don't create new listener each time
    protected synchronized UsbIrp createAsyncReader(short size, AdcDataListener adcDataListener) {
        final short[] _size = {size};
        final int[] finalSize = {0};
        UsbIrp irp = pipe.createUsbIrp();
        pipe.addUsbPipeListener(new UsbPipeListener() {
            @Override
            public void errorEventOccurred(UsbPipeErrorEvent usbPipeErrorEvent) {
                adcDataListener.onError(usbPipeErrorEvent.getUsbException());
            }

            @Override
            public void dataEventOccurred(UsbPipeDataEvent usbPipeDataEvent) {
                byte[] data = usbPipeDataEvent.getData();
                adcDataListener.onDataReceived(data);
                _size[0] -= data.length;
                finalSize[0] += data.length;
                if(_size[0] <= 0) adcDataListener.onCompleted(finalSize[0]);
            }
        });
        return irp;
    }

    public abstract void asyncReadPipe(short size, AdcDataListener adcDataListener) throws UsbException, IOException;

    public abstract byte[] syncReadPipe(short size) throws UsbException, IOException;

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

    public void closePipe() throws UsbException {
        pipe.close();
        isPipeOpen = false;
    }

    public void close() throws UsbException {
        closePipe();
    }
}
