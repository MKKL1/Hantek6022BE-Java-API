package com.mkkl.hantekapi;

import java.util.Collection;

public class Firmware {
    public FirmwareControlPacket[] firmwareData;

    public Firmware(FirmwareControlPacket[] firmwareData) {
        this.firmwareData = firmwareData;
    }

    public Firmware(Collection<FirmwareControlPacket> collection) {
        this.firmwareData = collection.toArray(new FirmwareControlPacket[collection.size()]);
    }
}

