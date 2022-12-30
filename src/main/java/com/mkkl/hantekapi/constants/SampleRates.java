package com.mkkl.hantekapi.constants;

public enum SampleRates {
    SAMPLES_20kS_s(102, 20000),
    SAMPLES_40kS_s(104, 40000),
    SAMPLES_50kS_s(105, 50000),
    SAMPLES_64kS_s(106, 64000),
    SAMPLES_100kS_s(110, 100000),
    SAMPLES_200kS_s(120, 200000),
    SAMPLES_400kS_s(140, 400000),
    SAMPLES_500kS_s(150, 500000),
    SAMPLES_1MS_s(1, 1000000),
    SAMPLES_2MS_s(2, 2000000),
    SAMPLES_3MS_s(3, 3000000),
    SAMPLES_4MS_s(4, 4000000),
    SAMPLES_5MS_s(5, 5000000),
    SAMPLES_6MS_s(6, 6000000),
    SAMPLES_8MS_s(8, 8000000),
    SAMPLES_10MS_s(10, 10000000),
    SAMPLES_12MS_s(12, 12000000),
    SAMPLES_15MS_s(15, 15000000),
    SAMPLES_16MS_s(16, 16000000),
    SAMPLES_24MS_s(24, 24000000),
    SAMPLES_30MS_s(30, 30000000),
    SAMPLES_48MS_s(48, 48000000);

    private final byte rateid;
    private final int sampleCount;

    SampleRates(int rateid, int sampleCount) {
        this.rateid = (byte)rateid;
        this.sampleCount = sampleCount;
    }

    public byte getSampleRateId() {
        return rateid;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public float timeFromPointCount(long pointCount) {
        return pointCount/(float)sampleCount;
    }
}
