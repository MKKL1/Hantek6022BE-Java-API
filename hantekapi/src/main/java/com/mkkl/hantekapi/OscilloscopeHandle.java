package com.mkkl.hantekapi;

public class OscilloscopeHandle {
    private final Oscilloscope oscilloscope;

    public OscilloscopeHandle(Oscilloscope oscilloscope) {
        this.oscilloscope = oscilloscope;
    }

    public Oscilloscope getOscilloscope() {
        return oscilloscope;
    }
}
