package com.mkkl.hantekapi.exceptions;

import javax.usb.UsbException;
import java.io.IOException;
import java.util.Objects;

public class UncheckedUsbException extends RuntimeException{

    public UncheckedUsbException(String message, Throwable cause) {
        super(message, Objects.requireNonNull(cause));
    }

    public UncheckedUsbException(Throwable cause) {
        super(Objects.requireNonNull(cause));
    }

    @Override
    public UsbException getCause() {
        return (UsbException) super.getCause();
    }
}
