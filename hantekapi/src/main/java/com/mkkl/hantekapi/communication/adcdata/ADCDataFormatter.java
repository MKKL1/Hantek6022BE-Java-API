package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.ScopeChannel;

import java.util.ArrayList;

//TODO this class needs redesign while keeping it's abstractness
public abstract class ADCDataFormatter {
    protected final ArrayList<ScopeChannel> channels;
    private final boolean singleMode;

    ADCDataFormatter(ChannelManager channelManager, boolean singleMode) {
        channels = channelManager.getChannels();
        this.singleMode = singleMode;
    }

    public static ADCDataFormatter create(ChannelManager channelManager) {
        return ADCDataFormatter.create(channelManager, channelManager.getActiveChannels().isSingleMode());
    }

    public static ADCDataFormatter create(ChannelManager channelManager, boolean singleMode) {
        if(singleMode) return new SingleADCDataFormatter(channelManager);
        else return new DoubleADCDataFormatter(channelManager);
    }

    public abstract float[] formatOneRawSample(byte[] raw);

    public abstract float[] formatRawData(byte[] raw);

    public boolean isSingleMode() {
        return singleMode;
    }
}

class SingleADCDataFormatter extends ADCDataFormatter {

    public SingleADCDataFormatter(ChannelManager channelManager) {
        super(channelManager, true);
    }

    @Override
    public float[] formatOneRawSample(byte[] raw) {
        return new float[] {channels.get(0).formatData(raw[0])};
    }

    @Override
    public float[] formatRawData(byte[] raw) {
        int dataLength = raw.length;
        ScopeChannel channel = channels.get(0);
        float[] formattedData = new float[dataLength];
        for(int i = 0; i < dataLength; i++)
            formattedData[i] = channel.formatData(raw[i]);
        return formattedData;
    }
}

class DoubleADCDataFormatter extends ADCDataFormatter {

    public DoubleADCDataFormatter(ChannelManager channelManager) {
        super(channelManager, false);
    }

    @Override
    public float[] formatOneRawSample(byte[] raw) {
        return new float[] {channels.get(0).formatData(raw[0]), channels.get(1).formatData(raw[1])};
    }

    @Override
    public float[] formatRawData(byte[] raw) {
        int dataLength = raw.length;
        if(dataLength%2 != 0) throw new RuntimeException("In double mode, data length of array should be even");//TODO proper exception
        ScopeChannel channel1 = channels.get(0);
        ScopeChannel channel2 = channels.get(1);
        float[] formattedData = new float[dataLength];
        for(int i = 0; i < dataLength; i+=2) {
            formattedData[i] = channel1.formatData(raw[i]);
            formattedData[i+1] = channel2.formatData(raw[i+1]);
        }
        return formattedData;
    }
}
