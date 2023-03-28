package com.mkkl.hantekapi.communication.interfaces.endpoints;

import com.mkkl.hantekapi.communication.UsbConnectionConst;
import org.usb4java.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class IsochronousEndpoint extends Endpoint {
    public IsochronousEndpoint(DeviceHandle deviceHandle, InterfaceDescriptor interfaceDescriptor) {
        super(UsbConnectionConst.ISO_ENDPOINT_ADDRESS, deviceHandle, interfaceDescriptor);
        if((endpointDescriptor.bmAttributes() & LibUsb.TRANSFER_TYPE_MASK) != LibUsb.TRANSFER_TYPE_ISOCHRONOUS)
            throw new RuntimeException("Endpoint is not isochronous");
    }

    //TODO isochronous transfer requires number of packets instead of overall size of data
    @Override
    public void asyncReadPipe(short numberOfPackets, TransferCallback callback) throws LibUsbException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(numberOfPackets*getPacketSize());
        Transfer transfer = LibUsb.allocTransfer();
        LibUsb.fillIsoTransfer(transfer, deviceHandle, endpointAddress, buffer, numberOfPackets, callback, null, timeout);
        int result = LibUsb.submitTransfer(transfer);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to submit transfer", result);
    }

    @Override
    public void asyncReadPipe(short size, ByteBuffer byteBuffer, TransferCallback callback) throws LibUsbException {

    }

    @Override
    public ByteBuffer syncReadPipe(short size) throws LibUsbException {
//        ByteBuffer buffer = ByteBuffer.allocateDirect(size);
//        IntBuffer transferred = IntBuffer.allocate(1);
//        int result = LibUsb.(deviceHandle, endpointAddress, buffer, transferred, timeout);
//        if (result != LibUsb.SUCCESS) throw new LibUsbException("Control transfer failed", result);
//        return buffer;
        return null;
    }

    @Override
    public void syncReadPipe(short size, ByteBuffer byteBuffer) throws LibUsbException {

    }
}
