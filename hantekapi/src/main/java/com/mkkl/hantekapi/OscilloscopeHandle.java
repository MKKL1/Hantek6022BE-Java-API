package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.Channels;
import com.mkkl.hantekapi.channel.ScopeChannel;
import com.mkkl.hantekapi.communication.HantekProtocolConstants;
import com.mkkl.hantekapi.communication.Serialization;
import com.mkkl.hantekapi.communication.controlcmd.ControlRequest;
import com.mkkl.hantekapi.communication.controlcmd.HantekRequestFactory;
import com.mkkl.hantekapi.communication.controlcmd.response.ControlResponse;
import com.mkkl.hantekapi.communication.controlcmd.response.SerializableData;
import com.mkkl.hantekapi.communication.controlcmd.response.calibration.CalibrationData;
import com.mkkl.hantekapi.communication.interfaces.ScopeUsbInterface;
import com.mkkl.hantekapi.communication.interfaces.UsbInterfaceType;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;
import com.mkkl.hantekapi.communication.readers.async.AsyncScopeDataReader;
import com.mkkl.hantekapi.communication.readers.sync.SyncScopeDataReader;
import com.mkkl.hantekapi.constants.HantekDeviceType;
import com.mkkl.hantekapi.constants.SampleRate;
import com.mkkl.hantekapi.exceptions.UncheckedUsbException;
import com.mkkl.hantekapi.firmware.FirmwareControlPacket;
import com.mkkl.hantekapi.firmware.FirmwareFileReader;
import com.mkkl.hantekapi.firmware.ScopeFirmware;
import com.mkkl.hantekapi.firmware.SupportedFirmwares;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;
import org.usb4java.LibUsbException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Class that handles requests to and from oscilloscope after initialization of {@link Oscilloscope}.
 * Provides high level methods used to interact with device,
 * as well as lower level methods for direct communication.
 */
public class OscilloscopeHandle implements AutoCloseable {
    private final Oscilloscope oscilloscope;
    private final UsbDevice usbDevice;

    private final DeviceHandle deviceHandle;
    private final ChannelManager channelManager;
    private SampleRate currentSampleRate;

    private final ScopeUsbInterface scopeUsbInterface;

    private boolean capture;


     OscilloscopeHandle(Oscilloscope oscilloscope, UsbInterfaceType usbInterfaceType) {
        this.oscilloscope = oscilloscope;
        this.usbDevice = oscilloscope.getUsbDevice();

        channelManager = ChannelManager.create(this);
        deviceHandle = usbDevice.open();
        scopeUsbInterface = new ScopeUsbInterface(usbDevice);
        scopeUsbInterface.setInterface(usbInterfaceType);
        scopeUsbInterface.claim();
        if (currentSampleRate == null) setSampleRate(SampleRate.SAMPLES_100kS_s);
    }

    /**
     * Makes control request to usb device and reads response
     * @param controlRequest Request data from {@link HantekRequestFactory}
     * @return raw response from device
     */
    public ControlResponse<byte[]> readControl(final ControlRequest controlRequest) {
        byte[] bytes = null;
        LibUsbException e = null;
        try {
            bytes = controlRequest.read(deviceHandle);
        } catch (LibUsbException _e) {
            e = _e;
        }
        return new ControlResponse<>(bytes, e);
    }

    /**
     * Makes control request to usb device, reads and deserializes response
     * @param controlRequest Request data from {@link HantekRequestFactory}
     * @param clazz Class which should be used for deserialization
     * @param <T> extends {@link SerializableData}
     * @return Deserialized response
     */
    public <T extends SerializableData> ControlResponse<T> readControl(final ControlRequest controlRequest, final Class<T> clazz) {
        ControlResponse<byte[]> rawResponse = readControl(controlRequest);
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
     * @param controlRequest Request data from {@link HantekRequestFactory}
     * @return response without data, used for passing exception
     */
    public ControlResponse<Void> writeControl(final ControlRequest controlRequest) {
        LibUsbException e = null;
        try {
            controlRequest.write(deviceHandle);
        } catch (LibUsbException _e) {
            e = _e;
        }
        return new ControlResponse<>(null, e);
    }

    /**
     * Sets which channels are active on oscilloscope.
     * To be exact this method sends control request from {@link HantekRequestFactory#getChangeChCountRequest(byte)} to device.
     * Used when you need bigger sample rate (>30M samples/s).
     * While capturing on single channel(CH1), CH2 data will be captured, but it wouldn't be accurate
     * After changing active channels make sure to start capture again {@link OscilloscopeHandle#startCapture()}.
     * Use {@link Oscilloscope#setup(UsbInterfaceType)} before using this method
     * @param activeChannels Either CH1 active CH2 deactivated or CH1 and CH2 active
     */
    public void setActiveChannels(ActiveChannels activeChannels) {
        channelManager.setActiveChannelCount(activeChannels);
    }

    //TODO link method for calculating measurement time
    /**
     * Sets the sample rate for oscilloscope to capture at.
     * Bigger sample rate means more samples have to be read to capture data for the same time.
     * For sample rates greater than 30M samples/s, only single channel(CH1) can capture data.
     * @see OscilloscopeHandle#setActiveChannels(ActiveChannels)
     */
    public void setSampleRate(SampleRate sampleRate) {
        writeControl(HantekRequestFactory.getSampleRateSetRequest(sampleRate.getSampleRateId()))
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to set sample rate",ex))
                .onSuccess(() -> currentSampleRate = sampleRate);
    }

    /**
     * Sets the sample rate for oscilloscope to capture at.
     * Bigger sample rate means more samples have to be read to capture data for the same time.
     * For sample rates greater than 30M samples/s, only single channel(CH1) can capture data.
     * @param sampleRateId id of {@link SampleRate}, given by {@link SampleRate#getSampleRateId()}
     */
    public void setSampleRate(byte sampleRateId) {
        writeControl(HantekRequestFactory.getSampleRateSetRequest(sampleRateId))
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to set sample rate",ex))
                .onSuccess(() ->
                        currentSampleRate = Arrays
                                .stream(SampleRate.values())
                                .filter(x -> x.getSampleRateId() == sampleRateId)
                                .findFirst()
                                .orElseThrow());
    }

    /**
     * Send control request to usb device, which reads calibration data from it's eeprom.
     * Then it's deserialized to {@link CalibrationData}.
     * Use with {@link OscilloscopeHandle#setCalibration(CalibrationData)} to read and set calibration data for current instance
     * @return Deserialized calibration data
     */
    public CalibrationData readCalibrationValues() {
        ControlResponse<CalibrationData> r = readControl(
                HantekRequestFactory.getEepromReadRequest(HantekProtocolConstants.CALIBRATION_EEPROM_OFFSET, (short) 80), CalibrationData.class)
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
        writeControl(HantekRequestFactory.getEepromWriteRequest(HantekProtocolConstants.CALIBRATION_EEPROM_OFFSET, data))
                .onFailureThrow((ex) -> new RuntimeException(ex.getMessage()));
    }

    /**
     * Sets calibration data for current instance.
     * This data will be used to properly calculate voltages from raw ADC data sent by usb device.
     * Use {@link Oscilloscope#setup(UsbInterfaceType)} before using this method
     * @param calibrationData calibration data either read or calculated
     */
    public void setCalibration(CalibrationData calibrationData) {
        channelManager.setCalibration(calibrationData);
    }

    /**
     * Makes control request to device to tell it, to start capturing samples.
     * Use before reading data from ADC.
     */
    public void startCapture() {
        writeControl(HantekRequestFactory.getStartRequest())
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to start capture", ex))
                .onSuccess(() -> capture = true);
    }

    /**
     * Makes control request to device to tell it, to stop capturing samples.
     * Supported only by custom openhantek firmware.
     * @see OscilloscopeHandle#flash_firmware()
     */
    public void stopCapture() {
        writeControl(HantekRequestFactory.getStopRequest())
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to stop capture", ex))
                .onSuccess(() -> capture = false);
    }

    /**
     * Checks if capture was started by {@link #startCapture()}, if not it starts capture.
     */
    public void ensureCaptureStarted() {
        if(!capture) startCapture();
    }

    /**
     * Read raw data from device's eeprom.
     * Check <a href="https://github.com/Ho-Ro/Hantek6022API/blob/main/docs/README.md">documentation</a>
     * @param offset offset in memory
     * @param length length of data to be read
     * @return response with raw data
     */
    public ControlResponse<byte[]> readEeprom(short offset, short length) {
        return readControl(HantekRequestFactory.getEepromReadRequest(offset, length));
    }

    /**
     * Writes data to device's eeprom
     * Check <a href="https://github.com/Ho-Ro/Hantek6022API/blob/main/docs/README.md">documentation</a>
     * @param offset offset in memory
     * @param data raw data that will be written to eeprom
     * @return response used for exception passing
     */
    public ControlResponse<Void> writeEeprom(short offset, byte[] data) {
        return writeControl(HantekRequestFactory.getEepromWriteRequest(offset, data));
    }

    /**
     * Flashes device's firmware with found openhantek's firmware.
     * If device is not in {@link HantekDeviceType}, {@link java.util.NoSuchElementException} will be thrown
     */
    public void flash_firmware() {
        DeviceDescriptor deviceDescriptor = usbDevice.getDeviceDescriptor();
        flash_firmware(Arrays.stream(HantekDeviceType.values())
                .filter(x -> x.getProductId() == deviceDescriptor.idProduct())
                .findFirst()
                .orElseThrow());
    }

    /**
     * Flashes device's firmware with openhantek's equivalence for given oscilloscope
     * @param scope device for which firmware will be searched for
     */
    public void flash_firmware(HantekDeviceType scope) {
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
        }
    }

    /**
     * Flashes device's firmware
     * @param firmwareInputStream input stream of firmware in intel's hex format. Typically saved in .hex or .ihex file
     */
    public void flash_firmware(InputStream firmwareInputStream) throws IOException, LibUsbException {
        if (firmwareInputStream == null) throw new IOException("No firmware in input stream");
        try(FirmwareFileReader firmwareFileReader = new FirmwareFileReader(new BufferedReader(new InputStreamReader(firmwareInputStream)))) {
            flash_firmware(new ScopeFirmware(firmwareFileReader.readAll()));
        }
    }

    /**
     * Flashes device's firmware
     * @param firmware can be read with {@link FirmwareFileReader} from .hex or .ihex files (saved in resources)
     */
    public void flash_firmware(ScopeFirmware firmware) throws LibUsbException {
        for(FirmwareControlPacket packet : firmware.getFirmwareData())
            writeControl(HantekRequestFactory.getFirmwareRequest(packet.address(), packet.data())).onFailureThrow((e) -> (LibUsbException)e);
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

        writeControl(HantekRequestFactory.getCalibrationFreqSetRequest(bytefreq))
                .onFailureThrow((ex) -> new UncheckedUsbException("Failed to set calibration frequency", ex));
    }

    /**
     * Use {@link Oscilloscope#setup(UsbInterfaceType)} before using this method
     * @return new instance of ScopeDataReader
     */
    public SyncScopeDataReader createSyncDataReader() throws LibUsbException {
        return new SyncScopeDataReader(this);
    }

    /**
     * Use {@link Oscilloscope#setup(UsbInterfaceType)} before using this method
     * @return new instance of ScopeDataReader
     */
    public AsyncScopeDataReader createAsyncDataReader() throws LibUsbException {
        return new AsyncScopeDataReader(this);
    }

    public ScopeChannel getChannel(Channels channels) {
        return getChannel(channels.getChannelId());
    }

    public ScopeChannel getChannel(int id) {
        return channelManager.getChannel(id);
    }

    public ArrayList<ScopeChannel> getChannels() {
        return channelManager.getChannels();
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public ScopeUsbInterface getScopeInterface() {
        return scopeUsbInterface;
    }

    public Endpoint getEndpoint() {
        return scopeUsbInterface.getEndpoint();
    }

    public SampleRate getCurrentSampleRate() {
        return currentSampleRate;
    }

    @Override
    public void close() throws Exception {
        if (scopeUsbInterface != null) scopeUsbInterface.close();
        if (deviceHandle != null) LibUsb.close(deviceHandle);
    }

    public Oscilloscope getOscilloscope() {
        return oscilloscope;
    }
}
