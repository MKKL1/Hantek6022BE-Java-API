package com.mkkl.hantekapi.constants;

import com.mkkl.hantekapi.firmware.SupportedFirmwares;

public enum HantekDeviceType {
    DSO6021(0x6021, SupportedFirmwares.dso6021_firmware),
    DSO6022BE(0x6022, SupportedFirmwares.dso6022be_firmware),
    DSO6022BL(0x602A, SupportedFirmwares.dso6022bl_firmware);
    private final short productId;
    private final SupportedFirmwares firmware;

    HantekDeviceType(int productId, SupportedFirmwares firmware) {
        this.productId = (short) productId;
        this.firmware = firmware;
    }

    public short getProductId() {
        return productId;
    }

    public SupportedFirmwares getFirmwareToFlash() {
        return firmware;
    }
}
