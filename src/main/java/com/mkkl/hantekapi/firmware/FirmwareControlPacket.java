package com.mkkl.hantekapi.firmware;

import java.util.Arrays;
import java.util.Objects;

public record FirmwareControlPacket(byte size, short address, byte[] data) {
    public FirmwareControlPacket {
        Objects.requireNonNull(data);
    }

    @Override
    public String toString() {
        return "a" + address + ":l" + size + ":d" +Arrays.toString(data);
    }
}
