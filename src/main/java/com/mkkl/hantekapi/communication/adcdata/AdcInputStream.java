package com.mkkl.hantekapi.communication.adcdata;

import java.io.*;

//TODO PipedInputStream can be replaced with BlockingQueue

/**
 * Basic input stream for data from oscilloscope.
 * It is used to divide data to chunks of length {@link #channelcount}
 */
public class AdcInputStream extends PipedInputStream{

    final int packetsize;
    final int channelcount;

    public AdcInputStream(PipedOutputStream src, int packetsize, int channelcount) throws IOException {
        super(src);
        this.packetsize = packetsize;
        this.channelcount = channelcount;
    }

    /**
     * Reads n bytes from stream where n {@link AdcInputStream#channelcount} given in constructor
     * @return Byte array of data where each column is raw data for channel of id by ascending order eg. [channel0data, channel1data]
     * @throws IOException {@link #read()}
     */

    public byte[] readChannels() throws IOException {
        byte[] bytes = new byte[channelcount];
        for (int i = 0; i < channelcount; i++) {
            int b = read();
            if (b == -1) return null;
            bytes[i] = (byte)b;
        }
        return bytes;
    }
    public void skipPacket() throws IOException {
        skipNBytes(packetsize);
    }
}
