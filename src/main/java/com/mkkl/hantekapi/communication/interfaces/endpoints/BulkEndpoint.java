package com.mkkl.hantekapi.communication.interfaces.endpoints;

import com.mkkl.hantekapi.communication.UsbConnectionConst;

import javax.usb.UsbConst;
import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbIrp;
import java.io.IOException;
import java.io.PipedInputStream;

public class BulkEndpoint extends Endpoint{
    public BulkEndpoint(UsbInterface usbInterface) {
        super(UsbConnectionConst.BULK_ENDPOINT_ADDRESS, usbInterface);
        if(usbEndpoint.getType() != UsbConst.ENDPOINT_TYPE_BULK) throw new RuntimeException("Endpoint is not of type bulk");
    }

    @Override
    public PipedInputStream asyncReadPipe(short size) throws UsbException, IOException {
        if(!isPipeOpen) openPipe();
        final PipedInputStream inputStream = new PipedInputStream();
        UsbIrp irp = createReader(inputStream);
        irp.setData(new byte[size]);
        pipe.asyncSubmit(irp);
        return inputStream;
    }

    @Override
    public PipedInputStream syncReadPipe(short size) throws UsbException, IOException {
        if(!isPipeOpen) openPipe();
        final PipedInputStream inputStream = new PipedInputStream();
        UsbIrp irp = createReader(inputStream);
        irp.setData(new byte[size]);
        pipe.asyncSubmit(irp);
        irp.waitUntilComplete();
        return inputStream;
    }
}
