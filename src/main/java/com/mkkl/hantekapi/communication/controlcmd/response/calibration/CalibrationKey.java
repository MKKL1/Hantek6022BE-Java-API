package com.mkkl.hantekapi.communication.controlcmd.response.calibration;

import com.mkkl.hantekapi.constants.VoltageRange;
import java.util.Objects;

public record CalibrationKey(int channelid, boolean isSlow, VoltageRange voltageRange) {
    public CalibrationKey {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalibrationKey that = (CalibrationKey) o;
        return channelid == that.channelid &&
                isSlow == that.isSlow &&
                voltageRange == that.voltageRange;
    }

    @Override
    public int hashCode() {
        return Objects.hash(channelid, isSlow, voltageRange);
    }

    @Override
    public String toString() {
        return "CalibrationKey{" +
                "channelid=" + channelid +
                ", isSlow=" + isSlow +
                ", voltageRange=" + voltageRange +
                '}';
    }
}
