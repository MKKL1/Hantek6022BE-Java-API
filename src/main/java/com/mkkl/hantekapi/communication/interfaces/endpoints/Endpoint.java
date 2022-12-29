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

    public Endpoint(byte endpointAddress, UsbInterface usbInterface) {
        this.endpointAddress = endpointAddress;
        this.usbEndpoint = usbInterface.getUsbEndpoint(endpointAddress);
        pipe = usbEndpoint.getUsbPipe();
    }

    //TODO close outputstream
    private UsbIrp createReader(PipedInputStream pipedInputStream) throws UsbException, IOException {
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
        openPipe();
        final PipedInputStream inputStream = new PipedInputStream();
        UsbIrp irp = createReader(inputStream);
        irp.setData(new byte[size]);
        pipe.asyncSubmit(irp);
        return inputStream;
    }
    public PipedInputStream syncReadPipe(short size) throws UsbException, IOException {
        openPipe();
        final PipedInputStream inputStream = new PipedInputStream();
        UsbIrp irp = createReader(inputStream);
        irp.setData(new byte[size]);
        pipe.asyncSubmit(irp);
        irp.waitUntilComplete();
        return inputStream;
    }

    public void openPipe() throws UsbException {
        pipe.open();
    }

    @Override
    public void close() throws UsbException {
        pipe.close();
    }
}