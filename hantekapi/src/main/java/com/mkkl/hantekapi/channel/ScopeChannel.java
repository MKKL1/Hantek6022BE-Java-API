package com.mkkl.hantekapi.channel;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.OscilloscopeHandle;
import com.mkkl.hantekapi.communication.controlcmd.HantekRequestFactory;
import com.mkkl.hantekapi.constants.VoltageRange;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class representing channel of oscilloscope.
 * Used to store calibration data and to format raw ADC data.<br>
 * Stored data:
 * <ul>
 *      <li>Offsets</li>
 *      <li>Gains</li>
 *      <li>Probe multiplier</li>
 *      <li>Selected voltage range</li>
 * </ul>
 */
public class ScopeChannel {
    private final OscilloscopeHandle oscilloscopeHandle;
    private final ChannelManager channelManager;
    private final Channels id;

    private final HashMap<VoltageRange, Float> offsets = new HashMap<>(){{
        put(VoltageRange.RANGE5000mV, 0f);
        put(VoltageRange.RANGE2500mV, 0f);
        put(VoltageRange.RANGE1000mV, 0f);
        put(VoltageRange.RANGE250mV, 0f);}};

    private final HashMap<VoltageRange, Float> gains = new HashMap<>(){{
        put(VoltageRange.RANGE5000mV, 1.01f);
        put(VoltageRange.RANGE2500mV, 1.01f);
        put(VoltageRange.RANGE1000mV, 0.99f);
        put(VoltageRange.RANGE250mV, 1f);}};

    //Save data from hashmap to variable each time a voltage range is changed
    private float current_offset;
    private float current_gain;
    private float current_scale_factor = 1;
    private VoltageRange currentVoltageRange = VoltageRange.RANGE5000mV;
    private int probeMultiplier = 1;
    private float additionalOffset = 0;

    //Used to temporarily save data from AdcInputStream for reading from channel
    public float currentData;

    private ScopeChannel(OscilloscopeHandle oscilloscopeHandle,ChannelManager channelManager, Channels id) {
        this.oscilloscopeHandle = oscilloscopeHandle;
        this.channelManager = channelManager;
        this.id = id;
        recalculate_scalefactor();
    }

    public static ScopeChannel create(OscilloscopeHandle oscilloscopeHandle, ChannelManager channelManager, Channels id) {
        return new ScopeChannel(oscilloscopeHandle,channelManager, id);
    }

    public static ScopeChannel create(OscilloscopeHandle oscilloscopeHandle,ChannelManager channelManager, int id) {
        return new ScopeChannel(oscilloscopeHandle,channelManager,
                Arrays.stream(Channels.values())
                        .filter(x -> x.getChannelId() == id)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Id of " + id + "not found in enum")));
    }

    /**
     * Sets offsets from formatted {@link com.mkkl.hantekapi.communication.controlcmd.response.calibration.CalibrationData#offsets}
     * @param newOffsets hash map of offset value for each voltage range
     */
    public void setOffsets(HashMap<VoltageRange, Float> newOffsets) {
        this.offsets.putAll(newOffsets);
        recalculate_scalefactor();
    }

    /**
     * Sets gains from formatted {@link com.mkkl.hantekapi.communication.controlcmd.response.calibration.CalibrationData#gains}
     * @param newGains hash map of gain value for each voltage range
     */
    public void setGains(HashMap<VoltageRange, Float> newGains) {
        for(Map.Entry<VoltageRange, Float> entry : newGains.entrySet())
            this.gains.put(entry.getKey(), this.gains.get(entry.getKey()) * entry.getValue());
        recalculate_scalefactor();
    }

    /**
     * Formats raw ADC data to formatted voltage data, using calibration data set on this channel.
     */
    public float formatData(byte rawData) {
        return ((rawData+128) - (current_offset + additionalOffset)) * current_scale_factor;
    }

    /**
     * Sets voltage range on channel
     */
    public void setVoltageRange(VoltageRange currentVoltageRange) {
        this.currentVoltageRange = currentVoltageRange;
        recalculate_scalefactor();
        if(id == Channels.CH1)
            oscilloscopeHandle.patch(HantekRequestFactory.getVoltRangeCH1Request((byte) currentVoltageRange.getGainId()))
                    .onFailureThrow((ex) -> new RuntimeException(ex));
        else oscilloscopeHandle.patch(HantekRequestFactory.getVoltRangeCH2Request((byte) currentVoltageRange.getGainId()))
                .onFailureThrow((ex) -> new RuntimeException(ex));
    }

    public void setVoltageRange(int gainId) {
        setVoltageRange(Arrays.stream(VoltageRange.values()).filter(x -> x.getGainId() == gainId).findFirst().orElseThrow());
    }

    /**
     * Sets probe multiplier factor.
     * @param probeMultiplier any int (most likely 1 or 10)
     */
    public void setProbeMultiplier(int probeMultiplier) {
        this.probeMultiplier = probeMultiplier;
        recalculate_scalefactor();
    }

    /**
     * Sets additional offset separate from calibration values.
     * Offsets are subtracted from base value
     */
    public void setAdditionalOffset(float additionalOffset) {
        this.additionalOffset = additionalOffset;
    }

    /**
     * Check if channel was set active
     */
    public boolean isActive() {
        ActiveChannels activeChannels = this.channelManager.getActiveChannels();
        if (activeChannels == ActiveChannels.CH1 && this.id == Channels.CH1) return true;
        else return activeChannels == ActiveChannels.CH1CH2 && (this.id == Channels.CH1 || this.id == Channels.CH2);
    }

    public Channels getId() {
        return id;
    }

    public HashMap<VoltageRange, Float> getOffsets() {
        return offsets;
    }

    public HashMap<VoltageRange, Float> getGains() {
        return gains;
    }

    public VoltageRange getVoltageRange() {
        return currentVoltageRange;
    }

    public int getProbeMultiplier() {
        return probeMultiplier;
    }


    public float getAdditionalOffset() {
        return additionalOffset;
    }

    private void recalculate_scalefactor() {
        current_offset = offsets.get(currentVoltageRange);
        current_gain = gains.get(currentVoltageRange);
        current_scale_factor = ((5.12f * probeMultiplier * current_gain) / (float)(currentVoltageRange.getGainId() << 7));
    }

    @Override
    public String toString() {
        return "ScopeChannel{" +
                "id=" + id +
                ", current_offset=" + current_offset +
                ", current_gain=" + current_gain +
                ", current_scale_factor=" + current_scale_factor +
                ", currentVoltageRange=" + currentVoltageRange.name() +
                ", probeMultiplier=" + probeMultiplier +
                ", additionalOffset=" + additionalOffset +
                '}';
    }
}
