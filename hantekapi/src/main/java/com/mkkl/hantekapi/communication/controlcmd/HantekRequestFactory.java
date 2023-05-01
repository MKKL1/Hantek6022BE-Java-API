package com.mkkl.hantekapi.communication.controlcmd;


import com.mkkl.hantekapi.communication.HantekProtocolConstants;

public class HantekRequestFactory {

    public static ControlRequest getFirmwareRequest(short address, byte[] data) {
        return new ControlRequest(
                (byte) 0x40,
                HantekProtocolConstants.RW_FIRMWARE_REQUEST,
                address,
                HantekProtocolConstants.RW_FIRMWARE_INDEX,
                data);
    }

    public static ControlRequest getEepromWriteRequest(short address, byte[] data) {
        return new ControlRequest(
                (byte) 0x40,
                HantekProtocolConstants.RW_EEPROM_REQUEST,
                address,
                HantekProtocolConstants.RW_EEPROM_INDEX,
                data);
    }

    public static ControlRequest getEepromReadRequest(short address, short length) {
        return new ControlRequest(
                (byte) 0xC0,
                HantekProtocolConstants.RW_EEPROM_REQUEST,
                address,
                HantekProtocolConstants.RW_EEPROM_INDEX,
                new byte[length]);
    }

    public static ControlRequest getStartRequest() {
        return new ControlRequest(
                (byte) 0x40,
                HantekProtocolConstants.TRIGGER_REQUEST,
                HantekProtocolConstants.TRIGGER_VALUE,
                HantekProtocolConstants.TRIGGER_INDEX,
                new byte[] {(byte) 0x01});
    }

    public static ControlRequest getStopRequest() {
        return new ControlRequest(
                (byte) 0x40,
                HantekProtocolConstants.TRIGGER_REQUEST,
                HantekProtocolConstants.TRIGGER_VALUE,
                HantekProtocolConstants.TRIGGER_INDEX,
                new byte[] {(byte) 0x00});
    }

    public static ControlRequest getChangeChCountRequest(byte count) {
        return new ControlRequest(
                (byte) 0x40,
                HantekProtocolConstants.SET_NUMCH_REQUEST,
                HantekProtocolConstants.SET_NUMCH_VALUE,
                HantekProtocolConstants.SET_NUMCH_INDEX,
                new byte[] {count});
    }

    public static ControlRequest getVoltRangeCH1Request(byte range_index) {
        return new ControlRequest(
                (byte) 0x40,
                HantekProtocolConstants.SET_CH1_VR_REQUEST,
                HantekProtocolConstants.SET_CH_VR_VALUE,
                HantekProtocolConstants.SET_CH_VR_INDEX,
                new byte[] {range_index});
    }

    public static ControlRequest getVoltRangeCH2Request(byte range_index) {
        return new ControlRequest(
                (byte) 0x40,
                HantekProtocolConstants.SET_CH2_VR_REQUEST,
                HantekProtocolConstants.SET_CH_VR_VALUE,
                HantekProtocolConstants.SET_CH_VR_INDEX,
                new byte[] {range_index});
    }

    public static ControlRequest getSampleRateSetRequest(byte sample_rate_index) {
        return new ControlRequest((byte) 0x40,
                HantekProtocolConstants.SET_SAMPLE_RATE_REQUEST,
                HantekProtocolConstants.SET_SAMPLE_RATE_VALUE,
                HantekProtocolConstants.SET_SAMPLE_RATE_INDEX,
                new byte[] {sample_rate_index});
    }

    public static ControlRequest getCalibrationFreqSetRequest(byte byte_freq) {
        return new ControlRequest((byte) 0x40,
                HantekProtocolConstants.SET_CAL_FREQ_REQUEST,
                HantekProtocolConstants.SET_CAL_FREQ_VALUE,
                HantekProtocolConstants.SET_CAL_FREQ_INDEX,
                new byte[] {byte_freq});
    }
}
