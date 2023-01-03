package com.mkkl.hantekapi.communication.controlcmd;


import com.mkkl.hantekapi.communication.UsbConnectionConst;

public class HantekRequest {

    public static ControlRequest getFirmwareRequest(short address, byte[] data) {
        return new ControlRequest((byte) 0x40, UsbConnectionConst.RW_FIRMWARE_REQUEST, address, UsbConnectionConst.RW_FIRMWARE_INDEX, data);
    }

    public static ControlRequest getEepromWriteRequest(short address, byte[] data) {
        return new ControlRequest((byte) 0x40, UsbConnectionConst.RW_EEPROM_REQUEST, address, UsbConnectionConst.RW_EEPROM_INDEX, data);
    }

    public static ControlRequest getEepromReadRequest(short address, byte[] data) {
        return new ControlRequest((byte) 0xC0, UsbConnectionConst.RW_EEPROM_REQUEST, address, UsbConnectionConst.RW_EEPROM_INDEX, data);
    }

    public static ControlRequest getStartRequest() {
        return new ControlRequest(
                (byte) 0x40,
                UsbConnectionConst.TRIGGER_REQUEST,
                UsbConnectionConst.TRIGGER_VALUE,
                UsbConnectionConst.TRIGGER_INDEX,
                new byte[] {(byte) 0x01});
    }

    public static ControlRequest getStopRequest() {
        return new ControlRequest(
                (byte) 0x40,
                UsbConnectionConst.TRIGGER_REQUEST,
                UsbConnectionConst.TRIGGER_VALUE,
                UsbConnectionConst.TRIGGER_INDEX,
                new byte[] {(byte) 0x00});
    }

    public static ControlRequest getChangeChCountRequest(byte count) {
        return new ControlRequest(
                (byte) 0x40,
                UsbConnectionConst.SET_NUMCH_REQUEST,
                UsbConnectionConst.SET_NUMCH_VALUE,
                UsbConnectionConst.SET_NUMCH_INDEX,
                new byte[] {count});
    }

    public static ControlRequest getVoltRangeCH1Request(byte range_index) {
        return new ControlRequest(
                (byte) 0x40,
                UsbConnectionConst.SET_CH1_VR_REQUEST,
                UsbConnectionConst.SET_CH_VR_VALUE,
                UsbConnectionConst.SET_CH_VR_INDEX,
                new byte[] {range_index});
    }

    public static ControlRequest getVoltRangeCH2Request(byte range_index) {
        return new ControlRequest(
                (byte) 0x40,
                UsbConnectionConst.SET_CH2_VR_REQUEST,
                UsbConnectionConst.SET_CH_VR_VALUE,
                UsbConnectionConst.SET_CH_VR_INDEX,
                new byte[] {range_index});
    }

    public static ControlRequest getSampleRateSetRequest(byte sample_rate_index) {
        return new ControlRequest((byte) 0x40,
                UsbConnectionConst.SET_SAMPLE_RATE_REQUEST,
                UsbConnectionConst.SET_SAMPLE_RATE_VALUE,
                UsbConnectionConst.SET_SAMPLE_RATE_INDEX,
                new byte[] {sample_rate_index});
    }

    public static ControlRequest getCalibrationFreqSetRequest(byte byte_freq) {
        return new ControlRequest((byte) 0x40,
                UsbConnectionConst.SET_CAL_FREQ_REQUEST,
                UsbConnectionConst.SET_CAL_FREQ_VALUE,
                UsbConnectionConst.SET_CAL_FREQ_INDEX,
                new byte[] {byte_freq});
    }
}
