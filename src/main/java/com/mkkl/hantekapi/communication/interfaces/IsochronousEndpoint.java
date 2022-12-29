package com.mkkl.hantekapi.communication.interfaces;

import com.mkkl.hantekapi.communication.UsbConnectionConst;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;

import javax.usb.UsbInterface;

public class IsochronousEndpoint extends Endpoint {
    public IsochronousEndpoint(UsbInterface usbInterface) {
        super(UsbConnectionConst.ISO_ENDPOINT_ADDRESS, usbInterface);
    }

}
