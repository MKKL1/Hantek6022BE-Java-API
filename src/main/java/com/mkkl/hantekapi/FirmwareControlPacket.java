package com.mkkl.hantekapi;

import java.util.Arrays;

public class FirmwareControlPacket {
    public byte size;
    public short value;
    public byte[] data;

    public FirmwareControlPacket(byte size, short address, byte[] data) {
        this.size = size;
        this.value = address;
        this.data = data;
    }

    @Override
    public String toString() {
        return "size=" + size + " addr=" + value + " data=" + Arrays.toString(data);
    }
}
