package com.mkkl.hantekapi.exceptions;

import java.io.IOException;
import java.util.Objects;

public class UncheckedUsbException extends RuntimeException{

    public UncheckedUsbException(String message, Throwable cause) {
        super(message, Objects.requireNonNull(cause));
    }

    public UncheckedUsbException(String message) {
        super(message);
    }

    public UncheckedUsbException(Throwable cause) {
        super(Objects.requireNonNull(cause));
    }

    @Override
    public Exception getCause() {
        return (Exception) super.getCause();
    }
}
