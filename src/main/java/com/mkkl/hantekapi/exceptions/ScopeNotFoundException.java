package com.mkkl.hantekapi.exceptions;

//TODO should be checked exception
public class ScopeNotFoundException extends OscilloscopeException {
    public ScopeNotFoundException() {
    }

    public ScopeNotFoundException(String s) {
        super(s);
    }
}
