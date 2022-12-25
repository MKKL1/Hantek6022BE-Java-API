package com.mkkl;

import java.util.Arrays;

public class FirmwareControlPacket {
    public byte size;
    public short address;
    public byte[] data;

    public FirmwareControlPacket(byte size, short address, byte[] data) {
        this.size = size;
        this.address = address;
        this.data = data;
    }

    @Override
    public String toString() {
        return "size=" + size + " addr=" + address + " data=" + Arrays.toString(data);
    }
}
