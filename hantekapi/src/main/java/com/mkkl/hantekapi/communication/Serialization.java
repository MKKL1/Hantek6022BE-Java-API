package com.mkkl.hantekapi.communication;

import com.mkkl.hantekapi.communication.controlcmd.response.SerializableData;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Serialization {
    public static <T extends SerializableData> byte[] serialize(T serializableObject) throws IOException {
        return serializableObject.serialize();
    }

    public static <T extends SerializableData> T deserialize(byte[] serializableData, final Class<T> tClass)
            throws IOException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        T instance = tClass.getDeclaredConstructor().newInstance();
        instance.deserialize(serializableData);
        return instance;
    }
}
