package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.Channels;
import com.mkkl.hantekapi.channel.ScopeChannel;
import com.mkkl.hantekapi.communication.UsbConnectionConst;
import com.mkkl.hantekapi.communication.adcdata.ScopeDataReader;
import com.mkkl.hantekapi.communication.controlcmd.*;
import com.mkkl.hantekapi.communication.controlcmd.response.calibration.CalibrationData;
import com.mkkl.hantekapi.communication.controlcmd.response.ControlResponse;
import com.mkkl.hantekapi.communication.controlcmd.response.SerializableData;
import com.mkkl.hantekapi.communication.Serialization;
import com.mkkl.hantekapi.communication.interfaces.ScopeInterface;
import com.mkkl.hantekapi.communication.interfaces.SupportedInterfaces;
import com.mkkl.hantekapi.constants.SampleRates;
import com.mkkl.hantekapi.constants.Scopes;
import com.mkkl.hantekapi.exceptions.DeviceNotInitialized;
import com.mkkl.hantekapi.exceptions.OscilloscopeException;
import com.mkkl.hantekapi.exceptions.UncheckedUsbException;
import com.mkkl.hantekapi.firmware.FirmwareControlPacket;
import com.mkkl.hantekapi.firmware.FirmwareReader;
import com.mkkl.hantekapi.firmware.ScopeFirmware;
import com.mkkl.hantekapi.firmware.SupportedFirmwares;

import javax.usb.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;

public class Oscilloscope implements AutoCloseable{
    private ChannelManager channelManager;
    private SampleRates currentSampleRate;
    private ScopeInterface scopeInterface;
    private final UsbDevice scopeDevice;
    private boolean deviceSetup = false;
    private final boolean firmwarePresent;

    private Oscilloscope(UsbDevice usbDevice, boolean firmwarePresent){
        this.scopeDevice = usbDevice;
        this.firmwarePresent = firmwarePresent;
    }

    public static Oscilloscope create(UsbDevice usbDevice) {
        return new Oscilloscope(usbDevice, false);
    }

    public static Oscilloscope create(UsbDevice usbDevice, boolean firmwarePresent) {
        return new Oscilloscope(usbDevice, firmwarePresent);
    }

    public Oscilloscope setup() {
        return setup(SupportedInterfaces.BulkTransfer);
    }

    public Oscilloscope setup(SupportedInterfaces supportedInterfaces) {
        try {
            channelManager = ChannelManager.create(this);
            scopeInterface = new ScopeInterface(scopeDevice);
            scopeInterface.setInterface(supportedInterfaces);
            scopeInterface.claim();
            deviceSetup = true;
        } catch (UsbException e) {
            throw new UncheckedUsbException(e);
        }
        return this;
    }

    public ControlResponse<byte[]> request(final ControlRequest controlRequest) {
        byte[] bytes = null;
        UsbException e = null;
        try {
            bytes = controlRequest.read(scopeDevice);
        } catch (UsbException _e) {
            e = _e;
        }
        return new ControlResponse<>(bytes, e);
    }

    //TODO catch all exceptions
    public <T extends SerializableData> ControlResponse<T> request(final ControlRequest controlRequest, final Class<T> clazz) {
        ControlResponse<byte[]> rawResponse = request(controlRequest);
        Exception e = null;
        T body = null;
        try {
            rawResponse.onFailureThrow();
            body = Serialization.deserialize(rawResponse.get(), clazz);
        } catch (Exception _e) {
            e = _e;
        }

        return new ControlResponse<>(body, e);
    }

    public ControlResponse<Void> patch(final ControlRequest controlRequest) {
        UsbException e = null;
        try {
            controlRequest.write(scopeDevice);
        } catch (UsbException _e) {
            e = _e;
        }
        return new ControlResponse<>(null, e);
    }

    public void setActiveChannels(ActiveChannels activeChannels) {
        if(!deviceSetup) throw new DeviceNotInitialized();
        patch(HantekRequest.getChangeChCountRequest((byte) activeChannels.getActiveCount()))
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to set active channels ", ex))
                .onSuccess(() -> channelManager.setActiveChannelCount(activeChannels.getActiveCount()));
    }

    public void setSampleRate(SampleRates sampleRates) {
        patch(HantekRequest.getSampleRateSetRequest(sampleRates.getSampleRateId()))
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to set sample rate",ex))
                .onSuccess(() -> currentSampleRate = sampleRates);
    }

    public CalibrationData readCalibrationValues() {
        if(!deviceSetup) throw new DeviceNotInitialized();
        ControlResponse<CalibrationData> r = request(
                    HantekRequest.getEepromReadRequest(UsbConnectionConst.CALIBRATION_EEPROM_OFFSET, (short) 80), CalibrationData.class)
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to read calibration data", ex));
        return r.get();
    }

    public void writeCalibrationValues(CalibrationData calibrationData) {
        byte[] data = null;
        try {
            data = Serialization.serialize(calibrationData);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        patch(HantekRequest.getEepromWriteRequest(UsbConnectionConst.CALIBRATION_EEPROM_OFFSET, data))
                .onFailureThrow((ex) -> new RuntimeException(ex.getMessage()));
    }

    public void setCalibration(CalibrationData calibrationData) {
        channelManager.setCalibration(calibrationData);
    }

    public ControlResponse<byte[]> readEeprom(short offset, short length) {
        return request(HantekRequest.getEepromReadRequest(offset, length));
    }

    public ControlResponse<Void> writeEeprom(short offset, byte[] data) {
        return patch(HantekRequest.getEepromWriteRequest(offset, data));
    }

    public void flash_firmware() {
        flash_firmware(Arrays.stream(Scopes.values())
                .filter(x -> x.getProductId() == scopeDevice.getUsbDeviceDescriptor().idProduct())
                .findFirst()
                .orElseThrow());
    }

    public void flash_firmware(Scopes scope) {
        flash_firmware(scope.getFirmwareToFlash());
    }

    public void flash_firmware(SupportedFirmwares supportedFirmwares) {
        try(InputStream firmwareInputStream = getClass().getClassLoader().getResourceAsStream(supportedFirmwares.getFilename())) {
            flash_firmware(firmwareInputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (UsbException e) {
            throw new UncheckedUsbException(e);
        }
    }

    public void flash_firmware(InputStream firmwareInputStream) throws IOException, UsbException {
        if (firmwareInputStream == null) throw new IOException("No firmware in input stream");
        try(FirmwareReader firmwareReader = new FirmwareReader(new BufferedReader(new InputStreamReader(firmwareInputStream)))) {
            flash_firmware(new ScopeFirmware(firmwareReader.readAll()));
        }
    }

    public void flash_firmware(ScopeFirmware firmware) throws UsbException {
        for(FirmwareControlPacket packet : firmware.getFirmwareData())
            patch(HantekRequest.getFirmwareRequest(packet.address(), packet.data())).onFailureThrow((e) -> (UsbException)e);
    }

    public void setCalibrationFrequency(int frequency) {
        if(frequency<32 || frequency>100000) throw new OscilloscopeException("Unsupported frequency of " + frequency);
        byte bytefreq;
        if (frequency < 1000) bytefreq = (byte) ((frequency/10)+100);
        else if (frequency < 5600) bytefreq = (byte) ((frequency/100)+200);
        else bytefreq = (byte) (frequency/1000);

        patch(HantekRequest.getCalibrationFreqSetRequest(bytefreq))
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to set calibration frequency", ex));
    }

    //TODO Copied for easier access
    public ScopeChannel getChannel(Channels channels) {
        return getChannel(channels.getChannelId());
    }

    public ScopeChannel getChannel(int id) {
        if(!deviceSetup) throw new DeviceNotInitialized();
        return channelManager.getChannel(id);
    }

    public ArrayList<ScopeChannel> getChannels() {
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

    public ScopeInterface getScopeInterface() {
        return scopeInterface;
    }

    public UsbDevice getScopeDevice() {
        return scopeDevice;
    }

    public SampleRates getCurrentSampleRate() {
        return currentSampleRate;
    }

    public ScopeDataReader createDataReader() throws UsbException, IOException {
        return new ScopeDataReader(this);
    }

    @Override
    public void close() throws Exception {
        scopeInterface.close();
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
