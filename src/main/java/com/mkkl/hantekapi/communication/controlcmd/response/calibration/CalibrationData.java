package com.mkkl.hantekapi.communication.controlcmd.response.calibration;

import com.mkkl.hantekapi.channel.Channels;
import com.mkkl.hantekapi.communication.controlcmd.response.SerializableData;
import com.mkkl.hantekapi.constants.VoltageRange;

import java.io.IOException;
import java.util.HashMap;

public class CalibrationData extends SerializableData {
    public HashMap<CalibrationKey, Byte> offsets;
    public HashMap<CalibrationKey, Byte> offsets_extended;
    public HashMap<CalibrationKey, Byte> gains;

    public CalibrationData() {
        offsets = new HashMap<>();
        offsets_extended = new HashMap<>();
        gains = new HashMap<>();
    }

    public void setFormattedOffset(Channels channelId, VoltageRange voltageRange, boolean lowspeed, float value) {
        CalibrationKey key = new CalibrationKey(channelId.getChannelId(), lowspeed, voltageRange);
        byte raw = (byte) (Math.round(value)-128);
        offsets.put(key, raw);
        offsets_extended.put(key, (byte)(((value-raw)*250)-128));
    }

    public void setFormattedGain(Channels channelId, VoltageRange voltageRange, float value) {
        CalibrationKey key = new CalibrationKey(channelId.getChannelId(), true, voltageRange);
        gains.put(key, (byte)(((value-1)*500)-128));
    }

    public byte getOffset(Channels channelId, VoltageRange voltageRange, boolean lowspeed) {
        return (byte) (offsets.getOrDefault(new CalibrationKey(channelId.getChannelId(), lowspeed, voltageRange), (byte)-128) + 128);
    }

    public byte getRawExtendedOffset(Channels channelId, VoltageRange voltageRange, boolean lowspeed) {
        return (byte) (offsets_extended.getOrDefault(new CalibrationKey(channelId.getChannelId(), lowspeed, voltageRange), (byte)-128) + 128);
    }

    public byte getRawGain(Channels channelId, VoltageRange voltageRange, boolean lowspeed){
        return gains.getOrDefault(new CalibrationKey(channelId.getChannelId(), lowspeed, voltageRange), (byte)0);
    }

    public float getFormattedOffset(Channels channelId, VoltageRange voltageRange, boolean lowspeed) {
        byte ext = getRawExtendedOffset(channelId, voltageRange, lowspeed);
        if(ext != (byte)0 && ext != (byte)255)
            return (getOffset(channelId, voltageRange, lowspeed) + ext/250f);
        else return getOffset(channelId, voltageRange, lowspeed);
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
            map.put(voltageRange, getFormattedOffset(channelId, voltageRange, true));
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
                calibrationRaw[i + v.getEepromOffset()] = offsets.getOrDefault(new CalibrationKey(i, true, v), (byte)-128);

        for (VoltageRange v: VoltageRange.values())
            for(int i = 0; i < 2; i++)
                calibrationRaw[i+16 + v.getEepromOffset()] = offsets.getOrDefault(new CalibrationKey(i, false, v), (byte)-128);

        for (VoltageRange v: VoltageRange.values())
            for(int i = 0; i < 2; i++)
                calibrationRaw[i+32 + v.getEepromGain()] = gains.getOrDefault(new CalibrationKey(i, true, v), (byte)-128);

        for (VoltageRange v: VoltageRange.values())
            for(int i = 0; i < 2; i++)
                calibrationRaw[i+48 + v.getEepromOffset()] = offsets_extended.getOrDefault(new CalibrationKey(i, true, v), (byte)-128);

        for (VoltageRange v: VoltageRange.values())
            for(int i = 0; i < 2; i++)
                calibrationRaw[i+64 + v.getEepromOffset()] = offsets_extended.getOrDefault(new CalibrationKey(i, false, v), (byte)-128);
        return calibrationRaw;
    }

    @Override
    public void deserialize(byte[] data) throws IOException {
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

    @Override
    public String toString() {
        String s = "CalibrationData" + System.lineSeparator();
        for(Channels channel : Channels.values()) {
            s += " " + channel.name() + ":" + System.lineSeparator();
            s += "  Offsets low speed:" + System.lineSeparator();

            for (VoltageRange voltageRange : VoltageRange.values())
                s += "   " + voltageRange.name() + ": " + getFormattedOffset(channel, voltageRange, true) + System.lineSeparator();

            if(channel == Channels.CH1) {
                s += "  Offsets high speed:" + System.lineSeparator();
                for (VoltageRange voltageRange : VoltageRange.values())
                    s += "   " + voltageRange.name() + ": " + getFormattedOffset(channel, voltageRange, false) + System.lineSeparator();
            }

            s += "  Gains:" + System.lineSeparator();
            for (VoltageRange voltageRange : VoltageRange.values())
                s += "   " + voltageRange.name() + ": " + getFormattedGain(channel, voltageRange) + System.lineSeparator();
        }
        return s;
    }
}

