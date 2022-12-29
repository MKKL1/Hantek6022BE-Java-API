package com.mkkl.hantekapi.channel;

import java.util.Arrays;

public class ChannelManager {
    private final int channelCount;
    private final ScopeChannel[] scopeChannels;

    public ChannelManager(int channelCount) {
        this.channelCount = channelCount;
        scopeChannels = new ScopeChannel[channelCount];
        for (int i = 0; i < channelCount; i++) {
            scopeChannels[i] = new ScopeChannel(i);
        }
    }

    public ScopeChannel[] getActiveChannels() {
        return Arrays.stream(scopeChannels).filter(ScopeChannel::isActive).toArray(ScopeChannel[]::new);
    }

    public int getActiveChannelCount() {
        return getActiveChannels().length;
    }

    public ScopeChannel[] getChannels() {
        return scopeChannels;
    }

    public ScopeChannel getChannel(int id) {
        return scopeChannels[id];
    }

    public int getChannelCount() {
        return channelCount;
    }
}
