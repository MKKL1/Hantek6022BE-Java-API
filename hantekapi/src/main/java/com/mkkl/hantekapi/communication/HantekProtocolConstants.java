package com.mkkl.hantekapi.communication;

public class HantekProtocolConstants {
    public static final byte RW_EEPROM_REQUEST = (byte) 0xa2;
    public static final byte RW_EEPROM_INDEX = 0x00;

    public static final byte RW_FIRMWARE_REQUEST = (byte) 0xa0;
    public static final byte RW_FIRMWARE_INDEX = 0x00;

    public static final short CALIBRATION_EEPROM_OFFSET = 0x08;
    public static final byte TRIGGER_REQUEST = (byte) 0xe3;
    public static final short TRIGGER_VALUE = 0x00;
    public static final short TRIGGER_INDEX = 0x00;

    public static final byte BULK_ENDPOINT_ADDRESS = (byte) 0x86;
    public static final byte ISO_ENDPOINT_ADDRESS = (byte) 0x82;

    public static final byte SET_NUMCH_REQUEST = (byte) 0xe4;
    public static final short SET_NUMCH_VALUE = (short) 0x00;
    public static final short SET_NUMCH_INDEX = (short) 0x00;

    public static final byte SET_CH1_VR_REQUEST = (byte) 0xe0;
    public static final byte SET_CH2_VR_REQUEST = (byte) 0xe1;

    public static final short SET_CH_VR_VALUE = (short) 0x00;
    public static final short SET_CH_VR_INDEX = (short) 0x00;

    public static final byte SET_SAMPLE_RATE_REQUEST = (byte) 0xe2;
    public static final short SET_SAMPLE_RATE_VALUE = (short) 0x00;
    public static final short SET_SAMPLE_RATE_INDEX = (short) 0x00;

    public static final byte SET_CAL_FREQ_REQUEST = (byte) 0xe6;
    public static final short SET_CAL_FREQ_VALUE = 0x00;
    public static final short SET_CAL_FREQ_INDEX = 0x00;


}
