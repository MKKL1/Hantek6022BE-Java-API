package com.mkkl.hantekapi.channel;

import javax.usb.UsbException;
import java.util.Arrays;
import java.util.stream.Stream;

public class ChannelManager {
    private final int channelCount;
    private final ScopeChannel[] scopeChannels;
    private int activeChannelCount;

    public ChannelManager(int channelCount, VoltageRangeChange voltageRangeChangeEvent) throws UsbException {
        this.channelCount = channelCount;
        scopeChannels = new ScopeChannel[channelCount];
        for (int i = 0; i < channelCount; i++) {
            scopeChannels[i] = new ScopeChannel(i, voltageRangeChangeEvent);
        }
        activeChannelCount = channelCount;
    }

    public void setActiveChannelCount(int activeChannelCount) {
        this.activeChannelCount = activeChannelCount;
    }

    public int getActiveChannelCount() {
        return activeChannelCount;
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
