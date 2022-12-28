package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.ScopeChannel;

import java.io.IOException;
import java.io.PipedInputStream;

public class FormattedDataStream extends AdcInputStream{
    private final ChannelManager channelManager;
    public FormattedDataStream(PipedInputStream pipedInputStream, ChannelManager channelManager, int length) throws IOException {
        super(pipedInputStream, channelManager.getChannelCount(), length);
        this.channelManager = channelManager;
    }


    /**
     * Reads voltage data and formats it, to {@link ScopeChannel#currentData}
     * @throws IOException
     */
    public void readToChannels() throws IOException {
        byte[] bytes = readChannels();
        ScopeChannel[] channels = channelManager.getChannels();
        for (int i = 0; i < channelcount; i++) {
            channels[i].currentData = channels[i].formatData(bytes[i]);
        }
    }

    /**
     *
     * @param channelId Id of channel to format with
     * @return Actual voltage on channel
     */
    public float readFormatted(int channelId) throws IOException {
        return channelManager.getChannel(channelId).formatData((byte) read());
    }

    public float[] readFormattedChannels() throws IOException {
        byte[] bytes = readChannels();
        float[] formatteddata = new float[bytes.length];
        ScopeChannel[] channels = channelManager.getChannels();
        for (int i = 0; i < bytes.length; i++) {
            formatteddata[i] = channels[i].formatData(bytes[i]);
        }
        return formatteddata;
    }
}
