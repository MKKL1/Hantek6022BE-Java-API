package com.mkkl.hantekapi.communication.adcdata;

import javax.usb.UsbException;

//TODO name
public interface AdcDataListener {
    void onDataReceived(byte[] data);
    void onCompleted(int finalLength);
    void onError(UsbException e);
}
