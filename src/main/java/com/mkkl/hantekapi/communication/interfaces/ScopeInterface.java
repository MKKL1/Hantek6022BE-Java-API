package com.mkkl.hantekapi.communication.interfaces;

import com.mkkl.hantekapi.communication.interfaces.endpoints.BulkEndpoint;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;
import com.mkkl.hantekapi.communication.interfaces.endpoints.EndpointTypes;
import com.mkkl.hantekapi.communication.interfaces.endpoints.IsochronousEndpoint;

import javax.usb.*;

public class ScopeInterface implements AutoCloseable{
    private Endpoint endpoint;
    private UsbInterface usbInterface;
    private final UsbDevice usbDevice;

    public ScopeInterface(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
    }

    public void setInterface(SupportedInterfaces supportedInterfaces) {
        this.usbInterface = usbDevice.getActiveUsbConfiguration().getUsbInterface(supportedInterfaces.getInterfaceId());
        if(supportedInterfaces.getEndpointType() == EndpointTypes.Bulk)
            this.endpoint = new BulkEndpoint(usbInterface);
        else if(supportedInterfaces.getEndpointType() == EndpointTypes.Iso)
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
