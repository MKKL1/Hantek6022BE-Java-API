package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.channel.ChannelManager;
import com.mkkl.hantekapi.channel.Channels;
import com.mkkl.hantekapi.channel.ScopeChannel;
import com.mkkl.hantekapi.communication.HantekProtocolConstants;
import com.mkkl.hantekapi.communication.readers.async.AsyncScopeDataReader;
import com.mkkl.hantekapi.communication.readers.sync.SyncScopeDataReader;
import com.mkkl.hantekapi.communication.controlcmd.*;
import com.mkkl.hantekapi.communication.controlcmd.response.calibration.CalibrationData;
import com.mkkl.hantekapi.communication.controlcmd.response.ControlResponse;
import com.mkkl.hantekapi.communication.controlcmd.response.SerializableData;
import com.mkkl.hantekapi.communication.Serialization;
import com.mkkl.hantekapi.communication.interfaces.ScopeUsbInterface;
import com.mkkl.hantekapi.communication.interfaces.UsbInterfaceType;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;
import com.mkkl.hantekapi.constants.SampleRate;
import com.mkkl.hantekapi.constants.HantekDeviceType;
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
public class Oscilloscope {

    private final UsbDevice usbDevice;
    private final boolean firmwarePresent;

    private OscilloscopeHandle oscilloscopeHandle;

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
     * @see Oscilloscope#setup(UsbInterfaceType)
     */
    public OscilloscopeHandle setup() {
        return setup(UsbInterfaceType.BulkTransfer);
    }

    /**
     * Method used to initialize connection with device's usb interface.
     * While control request's can be sent without connecting to interface,
     * {@link SyncScopeDataReader} requires it to read ADC data.
     * Use this method after finding device or after flashing firmware
     * @param usbInterfaceType Set which usb interface to use
     */
    public OscilloscopeHandle setup(UsbInterfaceType usbInterfaceType) {
        try {
            if (oscilloscopeHandle == null)
                oscilloscopeHandle = new OscilloscopeHandle(this, usbInterfaceType);

            if (firmwarePresent)
                oscilloscopeHandle.setActiveChannels(ActiveChannels.CH1CH2);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize oscilloscope!", e);
        }
        return oscilloscopeHandle;
    }

    public boolean isFirmwarePresent() {
        return firmwarePresent;
    }

    public UsbDevice getUsbDevice() {
        return usbDevice;
    }

    public OscilloscopeHandle getHandle() {
        return oscilloscopeHandle;
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
