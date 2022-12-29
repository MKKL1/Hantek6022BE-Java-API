package com.mkkl.hantekapi;

import com.mkkl.hantekapi.adcdata.FormattedDataStream;
import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.constants.VoltageRange;

import javax.usb.UsbException;
import java.io.*;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws UsbException, IOException, InterruptedException {
        OscilloscopeManager.findAllDevices();
        Oscilloscope oscilloscope = OscilloscopeManager.getFirstFound();

        System.out.println(oscilloscope.getDescriptor());

        if(!oscilloscope.isFirmwarePresent()) {
            System.out.println("Flashing firmware");
            oscilloscope.getHantekConnection().flash_firmware();
            Thread.sleep(5000);
            OscilloscopeManager.findAllDevices();
            oscilloscope = OscilloscopeManager.getFirstFound();
            System.out.println(oscilloscope.getDescriptor());
        }

        oscilloscope.setup();
        oscilloscope.getCalibrationValues();
        oscilloscope.setActive(ActiveChannels.CH1CH2);
        oscilloscope.getChannel(0).setVoltageRange(VoltageRange.RANGE1000mV);
        oscilloscope.getChannel(1).setVoltageRange(VoltageRange.RANGE250mV);
        oscilloscope.getChannel(1).setProbeMultiplier(10);
        oscilloscope.getChannel(1).setAdditionalOffset(-5f);

        oscilloscope.open();
        FormattedDataStream input = oscilloscope.getFormattedData();
        while(input.availableChannels() > 0) {
            System.out.print(Arrays.toString(input.readFormattedChannels()) + ",");
        }









        oscilloscope.getHantekConnection().getScopeInterface().getEndpoint().close();

    }
}
