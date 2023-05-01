package com.mkkl.hantekapi.channel;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.OscilloscopeHandle;
import com.mkkl.hantekapi.communication.controlcmd.HantekRequestFactory;
import com.mkkl.hantekapi.communication.controlcmd.response.calibration.CalibrationData;
import com.mkkl.hantekapi.exceptions.UncheckedUsbException;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Used to make managing list of channels easier.
 */
public class ChannelManager {
    private final int channelCount;
    private final ArrayList<ScopeChannel> scopeChannels = new ArrayList<>(); //TODO can be replaced with array or hashmap
    private ActiveChannels activeChannels = ActiveChannels.CH1CH2;
    private final OscilloscopeHandle oscilloscopeHandle;

    private ChannelManager(OscilloscopeHandle oscilloscopeHandle) {
        this.channelCount = Channels.values().length;
        this.oscilloscopeHandle = oscilloscopeHandle;
        for(Channels ch : Channels.values())
            scopeChannels.add(ScopeChannel.create(oscilloscopeHandle,this, ch));
        //Making absolute sure that [CH1, CH2]
        scopeChannels.sort(Comparator.comparingInt(o -> o.getId().getChannelId()));
    }

    public static ChannelManager create(OscilloscopeHandle oscilloscopeHandle) {
        return new ChannelManager(oscilloscopeHandle);
    }

    /**
     * Sets number of active channels for future reference.
     */
    public void setActiveChannelCount(ActiveChannels activeChannels) {
        oscilloscopeHandle.patch(HantekRequestFactory.getChangeChCountRequest((byte) activeChannels.getActiveCount()))
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to set active channels ", ex))
                .onSuccess(() -> this.activeChannels = activeChannels);
    }

    public ActiveChannels getActiveChannels() {
        return activeChannels;
    }

    public ArrayList<ScopeChannel> getChannels() {
        return scopeChannels;
    }

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
     * @return Count of channels that device has. (2)
     */
    public int getChannelCount() {
        return channelCount;
    }
}
