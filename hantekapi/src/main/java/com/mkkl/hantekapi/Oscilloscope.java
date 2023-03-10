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
import com.mkkl.hantekapi.constants.OscilloscopeDevices;
import com.mkkl.hantekapi.exceptions.DeviceNotInitialized;
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

/**
 * Class used to represent oscilloscope device.
 * Provides high level methods used to interact with device,
 * as well as lower level methods for direct communication.
 */
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

    /**
     * Method used to initialize connection with device's usb interface.
     * While control request's can be sent without connecting to interface,
     * {@link ScopeDataReader} requires it to read ADC data.
     * Use this method after finding device or after flashing firmware.
     * Connects to default bulk interface
     * @see Oscilloscope#setup(SupportedInterfaces) 
     */
    public Oscilloscope setup() {
        return setup(SupportedInterfaces.BulkTransfer);
    }

    /**
     * Method used to initialize connection with device's usb interface.
     * While control request's can be sent without connecting to interface,
     * {@link ScopeDataReader} requires it to read ADC data.
     * Use this method after finding device or after flashing firmware
     * @param supportedInterfaces Set which usb interface to use
     */
    public Oscilloscope setup(SupportedInterfaces supportedInterfaces) {
        try {
            channelManager = ChannelManager.create(this);
            scopeInterface = new ScopeInterface(scopeDevice);
            scopeInterface.setInterface(supportedInterfaces);
            scopeInterface.claim();
            deviceSetup = true;
            if (currentSampleRate==null) setSampleRate(SampleRates.SAMPLES_100kS_s);
            if (firmwarePresent) setActiveChannels(ActiveChannels.CH1CH2);
        } catch (UsbException e) {
            throw new UncheckedUsbException(e);
        }
        return this;
    }

    /**
     * Makes control request to usb device and reads response
     * @param controlRequest Request data from {@link HantekRequest}
     * @return raw response from device
     */
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

    /**
     * Makes control request to usb device, reads and deserializes response
     * @param controlRequest Request data from {@link HantekRequest}
     * @param clazz Class which should be used for deserialization
     * @param <T> extends {@link SerializableData}
     * @return Deserialized response
     */
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

    /**
     * Makes control request to usb device without reading response
     * To serialize data for sending use {@link Serialization}
     * @param controlRequest Request data from {@link HantekRequest}
     * @return response without data, used for passing exception
     */
    public ControlResponse<Void> patch(final ControlRequest controlRequest) {
        UsbException e = null;
        try {
            controlRequest.write(scopeDevice);
        } catch (UsbException _e) {
            e = _e;
        }
        return new ControlResponse<>(null, e);
    }

    /**
     * Sets which channels are active on oscilloscope.
     * To be exact this method sends control request from {@link HantekRequest#getChangeChCountRequest(byte)} to device.
     * Used when you need bigger sample rate (>30M samples/s).
     * While capturing on single channel(CH1), CH2 data will be captured, but it wouldn't be accurate
     * After changing active channels make sure to start capture again {@link ScopeDataReader#startCapture()}.
     * Use {@link Oscilloscope#setup(SupportedInterfaces)} before using this method
     * @param activeChannels Either CH1 active CH2 deactivated or CH1 and CH2 active
     */
    public void setActiveChannels(ActiveChannels activeChannels) {
        if(!deviceSetup) throw new DeviceNotInitialized();
        channelManager.setActiveChannelCount(activeChannels);
    }

    //TODO link method for calculating measurement time
    /**
     * Sets the sample rate for oscilloscope to capture at.
     * Bigger sample rate means more samples have to be read to capture data for the same time.
     * For sample rates greater than 30M samples/s, only single channel(CH1) can capture data.
     * @see Oscilloscope#setActiveChannels(ActiveChannels)
     */
    public void setSampleRate(SampleRates sampleRates) {
        patch(HantekRequest.getSampleRateSetRequest(sampleRates.getSampleRateId()))
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to set sample rate",ex))
                .onSuccess(() -> currentSampleRate = sampleRates);
    }

    public void setSampleRate(byte sampleRateId) {
        patch(HantekRequest.getSampleRateSetRequest(sampleRateId))
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to set sample rate",ex))
                .onSuccess(() ->
                        currentSampleRate = Arrays
                                .stream(SampleRates.values())
                                .filter(x -> x.getSampleRateId() == sampleRateId)
                                .findFirst()
                                .orElseThrow());
    }

    /**
     * Send control request to usb device, which reads calibration data from it's eeprom.
     * Then it's deserialized to {@link CalibrationData}.
     * Use with {@link Oscilloscope#setCalibration(CalibrationData)} to read and set calibration data for current instance
     * @return Deserialized calibration data
     */
    public CalibrationData readCalibrationValues() {
        ControlResponse<CalibrationData> r = request(
                    HantekRequest.getEepromReadRequest(UsbConnectionConst.CALIBRATION_EEPROM_OFFSET, (short) 80), CalibrationData.class)
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to read calibration data", ex));
        return r.get();
    }

    /**
     * Writes serialized {@link CalibrationData} to eeprom of usb device.
     * Use after calculating new calibration data with for example {@link CalibrateScope}
     * @param calibrationData data which will be written to device's eeprom
     */
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

    /**
     * Sets calibration data for current instance.
     * This data will be used to properly calculate voltages from raw ADC data sent by usb device.
     * Use {@link Oscilloscope#setup(SupportedInterfaces)} before using this method
     * @param calibrationData calibration data either read or calculated
     */
    public void setCalibration(CalibrationData calibrationData) {
        if(!deviceSetup) throw new DeviceNotInitialized();
        channelManager.setCalibration(calibrationData);
    }

    /**
     * Read raw data from device's eeprom.
     * Check <a href="https://github.com/Ho-Ro/Hantek6022API/blob/main/docs/README.md">documentation</a>
     * @param offset offset in memory
     * @param length length of data to be read
     * @return response with raw data
     */
    public ControlResponse<byte[]> readEeprom(short offset, short length) {
        return request(HantekRequest.getEepromReadRequest(offset, length));
    }

    /**
     * Writes data to device's eeprom
     * Check <a href="https://github.com/Ho-Ro/Hantek6022API/blob/main/docs/README.md">documentation</a>
     * @param offset offset in memory
     * @param data raw data that will be written to eeprom
     * @return response used for exception passing
     */
    public ControlResponse<Void> writeEeprom(short offset, byte[] data) {
        return patch(HantekRequest.getEepromWriteRequest(offset, data));
    }

    /**
     * Flashes device's firmware with found openhantek firmware.
     * If device is not in {@link OscilloscopeDevices}, {@link java.util.NoSuchElementException} will be thrown
     */
    public void flash_firmware() {
        flash_firmware(Arrays.stream(OscilloscopeDevices.values())
                .filter(x -> x.getProductId() == scopeDevice.getUsbDeviceDescriptor().idProduct())
                .findFirst()
                .orElseThrow());
    }

    /**
     * Flashes device's firmware with openhantek's equivalence for given oscilloscope
     * @param scope device for which firmware will be searched for
     */
    public void flash_firmware(OscilloscopeDevices scope) {
        flash_firmware(scope.getFirmwareToFlash());
    }

    /**
     * Flashes device's firmware
     * @param supportedFirmwares used to get firmware's file name
     */
    public void flash_firmware(SupportedFirmwares supportedFirmwares) {
        try(InputStream firmwareInputStream = getClass().getClassLoader().getResourceAsStream(supportedFirmwares.getFilename())) {
            flash_firmware(firmwareInputStream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (UsbException e) {
            throw new UncheckedUsbException(e);
        }
    }

    /**
     * Flashes device's firmware
     * @param firmwareInputStream input stream of firmware in intel's hex format. Typically saved in .hex or .ihex file
     */
    public void flash_firmware(InputStream firmwareInputStream) throws IOException, UsbException {
        if (firmwareInputStream == null) throw new IOException("No firmware in input stream");
        try(FirmwareReader firmwareReader = new FirmwareReader(new BufferedReader(new InputStreamReader(firmwareInputStream)))) {
            flash_firmware(new ScopeFirmware(firmwareReader.readAll()));
        }
    }

    /**
     * Flashes device's firmware
     * @param firmware can be read with {@link FirmwareReader} from .hex or .ihex files (saved in resources)
     */
    public void flash_firmware(ScopeFirmware firmware) throws UsbException {
        for(FirmwareControlPacket packet : firmware.getFirmwareData())
            patch(HantekRequest.getFirmwareRequest(packet.address(), packet.data())).onFailureThrow((e) -> (UsbException)e);
    }

    /**
     * Sets the frequency of calibration square wave output.
     * On Hantek6022BE it's the small metal hook on front of deivce, where you connect probes
     * @param frequency Frequency between 32Hz and 100kHz
     */
    public void setCalibrationFrequency(int frequency) {
        if(frequency<32 || frequency>100000) throw new RuntimeException("Unsupported frequency of " + frequency);
        byte bytefreq;
        if (frequency < 1000) bytefreq = (byte) ((frequency/10)+100);
        else if (frequency < 5600) bytefreq = (byte) ((frequency/100)+200);
        else bytefreq = (byte) (frequency/1000);

        patch(HantekRequest.getCalibrationFreqSetRequest(bytefreq))
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to set calibration frequency", ex));
    }

    /**
     * Use {@link Oscilloscope#setup(SupportedInterfaces)} before using this method
     * @return new instance of ScopeDataReader
     */
    public ScopeDataReader createDataReader() throws UsbException {
        return new ScopeDataReader(this);
    }

    public ScopeChannel getChannel(Channels channels) {
        return getChannel(channels.getChannelId());
    }

    /**
     * Use {@link Oscilloscope#setup(SupportedInterfaces)} before using this method
     */
    public ScopeChannel getChannel(int id) {
        if(!deviceSetup) throw new DeviceNotInitialized();
        return channelManager.getChannel(id);
    }

    /**
     * Use {@link Oscilloscope#setup(SupportedInterfaces)} before using this method
     */
    public ArrayList<ScopeChannel> getChannels() {
        if(!deviceSetup) throw new DeviceNotInitialized();
        return channelManager.getChannels();
    }

    /**
     * Use {@link Oscilloscope#setup(SupportedInterfaces)} before using this method
     */
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

    @Override
    public void close() throws Exception {
        if (scopeInterface != null) scopeInterface.close();
        deviceSetup = false;
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
