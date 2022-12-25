package com.mkkl;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            FirmwareReader firmwareReader = new FirmwareReader(new FileReader("dso6022be-firmware.hex"));
            Firmware firmware = firmwareReader.readFirmware();
            System.out.println(Arrays.toString(firmware.firmwareData));
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println((byte)255);
    }
}
