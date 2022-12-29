package com.mkkl.hantekapi;

import com.mkkl.hantekapi.adcdata.AdcInputStream;
import com.mkkl.hantekapi.adcdata.FormattedDataStream;
import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.ScopeChannel;
import com.mkkl.hantekapi.channel.VoltageRangeChange;
import com.mkkl.hantekapi.communication.HantekConnection;
import com.mkkl.hantekapi.constants.VoltageRange;
import com.mkkl.hantekapi.communication.controlcmd.ScopeControlRequest;
import com.mkkl.hantekapi.communication.interfaces.ScopeInterfaces;

import javax.usb.*;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HexFormat;

public class Oscilloscope implements AutoCloseable{
    private final HantekConnection hantekConnection;
    private ChannelManager channelManager;
    private boolean firmwarePresent = false;

    public Oscilloscope(UsbDevice usbDevice) {
        this.hantekConnection = new HantekConnection(usbDevice);
    }

    public Oscilloscope(UsbDevice usbDevice, boolean firmwarePresent){
        this(usbDevice);
        this.firmwarePresent = firmwarePresent;
    }

    public void setup() throws UsbException {
        this.channelManager = new ChannelManager(2, (newVoltageRange, channelid) ->
        {
            System.out.println("sending " + newVoltageRange + " " + channelid);
            if(channelid == 0) ScopeControlRequest.getVoltRangeCH1Request((byte) newVoltageRange.getGain()).send(getScopeDevice());
            else if(channelid == 1) ScopeControlRequest.getVoltRangeCH2Request((byte) newVoltageRange.getGain()).send(getScopeDevice());
            else throw new RuntimeException("Unknown channel id of " + channelid + "(" + channelid + 1 + ")");
        });
    }

    public void open() throws UsbException {
        open(ScopeInterfaces.BulkTransfer);
    }

    public void open(ScopeInterfaces scopeInterfaces) throws UsbException {
        hantekConnection.setInterface(scopeInterfaces);
        hantekConnection.open();
    }

    @Override
    public void close() throws UsbException {
        hantekConnection.close();
    }

    public void setActive(ActiveChannels activeChannels) throws UsbException {
        channelManager.setActiveChannelCount(activeChannels.getActiveCount());
        ScopeControlRequest.getChangeChCountRequest((byte) activeChannels.getActiveCount()).send(hantekConnection.getScopeDevice());
    }

    public byte[] getCalibrationValues() throws UsbException {
        byte[] standardcalibration = hantekConnection.getStandardCalibration();
        byte[] extendedcalibration = hantekConnection.getExtendedCalibration();
        System.out.println(Arrays.toString(standardcalibration));
        System.out.println(Arrays.toString(extendedcalibration));

        for(int j = 0; j < channelManager.getChannelCount(); j++) {
            float[] calibration = new float[VoltageRange.values().length];
            for (int i = 0; i < 4; i++)
                calibration[i] = standardcalibration[ScopeChannel.calibrationOffsets[i] + j] - 128;
            channelManager.getChannel(j).setOffsets(calibration);
        }

        for(int j = 0; j < channelManager.getChannelCount(); j++) {
            float[] calibration = new float[VoltageRange.values().length];
            for (int i = 0; i < 4; i++) {
                calibration[i] = standardcalibration[ScopeChannel.calibrationOffsets[i]+j]-128;
                byte extcal = extendedcalibration[48+ScopeChannel.calibrationOffsets[i]+j];
                if (extcal != (byte)0 && extcal != (byte)255)
                    calibration[i] = calibration[i] + (extcal - 128) / 250f;
            }
            channelManager.getChannel(j).setOffsets(calibration);

            float[] gains = new float[VoltageRange.values().length];
            for (int i = 0; i < 4; i++) {
                byte extcal = extendedcalibration[32+ScopeChannel.calibrationGainOff[i]+j];
                if (extcal != 0 && extcal != (byte)255)
                    gains[i] = gains[i] * (1 + (extcal - 128) / 500f);
            }
            channelManager.getChannel(j).setOffsets(gains);
        }

        return standardcalibration;
    }

    public void setCalibrationValues(byte[] calibrationvalues) throws UsbException {
        hantekConnection.setStandardCalibration(calibrationvalues);
    }

    public AdcInputStream getData() throws UsbException, IOException {
        return getData((short) 0x400);
    }

    public AdcInputStream getData(short size) throws UsbException, IOException {
        ScopeControlRequest.getStartRequest().send(getScopeDevice());
        AdcInputStream adcInputStream = new AdcInputStream(
                hantekConnection.getScopeInterface().getEndpoint().syncReadPipe(size),
                channelManager.getChannelCount(),
                size);
        ScopeControlRequest.getStopRequest().send(getScopeDevice());
        return adcInputStream;
    }

    public FormattedDataStream getFormattedData() throws UsbException, IOException {
        return getFormattedData((short) 0x400);
    }

    public FormattedDataStream getFormattedData(short size) throws UsbException, IOException {
        ScopeControlRequest.getStartRequest().send(getScopeDevice());
        FormattedDataStream formattedDataStream = new FormattedDataStream(
                hantekConnection.getScopeInterface().getEndpoint().syncReadPipe(size),
                channelManager,
                size);
        ScopeControlRequest.getStopRequest().send(getScopeDevice());
        return formattedDataStream;
    }

    public UsbDevice getScopeDevice() {
        return hantekConnection.getScopeDevice();
    }

    public HantekConnection getHantekConnection() {
        return hantekConnection;
    }

    public ScopeChannel getChannel(int id) {
        return channelManager.getChannel(id);
    }

    public ScopeChannel[] getChannels() {
        return channelManager.getChannels();
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public boolean isFirmwarePresent() {
        return firmwarePresent;
    }

    @Override
    public String toString() {
        String s = "Oscilloscope ";
        UsbDevice usbDevice = getScopeDevice();
        try {
            s += usbDevice.getProductString();
        } catch (UsbException | UnsupportedEncodingException e) {
            s += "UNKNOWN_PRODUCT_STRING";
        }
        return s;
    }

    public String getDescriptor() {
        String s = "Oscilloscope ";
        UsbDevice usbDevice = getScopeDevice();
        try {
            s += usbDevice.getProductString() + System.lineSeparator();
        } catch (UsbException | UnsupportedEncodingException e) {
            s += "UNKNOWN_PRODUCT_STRING"+ System.lineSeparator();
        }
        UsbDeviceDescriptor usbDeviceDescriptor = usbDevice.getUsbDeviceDescriptor();
        s += " idProduct=0x" + HexFormat.of().toHexDigits(usbDeviceDescriptor.idProduct()) + System.lineSeparator();
        s += " idVendor=0x" + HexFormat.of().toHexDigits(usbDeviceDescriptor.idVendor()) + System.lineSeparator();
        s += " bcdDevice=0x" + HexFormat.of().toHexDigits(usbDeviceDescriptor.bcdDevice()) + System.lineSeparator();
        s += " isFirmwarePresent=" + isFirmwarePresent() + System.lineSeparator();
        s += " interfaces=" + usbDevice.getActiveUsbConfiguration().getUsbInterfaces().toString() + System.lineSeparator();
        try {
            s += " manufacturer=" + usbDevice.getManufacturerString() + System.lineSeparator();
        } catch (UsbException | UnsupportedEncodingException e) {
            s += " manufacturer=UNKNOWN" + System.lineSeparator();
        }
        try {
            s += " serial=" + usbDevice.getSerialNumberString() + System.lineSeparator();
        } catch (UsbException | UnsupportedEncodingException e) {
            s += " serial=UNKNOWN" + System.lineSeparator();
        }
        return s;
    }
}
