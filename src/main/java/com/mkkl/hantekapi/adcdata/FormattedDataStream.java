package com.mkkl.hantekapi.adcdata;

import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.ScopeChannel;

import java.io.IOException;
import java.io.PipedInputStream;

/**
 * Variant of {@link AdcInputStream} which formats output data to human readable form.
 * Opposite to {@link AdcInputStream} it takes into account which channels are active automatically
 */
public class FormattedDataStream extends AdcInputStream {
    private final ChannelManager channelManager;
    private final int activeChannelCount;
    private final ScopeChannel[] channels;

    /**
     * @param pipedInputStream input stream from usb endpoint
     * @param channelManager taken from {@link com.mkkl.hantekapi.Oscilloscope}
     * @param length expected length of data to be read
     */
    public FormattedDataStream(PipedInputStream pipedInputStream, ChannelManager channelManager, int length) {
        super(pipedInputStream, channelManager.getActiveChannelCount(), length);
        this.channelManager = channelManager;
        activeChannelCount = channelManager.getActiveChannelCount();
        channels = channelManager.getChannels();
    }

    /**
     * Reads next data byte and formats it by given channel
     * @param scopeChannel channel to format with
     * @return Actual voltage on channel
     */
    public float readFormatted(ScopeChannel scopeChannel) throws IOException {
        return scopeChannel.formatData((byte) read());
    }

    /**
     * Reads next data byte and formats it by given channel
     * @param channelId Id of channel to format with
     * @return Actual voltage on channel
     */
    public float readFormatted(int channelId) throws IOException {
        return channelManager.getChannel(channelId).formatData((byte) read());
    }

    /**
     * Reads voltage data, formats it and then sets it to each {@link ScopeChannel#currentData}
     * @throws IOException From {@link #read()}
     */
    public void readToChannels() throws IOException {
        byte[] bytes = readChannels();
        for (int i = 0; i < activeChannelCount; i++) {
            channels[i].currentData = channels[i].formatData(bytes[i]);
        }
    }

    /**
     * @return float array of voltage data where each column is data from next active channel eg. {@code [channel0, channel2]}
     * @throws IOException From {@link #read()}
     */
    public float[] readFormattedChannels() throws IOException {
        byte[] bytes = readChannels();
        float[] formatteddata = new float[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            formatteddata[i] = channels[i].formatData(bytes[i]);
        }
        return formatteddata;
    }
}
