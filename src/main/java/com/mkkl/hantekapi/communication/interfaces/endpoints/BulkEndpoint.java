package com.mkkl.hantekapi.communication.interfaces.endpoints;

import com.mkkl.hantekapi.communication.UsbConnectionConst;

import javax.usb.UsbInterface;

public class BulkEndpoint extends Endpoint{
    public BulkEndpoint(UsbInterface usbInterface) {
        super(UsbConnectionConst.BULK_ENDPOINT_ADDRESS, usbInterface);
    }

}
