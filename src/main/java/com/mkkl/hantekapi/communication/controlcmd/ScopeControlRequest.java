package com.mkkl.hantekapi.communication.controlcmd;


import com.mkkl.hantekapi.communication.UsbConnectionConst;

public class ScopeControlRequest extends ControlRequest{

    public ScopeControlRequest(byte requestType, byte address, short wValue, short wIndex, byte[] data) {
        super(requestType, address, wValue, wIndex, data);
    }

    public static ScopeControlRequest getFirmwareRequest(short address, byte[] data) {
        return new ScopeControlRequest((byte) 0x40, UsbConnectionConst.RW_FIRMWARE_REQUEST, address, UsbConnectionConst.RW_FIRMWARE_INDEX, data);
    }

    public static ScopeControlRequest getEepromWriteRequest(short address, byte[] data) {
        return new ScopeControlRequest((byte) 0x40, UsbConnectionConst.RW_EEPROM_REQUEST, address, UsbConnectionConst.RW_EEPROM_INDEX, data);
    }

    public static ScopeControlRequest getEepromReadRequest(short address, byte[] data) {
        return new ScopeControlRequest((byte) 0xC0, UsbConnectionConst.RW_EEPROM_REQUEST, address, UsbConnectionConst.RW_EEPROM_INDEX, data);
    }

    public static ScopeControlRequest getStartRequest() {
        return new ScopeControlRequest(
                (byte) 0x40,
                UsbConnectionConst.TRIGGER_REQUEST,
                UsbConnectionConst.TRIGGER_VALUE,
                UsbConnectionConst.TRIGGER_INDEX,
                new byte[] {(byte) 0x01});
    }

    public static ScopeControlRequest getStopRequest() {
        return new ScopeControlRequest(
                (byte) 0x40,
                UsbConnectionConst.TRIGGER_REQUEST,
                UsbConnectionConst.TRIGGER_VALUE,
                UsbConnectionConst.TRIGGER_INDEX,
                new byte[] {(byte) 0x00});
    }

    public static ScopeControlRequest getChangeChCountRequest(byte count) {
        return new ScopeControlRequest(
                (byte) 0x40,
                UsbConnectionConst.SET_NUMCH_REQUEST,
                UsbConnectionConst.SET_NUMCH_VALUE,
                UsbConnectionConst.SET_NUMCH_INDEX,
                new byte[] {count});
    }

    public static ScopeControlRequest getVoltRangeCH1Request(byte range_index) {
        return new ScopeControlRequest(
                (byte) 0x40,
                UsbConnectionConst.SET_CH1_VR_REQUEST,
                UsbConnectionConst.SET_CH_VR_VALUE,
                UsbConnectionConst.SET_CH_VR_INDEX,
                new byte[] {range_index});
    }

    public static ScopeControlRequest getVoltRangeCH2Request(byte range_index) {
        return new ScopeControlRequest(
                (byte) 0x40,
                UsbConnectionConst.SET_CH2_VR_REQUEST,
                UsbConnectionConst.SET_CH_VR_VALUE,
                UsbConnectionConst.SET_CH_VR_INDEX,
                new byte[] {range_index});
    }

    public static ScopeControlRequest getSampleRateSetRequest(byte sample_rate_index) {
        return new ScopeControlRequest((byte) 0x40,
                UsbConnectionConst.SET_SAMPLE_RATE_REQUEST,
                UsbConnectionConst.SET_SAMPLE_RATE_VALUE,
                UsbConnectionConst.SET_SAMPLE_RATE_INDEX,
                new byte[] {sample_rate_index});
    }

    public static ScopeControlRequest getCalibrationFreqSetRequest(byte byte_freq) {
        return new ScopeControlRequest((byte) 0x40,
                UsbConnectionConst.SET_CAL_FREQ_REQUEST,
                UsbConnectionConst.SET_CAL_FREQ_VALUE,
                UsbConnectionConst.SET_CAL_FREQ_INDEX,
                new byte[] {byte_freq});
    }
}
