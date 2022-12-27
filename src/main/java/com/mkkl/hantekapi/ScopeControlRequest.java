package com.mkkl.hantekapi;


public class ScopeControlRequest extends ControlRequest{

    public ScopeControlRequest(byte requestType, byte address, short wValue, short wIndex, byte[] data) {
        super(requestType, address, wValue, wIndex, data);
    }

    public static ScopeControlRequest getFirmwareControlRequest(short address, byte[] data) {
        return new ScopeControlRequest((byte) 0x40, UsbConnectionConst.RW_FIRMWARE_REQUEST, address, UsbConnectionConst.RW_FIRMWARE_INDEX, data);
    }

    public static ScopeControlRequest getEepromControlRequest(short address, byte[] data) {
        return new ScopeControlRequest((byte) 0x40, UsbConnectionConst.RW_EEPROM_REQUEST, address, UsbConnectionConst.RW_EEPROM_INDEX, data);
    }
}
