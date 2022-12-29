package com.mkkl.hantekapi.channel;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.constants.VoltageRange;

import java.util.HashMap;


public class ScopeChannel {
    private static final VoltageRange[] voltageRanges = VoltageRange.values();
    public static final byte[] calibrationOffsets = {0, 6, 8, 14};
    public static final byte[] calibrationGainOff = {0, 4, 8, 14};

    private final int id;
    private final HashMap<VoltageRange, Float> offsets = new HashMap<>();
    private final HashMap<VoltageRange, Float> gains;

    //Save data from hashmap to variable each time a voltage range is changed
    private float _offset;
    private float _gain;
    private float scale_factor = 1;

    private boolean active;
    private VoltageRange currentVoltageRange;
    private int probeMultiplier = 1;
    private float additionalOffset = 0;

    public float currentData;

    public ScopeChannel(int id) {
        this.id = id;
        for(VoltageRange range : voltageRanges) offsets.put(range, 0f);
        gains = new HashMap<>(){{
                put(VoltageRange.RANGE5000mV, 1.01f);
                put(VoltageRange.RANGE2500mV, 1.01f);
                put(VoltageRange.RANGE1000mV, 0.99f);
                put(VoltageRange.RANGE250mV, 1f);}};

        setVoltageRange(VoltageRange.RANGE5000mV);
    }



    public void setOffsets(float[] newoffsets) {
        for (int i = 0; i < newoffsets.length; i++) {
            offsets.put(voltageRanges[i], newoffsets[i]);
        }
    }

    public void setGains(float[] newoffsets) {
        for (int i = 0; i < newoffsets.length; i++) {
            gains.put(voltageRanges[i], newoffsets[i]);
        }
    }

    private void recalculate_scalefactor() {
        scale_factor = ((5.12f * probeMultiplier * _gain) / (float)(currentVoltageRange.getGain() << 7));
    }

    public float formatData(byte rawdata) {
        return ((rawdata&0xFF) - (_offset + additionalOffset)) * scale_factor;
    }

    public int getId() {
        return id;
    }

    public HashMap<VoltageRange, Float> getOffsets() {
        return offsets;
    }

    public HashMap<VoltageRange, Float> getGains() {
        return gains;
    }

    public void setActive() {
        this.active = true;
    }

    public void setActive(boolean state) {
        this.active = state;
    }

    public boolean isActive() {
        return active;
    }

    public void setVoltageRange(VoltageRange currentVoltageRange) {
        this.currentVoltageRange = currentVoltageRange;
        _offset = offsets.get(currentVoltageRange);
        _gain = gains.get(currentVoltageRange);
        recalculate_scalefactor();
    }

    public VoltageRange getVoltageRange() {
        return currentVoltageRange;
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
        return "ScopeChannel{" +
                "id=" + id+1 +
                ", offsets=" + offsets +
                ", gains=" + gains +
                '}';
    }
}
