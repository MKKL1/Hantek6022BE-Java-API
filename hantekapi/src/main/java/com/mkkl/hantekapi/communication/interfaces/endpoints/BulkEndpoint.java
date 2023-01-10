package com.mkkl.hantekapi.communication.interfaces.endpoints;

import com.mkkl.hantekapi.communication.UsbConnectionConst;
import com.mkkl.hantekapi.communication.adcdata.AdcDataListener;

import javax.usb.UsbConst;
import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbIrp;

public class BulkEndpoint extends Endpoint{
    public BulkEndpoint(UsbInterface usbInterface) {
        super(UsbConnectionConst.BULK_ENDPOINT_ADDRESS, usbInterface);
        if(usbEndpoint.getType() != UsbConst.ENDPOINT_TYPE_BULK) throw new RuntimeException("Endpoint is not of type bulk");
    }

    @Override
    public void asyncReadPipe(short size, AdcDataListener adcDataListener) throws UsbException {
        if(!isPipeOpen) openPipe();
        UsbIrp irp = createAsyncReader(size, adcDataListener);
        irp.setData(new byte[size]);
        pipe.asyncSubmit(irp);
    }

    @Override
    public byte[] syncReadPipe(short size) throws UsbException {
        if(!isPipeOpen) openPipe();
        UsbIrp irp = pipe.createUsbIrp();
        byte[] data = new byte[size];
        irp.setData(data);
        pipe.syncSubmit(irp);
        return irp.getData();
    }
}
