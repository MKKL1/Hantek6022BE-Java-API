package com.mkkl.hantekapi.constants;

public enum SampleRate {
    SAMPLES_20kS_s(102, 20000, false),
    SAMPLES_40kS_s(104, 40000, false),
    SAMPLES_50kS_s(105, 50000, false),
    SAMPLES_64kS_s(106, 64000, false),
    SAMPLES_100kS_s(110, 100000, false),
    SAMPLES_200kS_s(120, 200000, false),
    SAMPLES_400kS_s(140, 400000, false),
    SAMPLES_500kS_s(150, 500000, false),
    SAMPLES_1MS_s(1, 1000000, false),
    SAMPLES_2MS_s(2, 2000000, false),
    SAMPLES_3MS_s(3, 3000000, false),
    SAMPLES_4MS_s(4, 4000000, false),
    SAMPLES_5MS_s(5, 5000000, false),
    SAMPLES_6MS_s(6, 6000000, false),
    SAMPLES_8MS_s(8, 8000000, false),
    SAMPLES_10MS_s(10, 10000000, false),
    SAMPLES_12MS_s(12, 12000000, false),
    SAMPLES_15MS_s(15, 15000000, false),
    SAMPLES_16MS_s(16, 16000000, false),
    SAMPLES_24MS_s(24, 24000000, false),
    SAMPLES_30MS_s(30, 30000000, true),
    SAMPLES_48MS_s(48, 48000000, true);

    private final byte rateid;
    private final int sampleCount;
    private final boolean singleChannel;

    SampleRate(int rateid, int sampleCount, boolean singleChannel) {
        this.rateid = (byte)rateid;
        this.sampleCount = sampleCount;
        this.singleChannel = singleChannel;
    }

    public byte getSampleRateId() {
        return rateid;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public boolean isSingleChannel() {
        return singleChannel;
    }

    public float timeFromPointCount(long pointCount) {
        return pointCount/(float)sampleCount;
    }

    public float timeBetweenTwoPoints() {
        return 1/(float)sampleCount;
    }
}
