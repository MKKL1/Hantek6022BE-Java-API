package com.mkkl.hantekapi.examples;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.OscilloscopeHandle;
import com.mkkl.hantekapi.ScopeUtils;
import com.mkkl.hantekapi.communication.adcdata.AdcInputStream;
import com.mkkl.hantekapi.communication.readers.sync.SyncScopeDataReader;
import com.mkkl.hantekapi.communication.controlcmd.response.calibration.CalibrationData;
import com.mkkl.hantekapi.constants.HantekDeviceType;
import org.usb4java.LibUsbException;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Main {
    //TODO test
    public static void main(String[] args) {
        Oscilloscope oscilloscope = null;
        try {
            oscilloscope = ScopeUtils.getAndFlashFirmware(HantekDeviceType.DSO6022BE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert oscilloscope != null;

        //Setting up connection with device
        OscilloscopeHandle oscilloscopeHandle = oscilloscope.setup();
        //Print descriptor
        System.out.println(oscilloscope.getDescriptor());
        //Read calibration data from device
        CalibrationData calibrationDataFromDevice = oscilloscopeHandle.readCalibrationValues();
        //Print it's deserialized values
        System.out.println("Calibration:" + calibrationDataFromDevice);
        //Set calibration values for this instance
        oscilloscopeHandle.setCalibration(calibrationDataFromDevice);

        //Writing csv file with header time,CH1 voltage,CH2 voltage


        //We have to skip some data, because it's values are corrupted
        short lengthToSkip = 64;
        System.out.println("Measurement time = " + oscilloscopeHandle.getCurrentSampleRate().timeFromPointCount((1024 + 64)/2) + "s");//TODO test if value is accurate
        try(BufferedWriter writer = new BufferedWriter(new FileWriter("capture.csv"))) {
            DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            df.setMaximumFractionDigits(340);
            float a = oscilloscopeHandle.getCurrentSampleRate().timeBetweenTwoPoints();
            float b = 0;
            int i = 0;
            //Creating reader for voltage data
            SyncScopeDataReader syncScopeDataReader = oscilloscopeHandle.createSyncDataReader();

            //Reading data from oscilloscope with given length, 1024 means 512 bytes are read from each channel
            byte[] bytes = syncScopeDataReader.readToByteArray((short) 1024);
            //Creating input stream for formatting output data of oscilloscope data reader
            AdcInputStream input = AdcInputStream.create(new ByteArrayInputStream(bytes), oscilloscopeHandle);
            int readBytes = lengthToSkip;
            //Skipping corrupted data
            input.skipNBytes(lengthToSkip);
            while (readBytes < bytes.length) {
                //Formatting raw data from device to human-readable voltages by calibration values set earlier
                //If you use 10x probe use oscilloscope.getChannel(channel).setProbeMultiplier(10)
                float[] f = input.readFormattedVoltages();
                System.out.printf("CH1=%.2fV CH2=%.2fV\n", f[0], f[1]);
                writer.write(df.format(b) + "," + df.format(f[0]) + "," + df.format(f[1]) + System.lineSeparator());
                b += a;
                i++;
                readBytes += 2;
            }

        } catch (IOException | LibUsbException e) {
            e.printStackTrace();
        } finally {
            try {
                oscilloscopeHandle.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
