package com.mkkl.hantekapi.communication.interfaces.endpoints;

import com.mkkl.hantekapi.communication.UsbConnectionConst;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;

import javax.usb.UsbConst;
import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbIrp;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class IsochronousEndpoint extends Endpoint {
    public IsochronousEndpoint(UsbInterface usbInterface) {
        super(UsbConnectionConst.ISO_ENDPOINT_ADDRESS, usbInterface);
        if(usbEndpoint.getType() != UsbConst.ENDPOINT_TYPE_ISOCHRONOUS) throw new RuntimeException("Endpoint is not isochronous");
    }

    public void asyncReadPipe(OutputStream outputStream, short size) throws UsbException, IOException {
        if(!isPipeOpen) openPipe();

        short bytesToRead = size;
        while(bytesToRead > 0) {
            short packetSize = bytesToRead > this.packetSize ? this.packetSize : bytesToRead;
            UsbIrp irp = createAsyncReader(outputStream);
            irp.setData(new byte[size]);
            pipe.asyncSubmit(irp);
            bytesToRead -= packetSize;
        }
    }

    public void syncReadPipe(OutputStream outputStream, short size) throws UsbException, IOException {
        if(!isPipeOpen) openPipe();
        short bytesToRead = size;
        while(bytesToRead > 0) {
            short packetSize = bytesToRead > this.packetSize ? this.packetSize : bytesToRead;
            UsbIrp irp = pipe.createUsbIrp();
            byte[] data = new byte[size];
            pipe.syncSubmit(irp);
            outputStream.write(data);
            bytesToRead -= packetSize;
        }
    }
}
