package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.communication.adcdata.AdcInputStream;
import com.mkkl.hantekapi.communication.adcdata.ScopeDataReader;
import com.mkkl.hantekapi.constants.VoltageRange;

import javax.usb.UsbException;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ScopeUtils {
    public static byte[] readAverages(Oscilloscope oscilloscope, ScopeDataReader reader, short size, int repeat) throws UsbException, InterruptedException, IOException {
        final boolean single = oscilloscope.getCurrentSampleRate().isSingleChannel();
        if(single) oscilloscope.setActiveChannels(ActiveChannels.CH1);
        else oscilloscope.setActiveChannels(ActiveChannels.CH1CH2);

        AdcInputStream adcInputStream = reader.getAdcInputStream();
        ArrayList<Byte> channel1Data = new ArrayList<>();
        ArrayList<Byte> channel2Data = new ArrayList<>();
        for (int i = 0; i < repeat; i++) {
            try {
                reader.readDataFrame(size);
                while (true) {
                    byte[] data = adcInputStream.readChannels();
                    System.out.print(Arrays.toString(data));
                    channel1Data.add(data[0]);
                    if (!single) channel2Data.add(data[1]);
                }
            } catch (EOFException e) {e.printStackTrace();}
            System.out.println("");
            Thread.sleep(100);
        }
        return new byte[] {channel1Data.size() != 0 ? (byte) (channel1Data.stream().mapToInt(Byte::intValue).sum()/channel1Data.size()) : 0,
                            channel1Data.size() != 0 ? (byte) (channel2Data.stream().mapToInt(Byte::intValue).sum()/channel2Data.size()) : 0};
    }
}
