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
        asyncReadPipe(size, buffer, callback);
    }

    @Override
    public void asyncReadPipe(short size, ByteBuffer byteBuffer, TransferCallback callback) throws LibUsbException {
        Transfer transfer = LibUsb.allocTransfer();
        LibUsb.fillBulkTransfer(transfer, deviceHandle, endpointAddress, byteBuffer, callback, null, timeout);
        int result = LibUsb.submitTransfer(transfer);
        if (result < 0) throw new LibUsbException("Unable to submit transfer", result);
    }

    @Override
    public ByteBuffer syncReadPipe(short size) throws LibUsbException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(size);
        syncReadPipe(size, buffer);
        return buffer;
    }

    @Override
    public void syncReadPipe(short size, ByteBuffer byteBuffer) throws LibUsbException {
        IntBuffer transferred = IntBuffer.allocate(1);
        int result = LibUsb.bulkTransfer(deviceHandle, endpointAddress, byteBuffer, transferred, timeout);
        if (result < 0) throw new LibUsbException("Control transfer failed", result);
    }
}
