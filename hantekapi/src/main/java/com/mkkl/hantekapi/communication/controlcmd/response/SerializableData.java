package com.mkkl.hantekapi.communication.controlcmd.response;

import java.io.IOException;

public abstract class SerializableData {
    public SerializableData() {

    }
    public abstract byte[] serialize() throws IOException;
    public abstract void deserialize(byte[] data) throws IOException;
}
