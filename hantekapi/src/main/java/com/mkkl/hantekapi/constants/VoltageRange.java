package com.mkkl.hantekapi.constants;

public enum VoltageRange {
    RANGE5000mV(1, 5000, 14, 14),
    RANGE2500mV(2, 2500, 8, 8),
    RANGE1000mV(5, 1000, 6, 4),
    RANGE250mV(10, 250, 0, 0);

    private final int gainId;
    private final int gainMiliV;
    private final int eeprom_offset;
    private final int eeprom_gain;

    VoltageRange(int gainId, int gainMiliV, int eeprom_offset, int eeprom_gain) {
        this.gainId = gainId;
        this.gainMiliV = gainMiliV;
        this.eeprom_offset = eeprom_offset;
        this.eeprom_gain = eeprom_gain;
    }

    public int getGainId() {
        return gainId;
    }

    public int getGainMiliV() {
        return gainMiliV;
    }

    public int getEepromOffset() {
        return eeprom_offset;
    }

    public int getEepromGain() {
        return eeprom_gain;
    }
}
