package com.mkkl.hantekapi;


import com.mkkl.hantekapi.devicemanager.HantekDeviceList;
import com.mkkl.hantekapi.devicemanager.OscilloscopeManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.NoSuchElementException;

public class InitializeScopeExtension implements BeforeAllCallback, ExtensionContext.Store.CloseableResource {

    private static boolean started = false;
    private static OscilloscopeHandle oscilloscopeHandle;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        System.out.println("before all init");
        if(!started) {
            System.out.println("before all init start");
            started = true;
            HantekDeviceList hantekDeviceList = OscilloscopeManager.findSupportedDevices();

            Assertions.assertNotEquals(hantekDeviceList.getConnections().size(), 0, "No device was connected");
            Oscilloscope oscilloscope = hantekDeviceList.getConnections().get(0).oscilloscope();
            Assertions.assertNotNull(oscilloscope, "Failed to get oscilloscope");
            final OscilloscopeHandle oscilloscopeHandle = oscilloscope.setup();
            Assertions.assertNotNull(oscilloscopeHandle, "Failed to initialize oscilloscope");

            int tries = 20;
            if (!oscilloscope.isFirmwarePresent()) {
                oscilloscopeHandle.flash_firmware();
                while(oscilloscope == null || !oscilloscope.isFirmwarePresent()) {
                    Thread.sleep(100);
                    try {
                        oscilloscope = OscilloscopeManager.findSupportedDevices().getConnections().get(0).oscilloscope();
                        tries--;
                        if(tries == 0) throw new RuntimeException("Failed to find device after firmware flash");
                    } catch (NoSuchElementException e) {
                        oscilloscope = null;
                    }
                }
            }

            InitializeScopeExtension.oscilloscopeHandle = oscilloscope.setup();
        }
    }

    @Override
    public void close() throws Throwable {
        InitializeScopeExtension.getOscilloscopeHandle().close();
    }

    public static OscilloscopeHandle getOscilloscopeHandle() {
        return oscilloscopeHandle;
    }
}
