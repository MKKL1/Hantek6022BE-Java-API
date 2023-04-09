package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.ScopeChannel;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class DoubleModeADCStream extends AdcInputStream{
    public DoubleModeADCStream(InputStream in, ChannelManager channelManager, int packetSize) {
        super(in, channelManager, packetSize);
    }

    /**
     * Reads two bytes from input stream corresponding to data sample of each channel
     * @return byte array with length of 2, where index 0 is voltage data of CH1 and index of 1 respectively CH2
     * @throws EOFException on end of stream
     * @throws IOException if other I/O error occurs
     */
    public byte[] readRawVoltages() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if((ch1 | ch2) < 0)
            throw new EOFException();
        return new byte[] {(byte) ch1, (byte) ch2};
    }

    /**
     * Uses {@link AdcInputStream#readRawVoltages()} and formats each data sample with {@link ScopeChannel#formatData(byte)}.
     * @return float array of formatted data samples (measured voltages). index 0 - CH1, index 1 - CH2
     * @throws EOFException on end of stream
     * @throws IOException if other I/O error occurs
     */
    public float[] readFormattedVoltages() throws IOException {
        byte[] rawdata = readRawVoltages();
        float[] fdata = new float[2];
        for (int i = 0; i < 2; i++)
            fdata[i] = channels.get(i).formatData(rawdata[i]);
        return fdata;
    }
}
