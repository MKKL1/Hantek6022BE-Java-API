package com.mkkl.hantekapi.communication.controlcmd.response;

import com.mkkl.hantekapi.channel.Channels;
import com.mkkl.hantekapi.constants.VoltageRange;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class CalibrationData extends SerializableData {
    public HashMap<CalibrationKey, Byte> offsets;
    public HashMap<CalibrationKey, Byte> offsets_extended;
    public HashMap<CalibrationKey, Byte> gains;

    public int getOffset(Channels channelId, VoltageRange voltageRange, boolean lowspeed){
        return offsets.get(new CalibrationKey(channelId.getChannelId(), lowspeed, voltageRange)) + 128;
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
            map.put(voltageRange, getFormattedExtOffset(channelId, voltageRange, true));
        return map;
    }

    public HashMap<VoltageRange, Float> getGains(Channels channelId) {
        HashMap<VoltageRange, Float> map = new HashMap<>();
        for(VoltageRange voltageRange: VoltageRange.values())
            map.put(voltageRange, getFormattedGain(channelId, voltageRange));
        return map;
    }

    @Override
    public byte[] serialize() throws IOException{
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
        return calibrationRaw;
    }

    @Override
    public void deserialize(byte[] data) throws IOException {
        offsets = new HashMap<>();
        offsets_extended = new HashMap<>();
        gains = new HashMap<>();
        if(data.length < 80) throw new IOException("Failed to serialize calibration data (data too short)");
        for (VoltageRange v: VoltageRange.values())
            for(int i = 0; i < 2; i++)
                offsets.put(new CalibrationKey(i, true, v), data[i+v.getEepromOffset()]);

        for (VoltageRange v: VoltageRange.values())
            for(int i = 0; i < 2; i++)
                offsets.put(new CalibrationKey(i, false, v), data[16 + i + v.getEepromOffset()]);

        for (VoltageRange v: VoltageRange.values())
            for(int i = 0; i < 2; i++)
                gains.put(new CalibrationKey(i, true, v), data[32 + i + v.getEepromGain()]);

        for (VoltageRange v: VoltageRange.values())
            for(int i = 0; i < 2; i++)
                offsets_extended.put(new CalibrationKey(i, true, v), data[48 + i + v.getEepromOffset()]);

        for (VoltageRange v: VoltageRange.values())
            for(int i = 0; i < 2; i++)
                offsets_extended.put(new CalibrationKey(i, false, v), data[64 + i + v.getEepromOffset()]);
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
