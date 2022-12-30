package com.mkkl.hantekapi;

import com.mkkl.hantekapi.adcdata.FormattedDataStream;
import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.constants.SampleRates;
import com.mkkl.hantekapi.constants.VoltageRange;

import javax.usb.UsbException;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;

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
        oscilloscope.open();
        oscilloscope.setActive(ActiveChannels.CH1CH2);
        oscilloscope.setSampleRate(SampleRates.SAMPLES_200kS_s);
        oscilloscope.getCalibrationValues();
        oscilloscope.getChannel(0).setVoltageRange(VoltageRange.RANGE2500mV);
        oscilloscope.getChannel(1).setVoltageRange(VoltageRange.RANGE5000mV);
        oscilloscope.getChannel(0).setProbeMultiplier(10);
//        oscilloscope.getChannel(1).setAdditionalOffset(-5f);
        System.out.println(oscilloscope.getChannel(0).toString());
        System.out.println(oscilloscope.getSampleRate().timeFromPointCount(0x400) + "s");
        BufferedWriter writer = new BufferedWriter(new FileWriter("E:\\programming\\hantek python api\\capture.txt"));
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setMaximumFractionDigits(340);
        float a = 0.00005f;
        float b = 0;
        FormattedDataStream input = oscilloscope.getFormattedData();
        while(input.availableChannels() > 0) {
            System.out.print(Arrays.toString(input.readFormattedChannels()) + ",");
            float[] f = input.readFormattedChannels();
            writer.write(df.format(b) + "," + df.format(f[0]) + "," + df.format(f[1]) + System.lineSeparator());
            b += a;
        }
        writer.close();









        oscilloscope.getHantekConnection().getScopeInterface().getEndpoint().close();

    }
}
