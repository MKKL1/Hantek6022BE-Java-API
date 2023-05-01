package com.mkkl.hantekapi.devicemanager;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.constants.HantekDeviceType;
import org.usb4java.Device;

public record HantekDeviceRecord(Device device, Oscilloscope oscilloscope, HantekDeviceType deviceType) {

}
