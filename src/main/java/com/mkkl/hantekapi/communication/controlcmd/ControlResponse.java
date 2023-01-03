package com.mkkl.hantekapi.communication.controlcmd;

import javax.usb.UsbException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ControlResponse <T> {

    T responseBody;
    Exception ex;

    public ControlResponse(T responseBody, Exception ex) {
        this.responseBody = responseBody;
        this.ex = ex;
    }

    public ControlResponse<T> onFailure(Consumer<Exception> consumer) {
        if(ex != null) consumer.accept(ex);
        return this;
    }

    public ControlResponse<T> onSuccess(Consumer<T> consumer) {
        if(ex == null) consumer.accept(responseBody);
        return this;
    }

    public <X extends Throwable> ControlResponse<T> onFailureThrow(Function<Exception, ? extends X> function) throws X{
        if(ex != null) function.apply(ex);
        return this;
    }

    public <X extends Throwable> void onFailureThrow(Supplier<? extends X> exceptionSupplier) throws X{
        if (ex != null) throw exceptionSupplier.get();
    }

    public void onFailureThrow() throws Exception {
        if(ex != null) throw ex;
    }

    public Exception getException() {
        return ex;
    }

    public T get() {
        return responseBody;
    }
}