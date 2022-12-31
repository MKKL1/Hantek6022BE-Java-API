package com.mkkl.hantekapi.channel;

import javax.usb.UsbException;

/**
 * Used to make managing array of channels easier.
 */
public class ChannelManager {
    private final int channelCount;
    private final ScopeChannel[] scopeChannels;
    private int activeChannelCount = 0;

    public ChannelManager(int channelCount, VoltageRangeChange voltageRangeChangeEvent) throws UsbException {
        this.channelCount = channelCount;
        scopeChannels = new ScopeChannel[channelCount];
        Channels[] channelsList = Channels.values();
        for (int i = 0; i < channelCount; i++) {
            scopeChannels[i] = new ScopeChannel(channelsList[i], voltageRangeChangeEvent);
        }
    }

    //TODO this method shouldn't be accessed from oscilloscope object
    /**
     * Sets number of active channels for future reference.
     * <b>Doesn't actually send information to device.</b>
     * You probably want to use {@link com.mkkl.hantekapi.Oscilloscope#setActiveChannels(ActiveChannels)}
     */
    public void setActiveChannelCount(int activeChannelCount) {
        this.activeChannelCount = activeChannelCount;
    }

    /**
     * @return Count of active channels, 2 or 1.
     * @see ActiveChannels
     */
    public int getActiveChannelCount() {
        return activeChannelCount;
    }

    public ScopeChannel[] getChannels() {
        return scopeChannels;
    }

    /**
     * @param id Channel index, 0 for channel 1 or 1 for channel 2
     */
    public ScopeChannel getChannel(int id) {
        return scopeChannels[id];
    }

    public ScopeChannel getChannel(Channels channels) {
        return getChannel(channels.getChannelId());
    }

    /**
     * @return Count of channels that device has.
     */
    public int getChannelCount() {
        return channelCount;
    }
}
