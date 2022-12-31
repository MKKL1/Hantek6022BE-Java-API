package com.mkkl.hantekapi.channel;

import com.mkkl.hantekapi.constants.VoltageRange;

import javax.usb.UsbException;

public interface VoltageRangeChange {
    void onVoltageChange(VoltageRange newVoltageRange, Channels channelid) throws UsbException;
}
