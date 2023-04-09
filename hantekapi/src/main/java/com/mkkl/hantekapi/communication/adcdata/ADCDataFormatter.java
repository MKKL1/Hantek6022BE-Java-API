package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.ScopeChannel;

import java.util.ArrayList;

public class ADCDataFormatter {
    private final ArrayList<ScopeChannel> channels;
    private final boolean singleMode;

    public ADCDataFormatter(ChannelManager channelManager) {
        channels = channelManager.getChannels();
        singleMode = channelManager.getActiveChannels().singleMode();
    }

    public float[] formatSample(byte[] raw) {
        int rawLength = raw.length;
        float[] formatted = new float[rawLength];
        formatted[0] = channels.get(0).formatData(raw[0]);
        if(rawLength == 2) formatted[1] = channels.get(1).formatData(raw[1]);
        return formatted;
    }

    public boolean isSingleMode() {
        return singleMode;
    }
}
