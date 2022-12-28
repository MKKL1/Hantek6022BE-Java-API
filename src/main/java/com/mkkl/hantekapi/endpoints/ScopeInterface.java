package com.mkkl.hantekapi.endpoints;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import javax.usb.UsbInterface;

public class ScopeInterface implements AutoCloseable{
    private Endpoint endpoint;
    private UsbInterface usbInterface;
    private final UsbDevice usbDevice;

    public ScopeInterface(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
    }

    public void setInterface(ScopeInterfaces scopeInterfaces) {
        this.usbInterface = usbDevice.getActiveUsbConfiguration().getUsbInterface(scopeInterfaces.getInterfaceId());
        if(scopeInterfaces.getEndpointType() == EndpointTypes.Bulk)
            this.endpoint = new BulkEndpoint(usbInterface);
        else if(scopeInterfaces.getEndpointType() == EndpointTypes.Iso)
            this.endpoint = new IsochronousEndpoint(usbInterface);
        //shouldn't be here
        else throw new RuntimeException("Endpoint type not supported");
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public UsbInterface getUsbInterface() {
        return usbInterface;
    }

    public void claim() throws UsbException {
        usbInterface.claim();
    }

    @Override
    public void close() throws UsbException {
        usbInterface.release();
    }
}
