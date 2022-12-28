package com.mkkl.hantekapi.controlrequest;

public class UsbConnectionConst {
    public static final byte RW_EEPROM_REQUEST = (byte) 0xa2;
    public static final byte RW_EEPROM_INDEX = 0x00;

    public static final byte RW_FIRMWARE_REQUEST = (byte) 0xa0;
    public static final byte RW_FIRMWARE_INDEX = 0x00;

    public static final short CALIBRATION_EEPROM_OFFSET = 0x08;
    public static final byte TRIGGER_REQUEST = (byte) 0xe3;
    public static final short TRIGGER_VALUE = 0x00;
    public static final byte TRIGGER_INDEX = 0x00;

    public static final byte BULK_ENDPOINT_ADDRESS = (byte) 0x86;
    public static final byte ISO_ENDPOINT_ADDRESS = (byte) 0x82;

}
