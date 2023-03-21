package com.mkkl.hantekapi.communication.interfaces.endpoints;

import com.mkkl.hantekapi.communication.UsbConnectionConst;
import org.usb4java.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;


public class BulkEndpoint extends Endpoint{
    public BulkEndpoint(DeviceHandle deviceHandle, InterfaceDescriptor interfaceDescriptor) {
        super(UsbConnectionConst.BULK_ENDPOINT_ADDRESS, deviceHandle, interfaceDescriptor);
        if((endpointDescriptor.bmAttributes() & LibUsb.TRANSFER_TYPE_MASK) != LibUsb.TRANSFER_TYPE_BULK)
            throw new RuntimeException("Endpoint is not of type bulk");
    }

    @Override
    public void asyncReadPipe(short size, TransferCallback callback) throws LibUsbException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(size);
        Transfer transfer = LibUsb.allocTransfer();
        LibUsb.fillBulkTransfer(transfer, deviceHandle, endpointAddress, buffer, callback, null, timeout);
        int result = LibUsb.submitTransfer(transfer);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to submit transfer", result);
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
