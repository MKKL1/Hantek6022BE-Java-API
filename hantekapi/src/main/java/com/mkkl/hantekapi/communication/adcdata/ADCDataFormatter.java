package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.ScopeChannel;

import java.util.ArrayList;

public class ADCDataFormatter {
    private final ArrayList<ScopeChannel> channels;

    public ADCDataFormatter(ChannelManager channelManager) {
        channels = channelManager.getChannels();
    }

    public float[] formatSample(byte ch1raw, byte ch2raw) {
        float[] data = new float[2];
        data[0] = channels.get(0).formatData(ch1raw);
        data[1] = channels.get(1).formatData(ch2raw);
        return data;
    }

    public float[] formatSample(byte[] raw) {
        return formatSample(raw[0], raw[1]);
    }
}
