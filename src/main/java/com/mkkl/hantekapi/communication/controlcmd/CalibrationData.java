package com.mkkl.hantekapi.communication.controlcmd;

import com.mkkl.hantekapi.channel.Channels;
import com.mkkl.hantekapi.constants.VoltageRange;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

public class CalibrationData implements Serializable {
    public HashMap<CalibrationKey, Byte> offsets;
    public HashMap<CalibrationKey, Byte> offsets_extended;
    public HashMap<CalibrationKey, Byte> gains;

    public byte getOffset(Channels channelId, VoltageRange voltageRange, boolean lowspeed){
        return offsets.get(new CalibrationKey(channelId.getChannelId(), lowspeed, voltageRange));
    }

    public byte getRawExtendedOffset(Channels channelId, VoltageRange voltageRange, boolean lowspeed){
        return offsets_extended.get(new CalibrationKey(channelId.getChannelId(), lowspeed, voltageRange));
    }

    public byte getRawGain(Channels channelId, VoltageRange voltageRange, boolean lowspeed){
        return gains.get(new CalibrationKey(channelId.getChannelId(), lowspeed, voltageRange));
    }

    public float getFormattedExtOffset(Channels channelId, VoltageRange voltageRange, boolean lowspeed) {
        byte ext = getRawExtendedOffset(channelId, voltageRange, lowspeed);
        if(ext != (byte)0 && ext != (byte)255)
            return (getOffset(channelId, voltageRange, lowspeed) + (ext+128)/250f);
        else return getOffset(channelId, voltageRange, lowspeed)+128;
    }

    public float getFormattedGain(Channels channelId, VoltageRange voltageRange) {
        byte b = getRawGain(channelId, voltageRange, true);
        if(b != (byte)0 && b != (byte)255)
            return 1 + (b+128)/500f;
        else return 1f;
    }

    public HashMap<VoltageRange, Float> getOffsets(Channels channelId) {
        HashMap<VoltageRange, Float> map = new HashMap<>();
        for(VoltageRange voltageRange: VoltageRange.values())
            map.put(voltageRange, getOffset(channelId, voltageRange, true) + getFormattedExtOffset(channelId, voltageRange, true));
        return map;
    }

    public HashMap<VoltageRange, Float> getGains(Channels channelId) {
        HashMap<VoltageRange, Float> map = new HashMap<>();
        for(VoltageRange voltageRange: VoltageRange.values())
            map.put(voltageRange, getFormattedGain(channelId, voltageRange));
        return map;
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        byte[] calibrationRaw = new byte[80];
        for (VoltageRange v: VoltageRange.values())
            for(int i = 0; i < 2; i++)
                calibrationRaw[i + v.getEepromOffset()] = offsets.get(new CalibrationKey(i, true, v));

        for (VoltageRange v: VoltageRange.values())
            for(int i = 0; i < 2; i++)
                calibrationRaw[i+16 + v.getEepromOffset()] = offsets.get(new CalibrationKey(i, false, v));

        for (VoltageRange v: VoltageRange.values())
            for(int i = 0; i < 2; i++)
                calibrationRaw[i+32 + v.getEepromGain()] = gains.get(new CalibrationKey(i, true, v));

        for (VoltageRange v: VoltageRange.values())
            for(int i = 0; i < 2; i++)
                calibrationRaw[i+48 + v.getEepromOffset()] = offsets_extended.get(new CalibrationKey(i, true, v));

        for (VoltageRange v: VoltageRange.values())
            for(int i = 0; i < 2; i++)
                calibrationRaw[i+64 + v.getEepromOffset()] = offsets_extended.get(new CalibrationKey(i, false, v));

        out.write(calibrationRaw);
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        offsets = new HashMap<>();
        offsets_extended = new HashMap<>();
        gains = new HashMap<>();
        byte[] calibrationRaw = in.readAllBytes();
        if(calibrationRaw.length < 80) throw new ClassNotFoundException("Failed to serialize calibration data (data too short)");
        for (VoltageRange v: VoltageRange.values()) {
            offsets.put(new CalibrationKey(0, true, v), calibrationRaw[v.getEepromOffset()]);
            offsets.put(new CalibrationKey(1, true, v), calibrationRaw[1 + v.getEepromOffset()]);
        }

        for (VoltageRange v: VoltageRange.values()) {
            offsets.put(new CalibrationKey(0, false, v), calibrationRaw[16 + v.getEepromOffset()]);
            offsets.put(new CalibrationKey(1, false, v), calibrationRaw[17 + v.getEepromOffset()]);
        }

        for (VoltageRange v: VoltageRange.values()) {
            gains.put(new CalibrationKey(0, true, v), calibrationRaw[32 + v.getEepromGain()]);
            gains.put(new CalibrationKey(1, true, v), calibrationRaw[33 + v.getEepromGain()]);
        }

        for (VoltageRange v: VoltageRange.values()) {
            offsets_extended.put(new CalibrationKey(0, true, v), calibrationRaw[48 + v.getEepromOffset()]);
            offsets_extended.put(new CalibrationKey(1, true, v), calibrationRaw[49 + v.getEepromOffset()]);
        }

        for (VoltageRange v: VoltageRange.values()) {
            offsets_extended.put(new CalibrationKey(0, false, v), calibrationRaw[64 + v.getEepromOffset()]);
            offsets_extended.put(new CalibrationKey(1, false, v), calibrationRaw[65 + v.getEepromOffset()]);
        }
    }

    private void readObjectNoData() throws ObjectStreamException {

    }
}

record CalibrationKey(int channelid, boolean isSlow, VoltageRange voltageRange){
    CalibrationKey {
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
}
