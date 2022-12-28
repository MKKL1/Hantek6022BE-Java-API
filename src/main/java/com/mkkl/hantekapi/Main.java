package com.mkkl.hantekapi;

import com.mkkl.hantekapi.constants.Scopes;
import com.mkkl.hantekapi.firmware.Firmwares;

import javax.usb.UsbDevice;
import javax.usb.UsbException;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws UsbException, IOException, InterruptedException {
        OscilloscopeManager.findAllDevices();
        Oscilloscope oscilloscope = OscilloscopeManager.getFirstFound();

        System.out.println(oscilloscope.getScopeDevice().getProductString());

        if(!oscilloscope.isFirmwarePresent()) {
            oscilloscope.getFirmwareUploader().flash_firmware();
            Thread.sleep(5000);
            OscilloscopeManager.findAllDevices();
            oscilloscope = OscilloscopeManager.getFirstFound();
            System.out.println(oscilloscope.getScopeDevice().getProductString());
        }

        oscilloscope.getCalibrationValues();

        oscilloscope.open();
        FormattedDataStream input = oscilloscope.getFormattedData();
        while(input.availableChannels() > 0) {
            System.out.print(Arrays.toString(input.readFormattedChannels()) + ",");
        }

        oscilloscope.close();

    }
}
