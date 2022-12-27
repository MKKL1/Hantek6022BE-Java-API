package com.mkkl.hantekapi;

import javax.usb.UsbDevice;
import javax.usb.UsbPipe;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AdcInputStream extends ByteArrayInputStream {

    public AdcInputStream(byte[] data) {
        super(data);
    }
}
