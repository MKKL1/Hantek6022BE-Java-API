package com.mkkl.hantekapi.communication.controlcmd.response;

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

    public ControlResponse<T> onFailure(Consumer<Exception> exceptionConsumer) {
        if(ex != null) exceptionConsumer.accept(ex);
        return this;
    }

    public ControlResponse<T> onSuccess(Consumer<T> bodyConsumer) {
        if(ex == null) bodyConsumer.accept(responseBody);
        return this;
    }

    public <X extends Throwable> ControlResponse<T> onFailureThrow(Function<Exception, ? extends X> function) throws X{
        if(ex != null) throw function.apply(ex);
        return this;
    }

    public <X extends Throwable> ControlResponse<T> onFailureThrow(Supplier<? extends X> exceptionSupplier) throws X{
        if (ex != null) throw exceptionSupplier.get();
        return this;
    }

    public ControlResponse<T> onFailureThrow() throws Exception {
        if(ex != null) throw ex;
        return this;
    }

    public Exception getException() {
        return ex;
    }

    public T get() {
        return responseBody;
    }
}