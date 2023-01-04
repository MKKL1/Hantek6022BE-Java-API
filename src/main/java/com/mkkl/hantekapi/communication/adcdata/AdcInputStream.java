package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.ScopeChannel;

import java.io.*;
import java.util.ArrayList;

public class AdcInputStream extends FilterInputStream{

    private final ChannelManager channelManager;
    private final ArrayList<ScopeChannel> channels;
    private final int packetSize;
    /**
     * Creates a {@code FilterInputStream}
     * by assigning the  argument {@code in}
     * to the field {@code this.in} so as
     * to remember it for later use.
     *  @param in the underlying input stream, or {@code null} if
     *           this instance is to be created without an underlying stream.
     * @param channelManager
     * @param packetSize
     */
    public AdcInputStream(InputStream in, ChannelManager channelManager, int packetSize) {
        super(in);
        this.channelManager = channelManager;
        this.channels = channelManager.getChannels();
        this.packetSize = packetSize;
    }

    public byte[] readRawVoltages() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if((ch1 | ch2) < 0)
            throw new EOFException();
        return new byte[] {(byte) ch1, (byte) ch2};
    }

    public void skipPacket() throws IOException {
        skipNBytes(packetSize);
    }

    public float[] readFormattedVoltages() throws IOException {
        byte[] rawdata = readRawVoltages();
        float[] fdata = new float[2];
        for (int i = 0; i < 2; i++)
            fdata[i] = channels.get(i).formatData(rawdata[i]);
        return fdata;
    }

    public void readToChannels() throws IOException {
        float[] fdata = readFormattedVoltages();
        for (int i = 0; i < 2; i++) {
            channels.get(i).currentData = fdata[i];
        }
    }

    public byte[][] readNRawVoltages(int n) throws IOException {
        byte[][] data = new byte[n][2];
        for (int i = 0; i < n; i++)
            data[i] = readRawVoltages();
        return data;
    }

    public float[][] readNFormattedVoltages(int n) throws IOException {
        float[][] data = new float[n][2];
        for (int i = 0; i < n; i++)
            data[i] = readFormattedVoltages();
        return data;
    }
}
