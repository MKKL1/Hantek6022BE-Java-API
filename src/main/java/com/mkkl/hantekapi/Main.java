package com.mkkl.hantekapi;

import javax.usb.UsbException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Main {

    public static void main(String[] args) throws UnsupportedEncodingException, UsbException {
//        try {
//            FirmwareReader firmwareReader = new FirmwareReader(new FileReader("dso6022be-firmware.hex"));
//            Firmware firmware = firmwareReader.readFirmware();
//            System.out.println(Arrays.toString(firmware.firmwareData));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        System.out.println((byte)255);
        Oscilloscope oscilloscope = new Oscilloscope();
        oscilloscope.setup();
        System.out.println(oscilloscope.getScopeDevice().getProductString());

    }
}
