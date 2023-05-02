package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.Channels;
import com.mkkl.hantekapi.communication.readers.sync.SyncScopeDataReader;
import com.mkkl.hantekapi.communication.controlcmd.response.calibration.CalibrationData;
import com.mkkl.hantekapi.constants.SampleRate;
import com.mkkl.hantekapi.constants.VoltageRange;
import org.usb4java.LibUsbException;

import java.io.IOException;

public class CalibrateScope {

    /**
     * Calculates calibration values.
     * Use only when 0V are applied to both channels
     */
    public static CalibrationData fastCalibration(OscilloscopeHandle oscilloscopeHandle, SyncScopeDataReader reader) {
        CalibrationData calibrationData = new CalibrationData();
        try {
            for (VoltageRange voltageRange : VoltageRange.values()) {
                oscilloscopeHandle.getChannel(Channels.CH1).setVoltageRange(voltageRange);
                oscilloscopeHandle.getChannel(Channels.CH2).setVoltageRange(voltageRange);
                oscilloscopeHandle.setSampleRate(SampleRate.SAMPLES_100kS_s);
                Thread.sleep(100);
                System.out.println("Low speed " + voltageRange.name());
                float[] lowspeed = ScopeUtils.readRawAverages(oscilloscopeHandle, reader, (short) (512*12), 10);
                calibrationData.setFormattedOffset(Channels.CH1, voltageRange, true, lowspeed[0]+128);
                calibrationData.setFormattedOffset(Channels.CH2, voltageRange, true, lowspeed[1]+128);

                Thread.sleep(100);
                oscilloscopeHandle.setSampleRate(SampleRate.SAMPLES_30MS_s);
                Thread.sleep(100);
                System.out.println("High speed " + voltageRange.name());
                calibrationData.setFormattedOffset(Channels.CH1, voltageRange, false,
                        ScopeUtils.readRawAverages(oscilloscopeHandle, reader, (short) (512*12), 2)[0]+128);
            }
            return calibrationData;
        } catch (LibUsbException | InterruptedException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
