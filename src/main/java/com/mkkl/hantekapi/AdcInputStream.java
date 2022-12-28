package com.mkkl.hantekapi;

import javax.usb.UsbDevice;
import javax.usb.UsbPipe;
import java.io.*;

//TODO change it to reader
public class AdcInputStream extends PipedInputStream {

    PipedInputStream pipedInputStream;
    int channelcount = 0;
    int length;

    public AdcInputStream(PipedInputStream pipedInputStream, int channelcount, int length) throws IOException {
        super();
        this.pipedInputStream = pipedInputStream;
        this.channelcount = channelcount;
        this.length = length;
    }

    public byte[] readChannels() throws IOException {
        byte[] bytes = new byte[channelcount];
        for (int i = 0; i < channelcount; i++) {
            bytes[i] = (byte) read();
        }
        return bytes;
    }

    public int availableChannels() throws IOException {return available()/channelcount;}


    public void connect(PipedOutputStream src) throws IOException {
        pipedInputStream.connect(src);
    }

    public synchronized int read() throws IOException {
        length--;
        return pipedInputStream.read();
    }

    public synchronized int read(byte[] b, int off, int len) throws IOException {
        return pipedInputStream.read(b, off, len);
    }

    public synchronized int available() throws IOException {
        return length;
    }

    public void close() throws IOException {
        pipedInputStream.close();
    }
}
