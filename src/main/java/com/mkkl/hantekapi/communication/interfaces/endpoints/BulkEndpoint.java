package com.mkkl.hantekapi.communication.interfaces.endpoints;

import com.mkkl.hantekapi.communication.UsbConnectionConst;

import javax.usb.UsbConst;
import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbIrp;
import java.io.*;

public class BulkEndpoint extends Endpoint{
    public BulkEndpoint(UsbInterface usbInterface) {
        super(UsbConnectionConst.BULK_ENDPOINT_ADDRESS, usbInterface);
        if(usbEndpoint.getType() != UsbConst.ENDPOINT_TYPE_BULK) throw new RuntimeException("Endpoint is not of type bulk");
    }

    public void asyncReadPipe(OutputStream outputStream, short size) throws UsbException, IOException {
        if(!isPipeOpen) openPipe();
        UsbIrp irp = createAsyncReader(outputStream);
        irp.setData(new byte[size]);
        pipe.asyncSubmit(irp);
    }

    public void syncReadPipe(OutputStream outputStream, short size) throws UsbException, IOException {
        if(!isPipeOpen) openPipe();
        UsbIrp irp = pipe.createUsbIrp();
        byte[] data = new byte[size];
        irp.setData(data);
        pipe.syncSubmit(irp);
        outputStream.write(data);
    }
}
