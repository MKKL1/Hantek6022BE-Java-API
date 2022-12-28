package com.mkkl.hantekapi.endpoints;

import com.mkkl.hantekapi.controlrequest.UsbConnectionConst;

import javax.usb.UsbInterface;

public class IsochronousEndpoint extends Endpoint{
    public IsochronousEndpoint(UsbInterface usbInterface) {
        super(UsbConnectionConst.ISO_ENDPOINT_ADDRESS, usbInterface);
    }

}
