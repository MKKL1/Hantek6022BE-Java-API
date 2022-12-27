package com.mkkl.hantekapi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScopeChannel {
    public static final VoltageRange[] voltageRanges = {VoltageRange.RANGE5000mV,VoltageRange.RANGE2500mV,VoltageRange.RANGE1000mV,VoltageRange.RANGE250mV};
    public static final byte[] calibrationOffsets = {0, 6, 8, 14};
    public static final byte[] calibrationGainOff = {0, 4, 8, 14};

    public final byte id;
    private final HashMap<VoltageRange, Float> offsets = new HashMap<>();
    private final HashMap<VoltageRange, Float> gains;

    public ScopeChannel(byte id) {
        this.id = id;
        for(VoltageRange range : voltageRanges) offsets.put(range, 0f);
        gains = new HashMap<>(){{
                put(VoltageRange.RANGE5000mV, 1.01f);
                put(VoltageRange.RANGE2500mV, 1.01f);
                put(VoltageRange.RANGE1000mV, 0.99f);
                put(VoltageRange.RANGE250mV, 1f);}};
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

    public HashMap<VoltageRange, Float> getOffsets() {
        return offsets;
    }

    public HashMap<VoltageRange, Float> getGains() {
        return gains;
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
