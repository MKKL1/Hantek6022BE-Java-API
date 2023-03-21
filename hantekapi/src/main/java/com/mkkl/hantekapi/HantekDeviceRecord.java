package com.mkkl.hantekapi;

import com.mkkl.hantekapi.constants.HantekDevices;
import org.usb4java.Device;

public record HantekDeviceRecord(Device device, Oscilloscope oscilloscope, HantekDevices deviceType) {

}
