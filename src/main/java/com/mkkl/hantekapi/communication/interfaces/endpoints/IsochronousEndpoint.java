package com.mkkl.hantekapi.communication.interfaces.endpoints;

import com.mkkl.hantekapi.communication.UsbConnectionConst;
import com.mkkl.hantekapi.communication.adcdata.AdcDataListener;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;

import javax.usb.UsbConst;
import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbIrp;
import java.io.*;

public class IsochronousEndpoint extends Endpoint {
    public IsochronousEndpoint(UsbInterface usbInterface) {
        super(UsbConnectionConst.ISO_ENDPOINT_ADDRESS, usbInterface);
        if(usbEndpoint.getType() != UsbConst.ENDPOINT_TYPE_ISOCHRONOUS) throw new RuntimeException("Endpoint is not isochronous");
    }

    @Override
    public void asyncReadPipe(short size, AdcDataListener adcDataListener) throws UsbException {
        if(!isPipeOpen) openPipe();

        short bytesToRead = size;
        while(bytesToRead > 0) {
            short packetSize = bytesToRead > this.packetSize ? this.packetSize : bytesToRead;
            UsbIrp irp = createAsyncReader(size, adcDataListener);
            irp.setData(new byte[size]);
            pipe.asyncSubmit(irp);
            bytesToRead -= packetSize;
        }
    }

    @Override
    public byte[] syncReadPipe(short size) throws UsbException, IOException {
        if(!isPipeOpen) openPipe();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(size);
        short bytesToRead = size;
        while(bytesToRead > 0) {
            short packetSize = bytesToRead > this.packetSize ? this.packetSize : bytesToRead;
            UsbIrp irp = pipe.createUsbIrp();
            byte[] data = new byte[size];
            pipe.syncSubmit(irp);
            outputStream.write(data);
            bytesToRead -= packetSize;
        }
        return outputStream.toByteArray();
    }
}
