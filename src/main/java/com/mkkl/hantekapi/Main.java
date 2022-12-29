package com.mkkl.hantekapi;

import com.mkkl.hantekapi.adcdata.FormattedDataStream;
import com.mkkl.hantekapi.channel.ScopeChannel;

import javax.usb.UsbException;
import java.io.*;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws UsbException, IOException, InterruptedException {
        OscilloscopeManager.findAllDevices();
        Oscilloscope oscilloscope = OscilloscopeManager.getFirstFound();

        System.out.println(oscilloscope.getScopeDevice().getProductString());

        if(!oscilloscope.isFirmwarePresent()) {
            System.out.println("Flashing firmware");
            oscilloscope.getFirmwareUploader().flash_firmware();
            Thread.sleep(5000);
            OscilloscopeManager.findAllDevices();
            oscilloscope = OscilloscopeManager.getFirstFound();
            System.out.println(oscilloscope.getScopeDevice().getProductString());
        }

        oscilloscope.getCalibrationValues();

        oscilloscope.open();
        oscilloscope.getChannel(0).setActive();
        oscilloscope.getChannel(1).setActive();
        FormattedDataStream input = oscilloscope.getFormattedData();
        while(input.availableChannels() > 0) {
            System.out.print(Arrays.toString(input.readFormattedChannels()) + ",");
        }
        oscilloscope.getScopeUsbConnection().getScopeInterface().getEndpoint().close();

    }
}
