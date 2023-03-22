package com.mkkl.hantekapi.devicemanager;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.constants.HantekDevices;
import org.usb4java.LibUsbException;

import java.util.List;
import java.util.NoSuchElementException;

public class HantekDeviceList {
    private final List<HantekDeviceRecord> connections;
    public HantekDeviceList(List<HantekDeviceRecord> connections) {
        this.connections = connections;
    }

    public Oscilloscope getFirstFound(HantekDevices scope) throws LibUsbException {
        return connections
                .stream()
                .filter(x -> x.deviceType() == scope)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Device not found"))
                .oscilloscope();
    }

    public Oscilloscope getFirstFound() {
        return connections
                .stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Device not found"))
                .oscilloscope();
    }

    public List<HantekDeviceRecord> getConnections() {
        return connections;
    }
}
