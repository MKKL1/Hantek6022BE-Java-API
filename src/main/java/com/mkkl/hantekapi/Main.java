package com.mkkl.hantekapi;

import com.mkkl.hantekapi.firmware.Firmwares;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws UsbException, UnsupportedEncodingException {
        OscilloscopeManager.findAllDevices();
        Oscilloscope oscilloscope = OscilloscopeManager.getFirstFound();

        System.out.println(oscilloscope.getScopeDevice().getProductString());
        oscilloscope.readCalibrationValues();

    }
}
