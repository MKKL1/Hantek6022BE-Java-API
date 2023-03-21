package com.mkkl.hantekapi.communication.interfaces.endpoints;

import com.mkkl.hantekapi.communication.UsbConnectionConst;
import org.usb4java.DeviceHandle;
import org.usb4java.InterfaceDescriptor;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;


public class BulkEndpoint extends Endpoint{
    public BulkEndpoint(DeviceHandle deviceHandle, InterfaceDescriptor interfaceDescriptor) {
        super(UsbConnectionConst.BULK_ENDPOINT_ADDRESS, deviceHandle, interfaceDescriptor);
        if((endpointDescriptor.bmAttributes() & LibUsb.TRANSFER_TYPE_MASK) != LibUsb.TRANSFER_TYPE_BULK)
            throw new RuntimeException("Endpoint is not of type bulk");
    }

    @Override
    public void asyncReadPipe(short size, AdcDataListener adcDataListener) throws LibUsbException {

    }

    @Override
    public ByteBuffer syncReadPipe(short size) throws LibUsbException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(size);
        IntBuffer transferred = IntBuffer.allocate(1);
        int result = LibUsb.bulkTransfer(deviceHandle, endpointAddress, buffer, transferred, timeout);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Control transfer failed", result);
        return buffer;
    }
}
