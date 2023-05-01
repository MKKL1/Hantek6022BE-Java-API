package com.mkkl.hantekapi.communication.adcdata;

import com.mkkl.hantekapi.Oscilloscope;
import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.ScopeChannel;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;

import java.io.*;
import java.util.ArrayList;
/**
 * Stream used for parsing data read from oscilloscope ADC.
 * ADC data is sent in packets with length of even numbers.
 * Every second byte is channel's raw voltage data, starting with CH1.
 * Raw voltage data needs to be formatted by {@link ScopeChannel#formatData(byte)}.
 */
public abstract class AdcInputStream extends FilterInputStream{

    protected final ArrayList<ScopeChannel> channels;
    protected final int packetSize;

    public static AdcInputStream create(InputStream in, ChannelManager channelManager, int packetSize, ActiveChannels activeChannels) {
        if(activeChannels.isSingleMode())
            return new SingleModeADCStream(in, channelManager, packetSize);
        else return new DoubleModeADCStream(in, channelManager, packetSize);
    }

    public static AdcInputStream create(InputStream in, ChannelManager channelManager, int packetSize) {
        return AdcInputStream.create(in, channelManager, packetSize, channelManager.getActiveChannels());
    }

    public static AdcInputStream create(InputStream in, Oscilloscope oscilloscope) {
        ChannelManager channelManager = oscilloscope.getChannelManager();
        int packetSize = oscilloscope.getScopeInterface().getEndpoint().getPacketSize();

        if(channelManager.getActiveChannels().isSingleMode())
            return new SingleModeADCStream(in, channelManager, packetSize);
        else return new DoubleModeADCStream(in, channelManager, packetSize);
    }

    protected AdcInputStream(InputStream in, ChannelManager channelManager, int packetSize) {
        super(in);
        this.channels = channelManager.getChannels();
        this.packetSize = packetSize;
    }

    public abstract byte[] readRawVoltages() throws IOException;

    public abstract float[] readFormattedVoltages() throws IOException;

    /**
     * Skips N bytes, where N is length of packet sent by device.
     * @see Endpoint#getPacketSize()
     * @throws IOException if an I/O error occurs
     */
    public int skipPacket() throws IOException {
        skipNBytes(packetSize);
        return packetSize;
    }
//    /**
//     * Similar to {@link AdcInputStream#readFormattedVoltages()}, but values are saved in each {@link ScopeChannel}
//     * @throws EOFException on end of stream
//     * @throws IOException if other I/O error occurs
//     */
//    public void readToChannels() throws IOException {
//        float[] fdata = readFormattedVoltages();
//        for (int i = 0; i < 2; i++) {
//            channels.get(i).currentData = fdata[i];
//        }
//    }

//    /**
//     * Similar to {@link AdcInputStream#readRawVoltages()}
//     * @param n how many samples for channels to read. n means that 2n of bytes will read from stream
//     * @return array of byte arrays of raw voltage data. byte[n][2]
//     * @throws EOFException on end of stream
//     * @throws IOException if other I/O error occurs
//     */
//    public byte[][] readNRawVoltages(int n) throws IOException {
//        byte[][] data = new byte[n][2];
//        for (int i = 0; i < n; i++)
//            data[i] = readRawVoltages();
//        return data;
//    }
//
//    /**
//     * Similar to {@link AdcInputStream#readFormattedVoltages()}
//     * @param n how many samples for channels to read. n means that 2n of bytes will read from stream
//     * @return array of float arrays of formatted voltage data. float[n][2]
//     * @throws EOFException on end of stream
//     * @throws IOException if other I/O error occurs
//     */
//    public float[][] readNFormattedVoltages(int n) throws IOException {
//        float[][] data = new float[n][2];
//        for (int i = 0; i < n; i++)
//            data[i] = readFormattedVoltages();
//        return data;
//    }
}

class DoubleModeADCStream extends AdcInputStream{
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

class SingleModeADCStream extends AdcInputStream {
    public SingleModeADCStream(InputStream in, ChannelManager channelManager, int packetSize) {
        super(in, channelManager, packetSize);
    }

    @Override
    public byte[] readRawVoltages() throws IOException {
        int ch1 = read();
        if(ch1 < 0) throw new EOFException();
        return new byte[] {(byte) ch1};
    }

    @Override
    public float[] readFormattedVoltages() throws IOException {
        return new float[] {channels.get(0).formatData(readRawVoltages()[0])};
    }
}
