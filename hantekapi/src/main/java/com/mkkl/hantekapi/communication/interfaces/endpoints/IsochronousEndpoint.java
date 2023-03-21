package com.mkkl.hantekapi.communication.interfaces.endpoints;

import com.mkkl.hantekapi.communication.UsbConnectionConst;
import org.usb4java.DeviceHandle;
import org.usb4java.InterfaceDescriptor;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

import java.nio.ByteBuffer;

public class IsochronousEndpoint extends Endpoint {
    public IsochronousEndpoint(DeviceHandle deviceHandle, InterfaceDescriptor interfaceDescriptor) {
        super(UsbConnectionConst.ISO_ENDPOINT_ADDRESS, deviceHandle, interfaceDescriptor);
        if((endpointDescriptor.bmAttributes() & LibUsb.TRANSFER_TYPE_MASK) != LibUsb.TRANSFER_TYPE_ISOCHRONOUS)
            throw new RuntimeException("Endpoint is not isochronous");
    }

    @Override
    public void asyncReadPipe(short size, AdcDataListener adcDataListener) throws LibUsbException {
    }

    @Override
    public ByteBuffer syncReadPipe(short size) throws LibUsbException {
        return null;
    }
}
