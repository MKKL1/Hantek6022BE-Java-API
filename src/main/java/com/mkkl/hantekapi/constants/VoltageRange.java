package com.mkkl.hantekapi.constants;

public enum VoltageRange {
    RANGE5000mV(1, 14, 14),
    RANGE2500mV(2, 8, 8),
    RANGE1000mV(5, 6, 4),
    RANGE250mV(10, 0, 0);

    private final int gain;
    private final int eeprom_offset;
    private final int eeprom_gain;

    VoltageRange(int gain, int eeprom_offset, int eeprom_gain) {
        this.gain = gain;
        this.eeprom_offset = eeprom_offset;
        this.eeprom_gain = eeprom_gain;
    }

    public int getGain() {
        return gain;
    }

    public int getEepromOffset() {
        return eeprom_offset;
    }

    public int getEepromGain() {
        return eeprom_gain;
    }
}
