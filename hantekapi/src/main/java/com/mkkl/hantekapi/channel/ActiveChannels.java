package com.mkkl.hantekapi.channel;

/**
 * When channel is active it can capture data.
 * One channel(CH2) can be disabled to increase channel(CH1) sample rate.<br>
 * There are only 2 states device supports:
 * <ul>
 *      <li>CH1 enabled and CH2 disabled</li>
 *      <li>CH1 and CH2 enabled</li>
 * </ul>
 */
public enum ActiveChannels {
    CH1(1),
    CH1CH2(2);

    private final int activeCount;

    ActiveChannels(int activeCount) {
        this.activeCount = activeCount;
    }

    /**
     * @return Count of active channels, 2 for CH1 and CH2 active and 1 for CH1 active
     */
    public int getActiveCount() {
        return activeCount;
    }

    public boolean isSingleMode() {
        return activeCount==1;
    }
}
