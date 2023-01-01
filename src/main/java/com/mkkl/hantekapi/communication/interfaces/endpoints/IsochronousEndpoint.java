package com.mkkl.hantekapi.communication.interfaces.endpoints;

import com.mkkl.hantekapi.communication.UsbConnectionConst;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;

import javax.usb.UsbConst;
import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbIrp;
import java.io.IOException;
import java.io.PipedInputStream;

public class IsochronousEndpoint extends Endpoint {
    public IsochronousEndpoint(UsbInterface usbInterface) {
        super(UsbConnectionConst.ISO_ENDPOINT_ADDRESS, usbInterface);
        if(usbEndpoint.getType() != UsbConst.ENDPOINT_TYPE_ISOCHRONOUS) throw new RuntimeException("Endpoint is not isochronous");
    }

    @Override
    public PipedInputStream asyncReadPipe(short size) throws UsbException, IOException {
        if(!isPipeOpen) openPipe();
        final PipedInputStream inputStream = new PipedInputStream();

        short bytesToRead = size;
        while(bytesToRead > 0) {
            short packetSize = bytesToRead > this.packetSize ? this.packetSize : bytesToRead;
            UsbIrp irp = createReader(inputStream);
            irp.setData(new byte[size]);
            pipe.asyncSubmit(irp);
            bytesToRead -= packetSize;
        }
        return inputStream;
    }

    @Override
    public PipedInputStream syncReadPipe(short size) throws UsbException, IOException {
        if(!isPipeOpen) openPipe();
        final PipedInputStream inputStream = new PipedInputStream();
        short bytesToRead = size;
        while(bytesToRead > 0) {
            short packetSize = bytesToRead > this.packetSize ? this.packetSize : bytesToRead;
            UsbIrp irp = createReader(inputStream);
            irp.setData(new byte[size]);
            pipe.syncSubmit(irp);
            bytesToRead -= packetSize;
        }
        return inputStream;
    }
}
