package com.mkkl.hantekapi.constants;

import com.mkkl.hantekapi.firmware.Firmwares;

public enum Scopes {
    DSO6021(0x6021, Firmwares.dso6021_firmware),
    DSO6022BE(0x6022, Firmwares.dso6022be_firmware),
    DSO6022BL(0x602A, Firmwares.dso6022bl_firmware);
    private final short productId;
    private final Firmwares firmware;

    Scopes(int productId, Firmwares firmware) {
        this.productId = (short) productId;
        this.firmware = firmware;
    }

    public short getProductId() {
        return productId;
    }

    public Firmwares getFirmwareToFlash() {
        return firmware;
    }
}
