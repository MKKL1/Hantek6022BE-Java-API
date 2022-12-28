package com.mkkl.hantekapi.constants;

public enum VoltageRange {
    RANGE5000mV(1),
    RANGE2500mV(2),
    RANGE1000mV(5),
    RANGE250mV(10);

    //TODO name?
    private final int gain;

    VoltageRange(int gain) {
        this.gain = gain;
    }

    public int getGain() {
        return gain;
    }
}
