package com.mkkl.hantekapi.endpoints;

import com.mkkl.hantekapi.controlrequest.UsbConnectionConst;

import javax.usb.UsbException;
import javax.usb.UsbInterface;
import javax.usb.UsbIrp;
import javax.usb.UsbPipe;
import javax.usb.event.UsbPipeDataEvent;
import javax.usb.event.UsbPipeErrorEvent;
import javax.usb.event.UsbPipeListener;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;

public class BulkEndpoint extends Endpoint{
    public BulkEndpoint(UsbInterface usbInterface) {
        super(UsbConnectionConst.BULK_ENDPOINT_ADDRESS, usbInterface);
    }

}
