package com.mkkl.hantekapi.exceptions;

public class DeviceNotInitialized extends RuntimeException {
    public DeviceNotInitialized() {
        super("Oscilloscope was not initialized. Use Oscilloscope.setup()");
    }

    public DeviceNotInitialized(String message) {
        super(message);
    }
}
