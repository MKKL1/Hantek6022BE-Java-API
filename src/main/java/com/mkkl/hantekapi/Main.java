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

    public static void main(String[] args) throws UnsupportedEncodingException, UsbException, InterruptedException {
//
//
//        System.out.println((byte)255);
        Oscilloscope oscilloscope = new Oscilloscope();
        oscilloscope.setup();
//        oscilloscope.open_handle();
//        System.out.println(oscilloscope.getScopeDevice().getProductString());
//        System.out.println(Arrays.toString(oscilloscope.read_eeprom((short) 0x08, (short) 32)));
//        System.out.println(Arrays.toString(oscilloscope.read_eeprom((short) 0x08, (short) 80)));
        oscilloscope.flash_firmware(Firmwares.dso6022be_firmware);
//        oscilloscope.close_handle();

    }
}
