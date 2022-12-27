package com.mkkl.hantekapi.controlrequest;


public class ScopeControlRequest extends ControlRequest{

    public ScopeControlRequest(byte requestType, byte address, short wValue, short wIndex, byte[] data) {
        super(requestType, address, wValue, wIndex, data);
    }

    public static ScopeControlRequest getFirmwareRequest(short address, byte[] data) {
        return new ScopeControlRequest((byte) 0x40, UsbConnectionConst.RW_FIRMWARE_REQUEST, address, UsbConnectionConst.RW_FIRMWARE_INDEX, data);
    }

    public static ScopeControlRequest getEepromRequest(short address, byte[] data) {
        return new ScopeControlRequest((byte) 0x40, UsbConnectionConst.RW_EEPROM_REQUEST, address, UsbConnectionConst.RW_EEPROM_INDEX, data);
    }

    public static ScopeControlRequest getStartRequest() {
        return new ScopeControlRequest(
                (byte) 0x40,
                UsbConnectionConst.TRIGGER_REQUEST,
                UsbConnectionConst.TRIGGER_VALUE,
                UsbConnectionConst.RW_EEPROM_INDEX,
                new byte[] {(byte) 0x01});
    }

    public static ScopeControlRequest getStopRequest() {
        return new ScopeControlRequest(
                (byte) 0x40,
                UsbConnectionConst.TRIGGER_REQUEST,
                UsbConnectionConst.TRIGGER_VALUE,
                UsbConnectionConst.RW_EEPROM_INDEX,
                new byte[] {(byte) 0x00});
    }
}
