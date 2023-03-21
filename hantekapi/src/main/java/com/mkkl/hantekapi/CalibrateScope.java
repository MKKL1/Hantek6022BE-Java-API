package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.Channels;
import com.mkkl.hantekapi.communication.adcdata.ScopeDataReader;
import com.mkkl.hantekapi.communication.controlcmd.response.calibration.CalibrationData;
import com.mkkl.hantekapi.constants.SampleRates;
import com.mkkl.hantekapi.constants.VoltageRange;
import org.usb4java.LibUsbException;

import java.io.IOException;

public class CalibrateScope {

    /**
     * Calculates calibration values.
     * Use only when 0V are applied to both channels
     */
    public static CalibrationData fastCalibration(Oscilloscope oscilloscope,ScopeDataReader reader) {
        CalibrationData calibrationData = new CalibrationData();
        try {
            for (VoltageRange voltageRange : VoltageRange.values()) {
                oscilloscope.getChannel(Channels.CH1).setVoltageRange(voltageRange);
                oscilloscope.getChannel(Channels.CH2).setVoltageRange(voltageRange);
                oscilloscope.setSampleRate(SampleRates.SAMPLES_100kS_s);
                Thread.sleep(100);
                System.out.println("Low speed " + voltageRange.name());
                float[] lowspeed = ScopeUtils.readRawAverages(oscilloscope, reader, (short) (512*12), 10);
                calibrationData.setFormattedOffset(Channels.CH1, voltageRange, true, lowspeed[0]+128);
                calibrationData.setFormattedOffset(Channels.CH2, voltageRange, true, lowspeed[1]+128);

                Thread.sleep(100);
                oscilloscope.setSampleRate(SampleRates.SAMPLES_30MS_s);
                Thread.sleep(100);
                System.out.println("High speed " + voltageRange.name());
                calibrationData.setFormattedOffset(Channels.CH1, voltageRange, false,
                        ScopeUtils.readRawAverages(oscilloscope, reader, (short) (512*12), 2)[0]+128);
            }
            return calibrationData;
        } catch (LibUsbException | InterruptedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
