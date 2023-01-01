package com.mkkl.hantekapi;

import com.mkkl.hantekapi.communication.adcdata.FormattedDataStream;
import com.mkkl.hantekapi.channel.Channels;
import com.mkkl.hantekapi.communication.adcdata.ScopeDataReader;
import com.mkkl.hantekapi.communication.interfaces.SupportedInterfaces;
import com.mkkl.hantekapi.constants.SampleRates;
import com.mkkl.hantekapi.constants.Scopes;
import com.mkkl.hantekapi.constants.VoltageRange;

import javax.usb.UsbException;
import java.io.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;

public class Main {

    public static void main(String[] args) throws IOException, UsbException, InterruptedException {


        Oscilloscope oscilloscope = OscilloscopeManager.findAndGetFirst(Scopes.DSO6022BE);

        System.out.println(oscilloscope.getDescriptor());

        if(!oscilloscope.isFirmwarePresent()) {
            System.out.println("Flashing firmware");
            oscilloscope.flash_firmware();
            Thread.sleep(5000);
            OscilloscopeManager.findSupportedDevices();
            oscilloscope = OscilloscopeManager.getFirstFound();
            System.out.println(oscilloscope.getDescriptor());
        }

        System.out.println(oscilloscope.getScopeDevice().getActiveUsbConfiguration().getUsbInterface((byte)0).getSettings());
        oscilloscope.setup();
        //oscilloscope.setActiveChannels(ActiveChannels.CH1CH2);
        oscilloscope.setSampleRate(SampleRates.SAMPLES_200kS_s);
        oscilloscope.getCalibrationValues();
        oscilloscope.getChannel(Channels.CH1).setVoltageRange(VoltageRange.RANGE2500mV);
        oscilloscope.getChannel(Channels.CH2).setVoltageRange(VoltageRange.RANGE5000mV);
        oscilloscope.getChannel(Channels.CH1).setProbeMultiplier(10);
        oscilloscope.setCalibrationFrequency(10000);
        System.out.println(oscilloscope.getSampleRate().timeFromPointCount(512) + "s");

        BufferedWriter writer = new BufferedWriter(new FileWriter("E:\\programming\\hantek python api\\capture.txt"));
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setMaximumFractionDigits(340);
        float a = oscilloscope.getSampleRate().timeFromPointCount(512);
        float b = 0;
        int i = 0;
        try(ScopeDataReader scopeDataReader = new ScopeDataReader(oscilloscope)) {
            FormattedDataStream input = new FormattedDataStream(scopeDataReader.asyncReadDataFrame(), oscilloscope.getChannelManager());
            while(input.available() > 0) {
                //System.out.print(Arrays.toString(input.readFormattedChannels()) + ",");
                float[] f = input.readFormattedChannels();
                System.out.println(Arrays.toString(f));
                writer.write(df.format(b) + "," + df.format(f[0]) + "," + df.format(f[1]) + System.lineSeparator());
                b += a;
                i++;
            }
            writer.close();
            input.close();
            scopeDataReader.stopCapture();
        } catch (UsbException e) {
            e.printStackTrace();
        }
        System.out.println(i);

    }
}
