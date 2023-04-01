package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.communication.adcdata.AdcInputStream;
import com.mkkl.hantekapi.communication.readers.sync.SyncScopeDataReader;
import com.mkkl.hantekapi.constants.HantekDevices;
import com.mkkl.hantekapi.devicemanager.HantekDeviceList;
import com.mkkl.hantekapi.devicemanager.OscilloscopeManager;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;

public class ScopeUtils {
    public static float[] readRawAverages(Oscilloscope oscilloscope, SyncScopeDataReader reader, short size, int repeat) throws InterruptedException, IOException {
        final boolean single = oscilloscope.getCurrentSampleRate().isSingleChannel();
        if(single) oscilloscope.setActiveChannels(ActiveChannels.CH1);
        else oscilloscope.setActiveChannels(ActiveChannels.CH1CH2);

        ArrayList<Byte> channel1Data = new ArrayList<>();
        ArrayList<Byte> channel2Data = new ArrayList<>();
        for (int i = 0; i < repeat; i++) {
            oscilloscope.startCapture();
            byte[] data = reader.readToByteArray(size);
            AdcInputStream inputStream = new AdcInputStream(new ByteArrayInputStream(data), oscilloscope);
            int sizeToRead = data.length;
            try {
                while (sizeToRead > 0) {
                    byte[] channelVoltages = inputStream.readRawVoltages();
                    channel1Data.add(channelVoltages[0]);
                    if (!single) channel2Data.add(channelVoltages[1]);
                    sizeToRead -= 2;
                }
            } catch (EOFException e) {
                //END
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            oscilloscope.stopCapture();
            Thread.sleep(100);
        }
        return new float[] {channel1Data.size() != 0 ? (channel1Data.stream().mapToInt(Byte::intValue).sum()/(float)channel1Data.size()) : 0f,
                            channel2Data.size() != 0 ? (channel2Data.stream().mapToInt(Byte::intValue).sum()/(float)channel2Data.size()) : 0f};
    }

    public static float[] readRawAverages(Oscilloscope oscilloscope, short size, int repeat) throws Exception {
        try(SyncScopeDataReader reader = new SyncScopeDataReader(oscilloscope)) {
            return readRawAverages(oscilloscope, reader, size, repeat);
        }
    }

    public static Oscilloscope getAndFlashFirmware(HantekDevices hantekDevices) throws InterruptedException {
        HantekDeviceList hantekDeviceList = OscilloscopeManager.findSupportedDevices();
        //Find connected oscilloscopes of type DSO6022BE and choose first found
        Oscilloscope oscilloscope = hantekDeviceList.getFirstFound(hantekDevices);

        int tries = 20;
        //Check if software is found, if not flash new firmware
        if (!oscilloscope.isFirmwarePresent()) {
            //flashing firmware with openhantek's alternative for given device (in this case DSO6022BE)
            oscilloscope.flash_firmware();
            //Waiting for device with flashed firmware to appear
            while(oscilloscope == null || !oscilloscope.isFirmwarePresent()) {
                Thread.sleep(100);
                try {
                    oscilloscope = OscilloscopeManager.findSupportedDevices()
                            .getFirstFound(hantekDevices);
                    tries--;
                    if(tries == 0) throw new RuntimeException("Failed to find device after firmware flash");
                } catch (NoSuchElementException e) {
                    oscilloscope = null;
                }
            }
        }
        //TODO log time
        return oscilloscope;
    }
}
