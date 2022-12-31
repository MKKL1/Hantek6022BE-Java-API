package com.mkkl.hantekapi.communication.interfaces.endpoints;

import com.mkkl.hantekapi.communication.UsbConnectionConst;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;

import javax.usb.UsbConst;
import javax.usb.UsbInterface;

public class IsochronousEndpoint extends Endpoint {
    public IsochronousEndpoint(UsbInterface usbInterface) {
        super(UsbConnectionConst.ISO_ENDPOINT_ADDRESS, usbInterface);
        if(usbEndpoint.getType() != UsbConst.ENDPOINT_TYPE_ISOCHRONOUS) throw new RuntimeException("Endpoint is not isochronous");
    }

}
