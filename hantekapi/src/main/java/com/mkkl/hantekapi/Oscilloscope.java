package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.Channels;
import com.mkkl.hantekapi.channel.ScopeChannel;
import com.mkkl.hantekapi.communication.UsbConnectionConst;
import com.mkkl.hantekapi.communication.adcdata.SyncScopeDataReader;
import com.mkkl.hantekapi.communication.controlcmd.*;
import com.mkkl.hantekapi.communication.controlcmd.response.calibration.CalibrationData;
import com.mkkl.hantekapi.communication.controlcmd.response.ControlResponse;
import com.mkkl.hantekapi.communication.controlcmd.response.SerializableData;
import com.mkkl.hantekapi.communication.Serialization;
import com.mkkl.hantekapi.communication.interfaces.ScopeInterface;
import com.mkkl.hantekapi.communication.interfaces.SupportedInterfaces;
import com.mkkl.hantekapi.constants.SampleRates;
import com.mkkl.hantekapi.constants.HantekDevices;
import com.mkkl.hantekapi.exceptions.DeviceNotInitialized;
import com.mkkl.hantekapi.exceptions.UncheckedUsbException;
import com.mkkl.hantekapi.firmware.FirmwareControlPacket;
import com.mkkl.hantekapi.firmware.FirmwareReader;
import com.mkkl.hantekapi.firmware.ScopeFirmware;
import com.mkkl.hantekapi.firmware.SupportedFirmwares;
import org.usb4java.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;

/**
 * Class used to represent oscilloscope device.
 * Provides high level methods used to interact with device,
 * as well as lower level methods for direct communication.
 */
public class Oscilloscope implements AutoCloseable {
    private DeviceHandle deviceHandle;
    private final UsbDevice usbDevice;
    //private final Context context = LibUsbInstance.getContext();

    private ChannelManager channelManager;
    private SampleRates currentSampleRate;
    private ScopeInterface scopeInterface;

    private boolean deviceSetup = false;
    private final boolean firmwarePresent;

    private Oscilloscope(Device device, boolean firmwarePresent){
        this.usbDevice = new UsbDevice(device);
        this.firmwarePresent = firmwarePresent;
    }

    public static Oscilloscope create(Device usbDevice) {
        return new Oscilloscope(usbDevice, false);
    }

    public static Oscilloscope create(Device usbDevice, boolean firmwarePresent) {
        return new Oscilloscope(usbDevice, firmwarePresent);
    }

    /**
     * Method used to initialize connection with device's usb interface.
     * While control request's can be sent without connecting to interface,
     * {@link SyncScopeDataReader} requires it to read ADC data.
     * Use this method after finding device or after flashing firmware.
     * Connects to default bulk interface
     * @see Oscilloscope#setupInterface(SupportedInterfaces)
     */
    public Oscilloscope setupInterface() {
        return setupInterface(SupportedInterfaces.BulkTransfer);
    }

    /**
     * Method used to initialize connection with device's usb interface.
     * While control request's can be sent without connecting to interface,
     * {@link SyncScopeDataReader} requires it to read ADC data.
     * Use this method after finding device or after flashing firmware
     * @param supportedInterfaces Set which usb interface to use
     */
    public Oscilloscope setupInterface(SupportedInterfaces supportedInterfaces) {
        try {
            channelManager = ChannelManager.create(this);
            if (deviceHandle == null) openHandle();
            scopeInterface = new ScopeInterface(usbDevice);
            scopeInterface.setInterface(supportedInterfaces);
            scopeInterface.claim();
            deviceSetup = true;
            if (currentSampleRate == null) setSampleRate(SampleRates.SAMPLES_100kS_s);
            if (firmwarePresent) setActiveChannels(ActiveChannels.CH1CH2);
        } catch (Exception e) {
            //e.printStackTrace();
            throw new RuntimeException("Failed to initialize oscilloscope!", e);
        }
        return this;
    }

    public void openHandle() {
        deviceHandle = usbDevice.open();
    }

    /**
     * Makes control request to usb device and reads response
     * @param controlRequest Request data from {@link HantekRequest}
     * @return raw response from device
     */
    public ControlResponse<byte[]> request(final ControlRequest controlRequest) {
        if (deviceHandle == null) openHandle();
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
        if (deviceHandle == null) openHandle();
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
     * To be exact this method sends control request from {@link HantekRequest#getChangeChCountRequest(byte)} to device.
     * Used when you need bigger sample rate (>30M samples/s).
     * While capturing on single channel(CH1), CH2 data will be captured, but it wouldn't be accurate
     * After changing active channels make sure to start capture again {@link SyncScopeDataReader#startCapture()}.
     * Use {@link Oscilloscope#setupInterface(SupportedInterfaces)} before using this method
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
     * Use {@link Oscilloscope#setupInterface(SupportedInterfaces)} before using this method
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
     * If device is not in {@link HantekDevices}, {@link java.util.NoSuchElementException} will be thrown
     */
    public void flash_firmware() {
        DeviceDescriptor deviceDescriptor = usbDevice.getDeviceDescriptor();
        flash_firmware(Arrays.stream(HantekDevices.values())
                .filter(x -> x.getProductId() == deviceDescriptor.idProduct())
                .findFirst()
                .orElseThrow());
    }

    /**
     * Flashes device's firmware with openhantek's equivalence for given oscilloscope
     * @param scope device for which firmware will be searched for
     */
    public void flash_firmware(HantekDevices scope) {
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
        try(FirmwareReader firmwareReader = new FirmwareReader(new BufferedReader(new InputStreamReader(firmwareInputStream)))) {
            flash_firmware(new ScopeFirmware(firmwareReader.readAll()));
        }
    }

    /**
     * Flashes device's firmware
     * @param firmware can be read with {@link FirmwareReader} from .hex or .ihex files (saved in resources)
     */
    public void flash_firmware(ScopeFirmware firmware) throws LibUsbException {
        for(FirmwareControlPacket packet : firmware.getFirmwareData())
            patch(HantekRequest.getFirmwareRequest(packet.address(), packet.data())).onFailureThrow((e) -> (LibUsbException)e);
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
     * Use {@link Oscilloscope#setupInterface(SupportedInterfaces)} before using this method
     * @return new instance of ScopeDataReader
     */
    public SyncScopeDataReader createDataReader() throws LibUsbException {
        return new SyncScopeDataReader(this);
    }

    public ScopeChannel getChannel(Channels channels) {
        return getChannel(channels.getChannelId());
    }

    /**
     * Use {@link Oscilloscope#setupInterface(SupportedInterfaces)} before using this method
     */
    public ScopeChannel getChannel(int id) {
        if(!deviceSetup) throw new DeviceNotInitialized();
        return channelManager.getChannel(id);
    }

    /**
     * Use {@link Oscilloscope#setupInterface(SupportedInterfaces)} before using this method
     */
    public ArrayList<ScopeChannel> getChannels() {
        if(!deviceSetup) throw new DeviceNotInitialized();
        return channelManager.getChannels();
    }

    /**
     * Use {@link Oscilloscope#setupInterface(SupportedInterfaces)} before using this method
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

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }

    public SampleRates getCurrentSampleRate() {
        return currentSampleRate;
    }

    @Override
    public void close() throws Exception {
        if (scopeInterface != null) scopeInterface.close();
        if (deviceHandle != null) LibUsb.close(deviceHandle);
        deviceSetup = false;
    }

    @Override
    public String toString() {
        String s;
        try {
            s = usbDevice.getStringDescriptor();
        } catch (Exception e) {
            s = "UNKNOWN_PRODUCT_STRING";
        }
        return s;
    }

    public String getDescriptor() {
        String s = this + System.lineSeparator();
        DeviceDescriptor deviceDescriptor = usbDevice.getDeviceDescriptor();
        s += " idProduct=0x" + HexFormat.of().toHexDigits(deviceDescriptor.idProduct()) + System.lineSeparator();
        s += " idVendor=0x" + HexFormat.of().toHexDigits(deviceDescriptor.idVendor()) + System.lineSeparator();
        s += " bcdDevice=0x" + HexFormat.of().toHexDigits(deviceDescriptor.bcdDevice()) + System.lineSeparator();
        s += " isFirmwarePresent=" + isFirmwarePresent() + System.lineSeparator();
        return s;
    }
}
