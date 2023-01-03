package com.mkkl.hantekapi.communication.controlcmd;

import javax.usb.UsbException;
import java.util.function.Consumer;

public class ControlResponse <T> {

    T responseBody;
    Exception ex;

    ControlResponse<T> onFailure(Consumer<Exception> consumer) {
        if(ex != null) consumer.accept(ex);
        return this;
    }

    T get() {
        return responseBody;
    }
}