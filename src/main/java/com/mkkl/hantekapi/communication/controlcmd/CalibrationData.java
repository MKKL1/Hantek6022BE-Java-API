package com.mkkl.hantekapi.communication.controlcmd;

import com.mkkl.hantekapi.channel.Channels;
import com.mkkl.hantekapi.constants.VoltageRange;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;

public class CalibrationData implements Serializable {
    public HashMap<Channels, HashMap<VoltageRange, Byte>> offsets = new HashMap<>();
    public HashMap<Channels, HashMap<VoltageRange, Byte>> offsets_extended = new HashMap<>();
    public HashMap<Channels, HashMap<VoltageRange, Byte>> gains = new HashMap<>();

    public byte getOffset(Channels channelId, VoltageRange voltageRange){
        return offsets.get(channelId).get(voltageRange);
    }

    public byte getRawExtendedOffset(Channels channelId, VoltageRange voltageRange){
        return offsets_extended.get(channelId).get(voltageRange);
    }

    public byte getRawGain(Channels channelId, VoltageRange voltageRange){
        return offsets.get(channelId).get(voltageRange);
    }

    public float getFormattedExtOffset(Channels channelId, VoltageRange voltageRange) {
        byte ext = getRawExtendedOffset(channelId, voltageRange);
        if(ext != (byte)0 && ext != (byte)255)
            return (getOffset(channelId, voltageRange) + (ext-128)/250f);
        else return getOffset(channelId, voltageRange);
    }

    public byte getFormattedGain(Channels channelId, VoltageRange voltageRange){
        return gains.get(channelId).get(voltageRange);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {

    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {

    }

    private void readObjectNoData() throws ObjectStreamException {

    }
}
