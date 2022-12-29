package com.mkkl.hantekapi.channel;

public enum ActiveChannels {
    CH1(1),
    CH1CH2(2);

    private final int activeCount;

    ActiveChannels(int activeCount) {
        this.activeCount = activeCount;
    }

    public int getActiveCount() {
        return activeCount;
    }
}
