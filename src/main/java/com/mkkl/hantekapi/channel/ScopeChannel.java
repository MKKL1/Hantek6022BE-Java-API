package com.mkkl.hantekapi.channel;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.communication.controlcmd.HantekRequest;
import com.mkkl.hantekapi.constants.VoltageRange;

import javax.usb.UsbException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class ScopeChannel {
    private static final VoltageRange[] voltageRanges = VoltageRange.values();

    private final Oscilloscope oscilloscope;
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

    private VoltageRange currentVoltageRange;
    private int probeMultiplier = 1;
    private float additionalOffset = 0;

    public float currentData;

    private ScopeChannel(Oscilloscope oscilloscope, Channels id) {
        this.oscilloscope = oscilloscope;
        this.id = id;
    }

    public static ScopeChannel create(Oscilloscope oscilloscope, Channels id) {
        return new ScopeChannel(oscilloscope, id);
    }

    public static ScopeChannel create(Oscilloscope oscilloscope, int id) {
        return new ScopeChannel(oscilloscope,
                Arrays.stream(Channels.values())
                        .filter(x -> x.getChannelId() == id)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Id of " + id + "not found in enum")));
    }

    public void setOffsets(HashMap<VoltageRange, Float> newOffsets) {
        this.offsets.putAll(newOffsets);
        recalculate_scalefactor();
    }

    public void setGains(HashMap<VoltageRange, Float> newGains) {
        for(Map.Entry<VoltageRange, Float> entry : newGains.entrySet())
            this.gains.put(entry.getKey(), this.gains.get(entry.getKey()) * entry.getValue());
        recalculate_scalefactor();
    }

    private void recalculate_scalefactor() {
        current_offset = offsets.get(currentVoltageRange);
        current_gain = gains.get(currentVoltageRange);
        current_scale_factor = ((5.12f * probeMultiplier * current_gain) / (float)(currentVoltageRange.getGain() << 7));
    }

    public float formatData(byte rawdata) {
        return ((rawdata+128) - (current_offset + additionalOffset)) * current_scale_factor;
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

    public void setVoltageRange(VoltageRange currentVoltageRange) {
        this.currentVoltageRange = currentVoltageRange;
        recalculate_scalefactor();
        if(id == Channels.CH1) oscilloscope.patch(HantekRequest.getVoltRangeCH1Request((byte) currentVoltageRange.getGain())).onFailureThrow((ex) -> new RuntimeException(ex));
        else oscilloscope.patch(HantekRequest.getVoltRangeCH2Request((byte) currentVoltageRange.getGain())).onFailureThrow((ex) -> new RuntimeException(ex));
    }

    public int getProbeMultiplier() {
        return probeMultiplier;
    }

    public void setProbeMultiplier(int probeMultiplier) {
        this.probeMultiplier = probeMultiplier;
        recalculate_scalefactor();
    }

    public float getAdditionalOffset() {
        return additionalOffset;
    }

    public void setAdditionalOffset(float additionalOffset) {
        this.additionalOffset = additionalOffset;
    }

    @Override
    public String toString() {
        System.out.println(offsets);
        System.out.println(gains);
        return "ScopeChannel{" +
                "id=" + id +
                ", _offset=" + current_offset +
                ", _gain=" + current_gain +
                ", scale_factor=" + current_scale_factor +
                ", currentVoltageRange=" + currentVoltageRange +
                ", probeMultiplier=" + probeMultiplier +
                ", additionalOffset=" + additionalOffset +
                '}';
    }
}
