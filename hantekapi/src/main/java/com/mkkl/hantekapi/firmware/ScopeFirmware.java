package com.mkkl.hantekapi.firmware;

import java.util.Arrays;
import java.util.Collection;

public class ScopeFirmware {
    private final FirmwareControlPacket[] firmwareData;

    public ScopeFirmware(FirmwareControlPacket[] firmwareData) {
        this.firmwareData = firmwareData;
    }

    public ScopeFirmware(Collection<FirmwareControlPacket> firmwareData) {
        this(firmwareData.toArray(FirmwareControlPacket[]::new));
    }

    public FirmwareControlPacket[] getFirmwareData() {
        FirmwareControlPacket[] packets = new FirmwareControlPacket[firmwareData.length+2];
        System.arraycopy(firmwareData, 0,  packets,1, firmwareData.length);
        packets[0] = new FirmwareControlPacket((byte) 1, (short)0xe600, new byte[]{0x01});
        packets[packets.length-1] = new FirmwareControlPacket((byte) 1, (short)0xe600, new byte[]{0x00});
        return packets;
    }

    @Override
    public String toString() {
        return "FirmwareData=" + Arrays.toString(firmwareData);
    }
}
