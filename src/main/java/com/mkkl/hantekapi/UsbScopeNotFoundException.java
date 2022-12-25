package com.mkkl.hantekapi;

import javax.usb.UsbException;

public class UsbScopeNotFoundException extends UsbException {
    public UsbScopeNotFoundException() {
    }

    public UsbScopeNotFoundException(String s) {
        super(s);
    }
}
