package com.mkkl.hantekapi.communication.adcdata;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Basic input stream for data from oscilloscope.
 * It is used to divide data to chunks of length {@link #channelcount}
 */
public class AdcInputStream {

    PipedInputStream pipedInputStream;
    final int channelcount;
    int length;
    public final int packetsize;

    /**
     * @param pipedInputStream input stream from usb endpoint
     * @param channelcount count of active channels
     * @param length expected length of data to be read
     * @param packetsize
     */
    public AdcInputStream(PipedInputStream pipedInputStream, int channelcount, int length, int packetsize) {
        super();
        this.pipedInputStream = pipedInputStream;
        this.channelcount = channelcount;
        this.length = length;
        this.packetsize = packetsize;
    }

    /**
     * Reads n bytes from stream where n {@link AdcInputStream#channelcount} given in constructor
     * @return Byte array of data where each column is raw data for channel of id by ascending order eg. [channel0data, channel1data]
     * @throws IOException {@link #read()}
     */
    public byte[] readChannels() throws IOException {
        byte[] bytes = new byte[channelcount];
        for (int i = 0; i < channelcount; i++) {
            bytes[i] = (byte) read();
        }
        return bytes;
    }
    public void skipPacket() throws IOException {
        skip(packetsize);
    }


    /**
     * To be used with methods that divide data to chunks
     * @return count of available data chunks to be received
     * @throws IOException {@link #available()}
     */
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


    public byte[] readNBytes(int len) throws IOException {
        byte[] bytes = pipedInputStream.readNBytes(len);
        length -= len;
        return bytes;
    }

    public int readNBytes(byte[] b, int off, int len) throws IOException {
        int res = pipedInputStream.readNBytes(b, off, len);
        length -= len;
        return res;
    }

    public long skip(long n) throws IOException {
        long res = pipedInputStream.skip(n);
        length -= n;
        return res;
    }

    public void skipNBytes(long n) throws IOException {
        pipedInputStream.skipNBytes(n);
        length -= n;
    }

    public void close() throws IOException {
        pipedInputStream.close();
    }
}
