package com.mkkl.hantekapi.communication.interfaces.endpoints;

import com.mkkl.hantekapi.communication.UsbConnectionConst;

import javax.usb.UsbConst;
import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbIrp;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class BulkEndpoint extends Endpoint{
    public BulkEndpoint(UsbInterface usbInterface) {
        super(UsbConnectionConst.BULK_ENDPOINT_ADDRESS, usbInterface);
        if(usbEndpoint.getType() != UsbConst.ENDPOINT_TYPE_BULK) throw new RuntimeException("Endpoint is not of type bulk");
    }

    public void asyncReadPipe(PipedOutputStream outputStream, short size) throws UsbException, IOException {
        if(!isPipeOpen) openPipe();
        UsbIrp irp = createReader(outputStream);
        irp.setData(new byte[size]);
        pipe.asyncSubmit(irp);
    }

    public void syncReadPipe(PipedOutputStream outputStream, short size) throws UsbException, IOException {
        if(!isPipeOpen) openPipe();
        UsbIrp irp = createReader(outputStream);
        irp.setData(new byte[size]);
        pipe.syncSubmit(irp);
    }
}
