package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.channel.ChannelManager;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class SingleModeADCStream extends AdcInputStream {
    public SingleModeADCStream(InputStream in, ChannelManager channelManager, int packetSize) {
        super(in, channelManager, packetSize);
    }

    @Override
    public byte[] readRawVoltages() throws IOException {
        int ch1 = read();
        if(ch1 < 0) throw new EOFException();
        return new byte[] {(byte) ch1};
    }

    @Override
    public float[] readFormattedVoltages() throws IOException {
        return new float[] {channels.get(0).formatData(readRawVoltages()[0])};
    }
}
