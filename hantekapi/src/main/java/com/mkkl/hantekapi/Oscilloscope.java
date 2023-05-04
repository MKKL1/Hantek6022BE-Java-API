package com.mkkl.hantekapi;

import com.mkkl.hantekapi.channel.ActiveChannels;
import com.mkkl.hantekapi.communication.interfaces.UsbInterfaceType;
import org.usb4java.*;

import java.util.HexFormat;

/**
 * Class that's represents oscilloscope device.
 * It doesn't provide any functionality to communicate with device, other than basic operations that doesn't require DeviceHandle from libusb library.
 * To use more functions use {@link #setup()}
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
     * Use this method after finding device or after flashing firmware.
     * Connects to default bulk interface
     * @see Oscilloscope#setup(UsbInterfaceType)
     */
    public OscilloscopeHandle setup() {
        return setup(UsbInterfaceType.BulkTransfer);
    }

    /**
     * Method used to initialize connection with device's usb interface.
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

    /**
     * @return Returns true if found device's vendor id matches that of custom openhantek's firmware.
     */
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
