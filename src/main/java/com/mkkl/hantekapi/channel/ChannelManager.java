package com.mkkl.hantekapi.channel;

public class ChannelManager {
    private final byte channelCount;
    private ScopeChannel[] scopeChannels;
    private byte activeChannels = 0;

    public ChannelManager(byte channelCount) {
        this.channelCount = channelCount;
        scopeChannels = new ScopeChannel[channelCount];
        for (int i = 0; i < channelCount; i++) {
            scopeChannels[i] = new ScopeChannel((byte) i);
        }
    }

    public ScopeChannel[] getChannels() {
        return scopeChannels;
    }

    public ScopeChannel getChannel(int id) {
        return scopeChannels[id];
    }

    public byte getChannelCount() {
        return channelCount;
    }
}
