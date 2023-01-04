package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.Channels;
import com.mkkl.hantekapi.communication.adcdata.ScopeDataReader;
import com.mkkl.hantekapi.constants.SampleRates;
import com.mkkl.hantekapi.constants.VoltageRange;

import javax.usb.UsbException;
import java.io.IOException;
import java.util.HashMap;

public class CalibrateScope {
    private final Oscilloscope oscilloscope;

    public CalibrateScope(Oscilloscope oscilloscope) {
        this.oscilloscope = oscilloscope;
    }

    /**
     * Calculates calibration values.
     * Use only when 0V are applied to both channels
     */
    public boolean fastCalibration(ScopeDataReader reader) {
        HashMap<VoltageRange, Byte> offsets1 = new HashMap<>();
        HashMap<VoltageRange, Byte> offsets1_hs = new HashMap<>();
        HashMap<VoltageRange, Byte> offsets2 = new HashMap<>();
        try {
            for (VoltageRange voltageRange : VoltageRange.values()) {
                oscilloscope.getChannel(Channels.CH1).setVoltageRange(voltageRange);
                oscilloscope.getChannel(Channels.CH2).setVoltageRange(voltageRange);
                oscilloscope.setSampleRate(SampleRates.SAMPLES_100kS_s);
                Thread.sleep(100);
                System.out.println("Low speed " + voltageRange.name());
                byte[] lowspeed = ScopeUtils.readRawAverages(oscilloscope, reader, (short) 1024, 5);
                offsets1.put(voltageRange, lowspeed[0]);
                offsets2.put(voltageRange, lowspeed[1]);

                Thread.sleep(100);
                oscilloscope.setSampleRate(SampleRates.SAMPLES_30MS_s);
                Thread.sleep(100);
                System.out.println("High speed " + voltageRange.name());
                offsets1_hs.put(voltageRange, ScopeUtils.readRawAverages(oscilloscope, reader, (short) 1024, 5)[0]);
            }
            System.out.println(offsets1);
            System.out.println(offsets2);
            System.out.println(offsets1_hs);
            return true;
        } catch (UsbException | InterruptedException | IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            reader.stopCapture();
        }
    }

}
