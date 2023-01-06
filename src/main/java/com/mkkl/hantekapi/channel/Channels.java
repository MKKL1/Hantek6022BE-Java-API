package com.mkkl.hantekapi.channel;

/**
 * Channels supported by oscilloscope.
 * CH1 - id 0, CH2 - id 1
 */
public enum Channels {
    CH1(0),
    CH2(1);

    private final int channel_id;

    Channels(int channel_id) {
        this.channel_id = channel_id;
    }

    public int getChannelId() {
        return channel_id;
    }
}
