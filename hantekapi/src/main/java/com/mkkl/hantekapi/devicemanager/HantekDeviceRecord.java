package com.mkkl.hantekapi.devicemanager;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.constants.HantekDevices;
import org.usb4java.Device;

public record HantekDeviceRecord(Device device, Oscilloscope oscilloscope, HantekDevices deviceType) {

}
