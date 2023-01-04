package com.mkkl.hantekapi.channel;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.communication.controlcmd.response.CalibrationData;

import java.util.ArrayList;

/**
 * Used to make managing array of channels easier.
 */
public class ChannelManager {
    private final int channelCount;
    private final ArrayList<ScopeChannel> scopeChannels = new ArrayList<>();
    private int activeChannelCount = 0;

    private ChannelManager(Oscilloscope oscilloscope) {
        this.channelCount = Channels.values().length;

        for(Channels ch : Channels.values())
            scopeChannels.add(ScopeChannel.create(oscilloscope, ch));
    }

    public static ChannelManager create(Oscilloscope oscilloscope) {
        return new ChannelManager(oscilloscope);
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

    public ArrayList<ScopeChannel> getChannels() {
        return scopeChannels;
    }

    //TODO use hashmap instead of arraylist
    /**
     * @param id Channel index, 0 for channel 1 or 1 for channel 2
     */
    public ScopeChannel getChannel(int id) {
        return scopeChannels.get(id);
    }

    public ScopeChannel getChannel(Channels channels) {
        return getChannel(channels.getChannelId());
    }

    public void setCalibration(CalibrationData calibrationData) {
        for(Channels channel : Channels.values()) {
            getChannel(channel).setOffsets(calibrationData.getOffsets(channel));
            getChannel(channel).setGains(calibrationData.getGains(channel));
        }
    }

    /**
     * @return Count of channels that device has.
     */
    public int getChannelCount() {
        return channelCount;
    }
}
