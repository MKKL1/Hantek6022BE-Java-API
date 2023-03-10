package com.mkkl.hantekapi.firmware;

public enum SupportedFirmwares {
    stock_firmware("stock_fw.ihex"),
    mod_firmware_01("mod_fw_01.ihex"),
    mod_firmware_iso("mod_fw_iso.ihex"),
    dso6021_firmware("dso6021-firmware.hex"),
    dso6022be_firmware("dso6022be-firmware.hex"),
    dso6022bl_firmware("dso6022bl-firmware.hex"),
    dds120_firmware("dds120-firmware.hex");

    private final String filename;

    SupportedFirmwares(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }
}
