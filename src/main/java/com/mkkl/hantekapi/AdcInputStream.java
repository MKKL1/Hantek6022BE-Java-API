package com.mkkl.hantekapi;

import javax.usb.UsbDevice;
import javax.usb.UsbPipe;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class AdcInputStream extends ByteArrayInputStream {

    int channelcount = 0;
    public AdcInputStream(byte[] data, int channelcount) {
        super(data);
        this.channelcount = channelcount;
    }

    public byte[] readChannels() throws IOException {
        return readNBytes(channelcount);
    }

    public int availableChannels() {return available()/channelcount;}
}
