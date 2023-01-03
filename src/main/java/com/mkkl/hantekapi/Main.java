package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.ActiveChannels;
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

    public static void main(String[] args) {

        Oscilloscope oscilloscope = null;
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter("E:\\programming\\hantek python api\\capture.txt"));
            oscilloscope = OscilloscopeManager.findAndGetFirst(Scopes.DSO6022BE);

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
            oscilloscope.setActiveChannels(ActiveChannels.CH1CH2);
            oscilloscope.setSampleRate(SampleRates.SAMPLES_100kS_s);
            //oscilloscope.getCalibrationValues();
            oscilloscope.getChannel(Channels.CH1).setVoltageRange(VoltageRange.RANGE5000mV);
            oscilloscope.getChannel(Channels.CH2).setVoltageRange(VoltageRange.RANGE5000mV);
            oscilloscope.getChannel(Channels.CH1).setProbeMultiplier(10);
            oscilloscope.getChannel(Channels.CH2).setProbeMultiplier(10);
            //oscilloscope.setCalibrationFrequency(10000);

            //new CalibrateScope(oscilloscope).fastCalibration();
            System.out.println(oscilloscope.getSampleRate().timeFromPointCount(512) + "s");
        } catch (IOException | UsbException | InterruptedException e) {
            e.printStackTrace();
        }

        assert oscilloscope != null;

        ScopeDataReader scopeDataReader = null;
        try {
            scopeDataReader = new ScopeDataReader(oscilloscope);
            CalibrateScope calibrateScope = new CalibrateScope(oscilloscope);
            calibrateScope.fastCalibration(scopeDataReader);
        } catch (UsbException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
                assert scopeDataReader != null;
                scopeDataReader.stopCapture();
                scopeDataReader.close();
            } catch (IOException | UsbException e) {
                e.printStackTrace();
            }

        }

//        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
//        df.setMaximumFractionDigits(340);
//        float a = oscilloscope.getSampleRate().timeFromPointCount(512);
//        float b = 0;
//        int i = 0;
//        FormattedDataStream input = null;
//        ScopeDataReader scopeDataReader = null;
//        try {
//            scopeDataReader = new ScopeDataReader(oscilloscope);
//            input = new FormattedDataStream(scopeDataReader.getAdcInputStream(), oscilloscope.getChannelManager());
//            scopeDataReader.readDataFrame();
//            while(true) {
//                //System.out.print(Arrays.toString(input.readFormattedChannels()) + ",");
//                float[] f = input.readFormattedChannels();
//                System.out.println(Arrays.toString(f));
//                writer.write(df.format(b) + "," + df.format(f[0]) + "," + df.format(f[1]) + System.lineSeparator());
//                b += a;
//                i++;
//            }
//
//        } catch (UsbException | IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                writer.close();
//                assert input != null;
//                input.close();
//                scopeDataReader.stopCapture();
//                scopeDataReader.close();
//            } catch (IOException | UsbException e) {
//                e.printStackTrace();
//            }
//
//        }

    }
}
