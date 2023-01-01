package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.Channels;
import com.mkkl.hantekapi.channel.ScopeChannel;
import com.mkkl.hantekapi.communication.UsbConnectionConst;
import com.mkkl.hantekapi.constants.SampleRates;
import com.mkkl.hantekapi.constants.Scopes;
import com.mkkl.hantekapi.constants.VoltageRange;
import com.mkkl.hantekapi.communication.controlcmd.ScopeControlRequest;
import com.mkkl.hantekapi.firmware.FirmwareControlPacket;
import com.mkkl.hantekapi.firmware.FirmwareReader;
import com.mkkl.hantekapi.firmware.ScopeFirmware;
import com.mkkl.hantekapi.firmware.SupportedFirmwares;
import org.apache.commons.lang3.ArrayUtils;

import javax.usb.*;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HexFormat;

public class Oscilloscope {
    private ChannelManager channelManager;
    private boolean firmwarePresent = false;
    private SampleRates currentSampleRate;
    private final UsbDevice scopeDevice;
    private boolean deviceSetup = false;

    public Oscilloscope(UsbDevice usbDevice) {
        this.scopeDevice = usbDevice;
    }

    public Oscilloscope(UsbDevice usbDevice, boolean firmwarePresent){
        this(usbDevice);
        this.firmwarePresent = firmwarePresent;
    }

    public void setup() throws UsbException {
        //TODO not sure this is the right way
        this.channelManager = new ChannelManager(2, (newVoltageRange, channelid) ->
        {
            if(channelid == Channels.CH1) ScopeControlRequest.getVoltRangeCH1Request((byte) newVoltageRange.getGain()).write(scopeDevice);
            else if(channelid == Channels.CH2) ScopeControlRequest.getVoltRangeCH2Request((byte) newVoltageRange.getGain()).write(scopeDevice);
            else throw new RuntimeException("Unknown channel " + channelid);
        });
        deviceSetup = true;
    }

    public void setActiveChannels(ActiveChannels activeChannels) throws UsbException {
        if(!deviceSetup) throw new DeviceNotInitialized();
        channelManager.setActiveChannelCount(activeChannels.getActiveCount());
        ScopeControlRequest.getChangeChCountRequest((byte) activeChannels.getActiveCount()).write(scopeDevice);
    }

    public void setSampleRate(SampleRates sampleRates) throws UsbException {
        currentSampleRate = sampleRates;
        ScopeControlRequest.getSampleRateSetRequest(sampleRates.getSampleRateId()).write(scopeDevice);
    }

    public SampleRates getSampleRate() {
        return currentSampleRate;
    }


    public byte[] getCalibrationValues() throws UsbException {
        return getCalibrationValues((short) 32);
    }

    //TODO move to each channel to set
    public byte[] getCalibrationValues(short length) throws UsbException {
        if(!deviceSetup) throw new DeviceNotInitialized();
        byte[] standardcalibration = read_eeprom(UsbConnectionConst.CALIBRATION_EEPROM_OFFSET, length);
        byte[] extendedcalibration = read_eeprom(UsbConnectionConst.CALIBRATION_EEPROM_OFFSET, (short) 80);

        for(int j = 0; j < channelManager.getChannelCount(); j++) {
            float[] calibration = new float[VoltageRange.values().length];
            for (int i = 0; i < 4; i++)
                calibration[i] = standardcalibration[ScopeChannel.calibrationOffsets[i] + j] + 128;
            channelManager.getChannel(j).setOffsets(calibration);
            System.out.println(Arrays.toString(calibration));
        }

        for(int j = 0; j < channelManager.getChannelCount(); j++) {
            Collection<Float> offsetsList = channelManager.getChannel(j).getOffsets().values();
            float[] offsets = ArrayUtils.toPrimitive(offsetsList.toArray(new Float[0]), 0f);
            for (int i = 0; i < 4; i++) {
                offsets[i] = standardcalibration[ScopeChannel.calibrationOffsets[i]+j]+128;
                byte extcal = extendedcalibration[48+ScopeChannel.calibrationOffsets[i]+j];
                if (extcal != (byte)0 && extcal != (byte)255)
                    offsets[i] = offsets[i] + (extcal - 128) / 250f;
            }
            channelManager.getChannel(j).setOffsets(offsets);

            Collection<Float> gainslist = channelManager.getChannel(j).getGains().values();
            float[] gains = ArrayUtils.toPrimitive(gainslist.toArray(new Float[0]), 1f);
            for (int i = 0; i < 4; i++) {
                byte extcal = extendedcalibration[32+ScopeChannel.calibrationGainOff[i]+j];
                if (extcal != 0 && extcal != (byte)255)
                    gains[i] = gains[i] * (1 + (extcal + 128) / 500f);
            }
            channelManager.getChannel(j).setGains(gains);
        }

        return standardcalibration;
    }

    public byte[] read_eeprom(short offset, short length) throws UsbException {
        return ScopeControlRequest.getEepromReadRequest(offset, new byte[length]).read(scopeDevice);
    }

    public void write_eeprom(short offset, byte[] data) throws UsbException {
        ScopeControlRequest.getEepromWriteRequest(offset, data).write(scopeDevice);
    }

    public void flash_firmware() throws IOException, UsbException {
        flash_firmware(Arrays.stream(Scopes.values())
                .filter(x -> x.getProductId() == scopeDevice.getUsbDeviceDescriptor().idProduct())
                .findFirst()
                .orElseThrow());
    }

    public void flash_firmware(Scopes scope) throws IOException, UsbException {
        flash_firmware(scope.getFirmwareToFlash());
    }

    public void flash_firmware(SupportedFirmwares supportedFirmwares) throws IOException, UsbException {
        InputStream firmwareInputStream = getClass().getClassLoader().getResourceAsStream(supportedFirmwares.getFilename());
        flash_firmware(firmwareInputStream);
    }

    public void flash_firmware(InputStream firmwareInputStream) throws IOException, UsbException {
        if (firmwareInputStream == null) throw new IOException("No firmware in input stream");
        try(FirmwareReader firmwareReader = new FirmwareReader(new BufferedReader(new InputStreamReader(firmwareInputStream)))) {
            flash_firmware(new ScopeFirmware(firmwareReader.readAll()));
        }
    }

    public void flash_firmware(ScopeFirmware firmware) throws UsbException {
        for(FirmwareControlPacket packet : firmware.getFirmwareData()) {
            ScopeControlRequest.getFirmwareRequest(packet.address(), packet.data()).write(scopeDevice);
        }
    }

    public void setStandardCalibration(byte[] data) throws UsbException {
        write_eeprom(UsbConnectionConst.CALIBRATION_EEPROM_OFFSET, data);
    }

    //TODO Copied for easier access
    public ScopeChannel getChannel(Channels channels) {
        return getChannel(channels.getChannelId());
    }

    public ScopeChannel getChannel(int id) {
        if(!deviceSetup) throw new DeviceNotInitialized();
        return channelManager.getChannel(id);
    }

    public ScopeChannel[] getChannels() {
        if(!deviceSetup) throw new DeviceNotInitialized();
        return channelManager.getChannels();
    }

    public ChannelManager getChannelManager() {
        if(!deviceSetup) throw new DeviceNotInitialized();
        return channelManager;
    }

    public boolean isFirmwarePresent() {
        return firmwarePresent;
    }

    public UsbDevice getScopeDevice() {
        return scopeDevice;
    }

    public SampleRates getCurrentSampleRate() {
        return currentSampleRate;
    }

    @Override
    public String toString() {
        String s = "Oscilloscope ";
        try {
            s += scopeDevice.getProductString();
        } catch (UsbException | UnsupportedEncodingException e) {
            s += "UNKNOWN_PRODUCT_STRING";
        }
        return s;
    }

    public String getDescriptor() {
        String s = this + System.lineSeparator();
        UsbDeviceDescriptor usbDeviceDescriptor = scopeDevice.getUsbDeviceDescriptor();
        s += " idProduct=0x" + HexFormat.of().toHexDigits(usbDeviceDescriptor.idProduct()) + System.lineSeparator();
        s += " idVendor=0x" + HexFormat.of().toHexDigits(usbDeviceDescriptor.idVendor()) + System.lineSeparator();
        s += " bcdDevice=0x" + HexFormat.of().toHexDigits(usbDeviceDescriptor.bcdDevice()) + System.lineSeparator();
        s += " isFirmwarePresent=" + isFirmwarePresent() + System.lineSeparator();
        s += " interfaces=" + scopeDevice.getActiveUsbConfiguration().getUsbInterfaces().toString() + System.lineSeparator();
        try {
            s += " manufacturer=" + scopeDevice.getManufacturerString() + System.lineSeparator();
        } catch (UsbException | UnsupportedEncodingException e) {
            s += " manufacturer=UNKNOWN" + System.lineSeparator();
        }
        try {
            s += " serial=" + scopeDevice.getSerialNumberString() + System.lineSeparator();
        } catch (UsbException | UnsupportedEncodingException e) {
            s += " serial=UNKNOWN" + System.lineSeparator();
        }
        return s;
    }
}
