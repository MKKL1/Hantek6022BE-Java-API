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
        if(!started) {
            started = true;
            HantekDeviceList hantekDeviceList = OscilloscopeManager.findSupportedDevices();

            Assertions.assertNotEquals(hantekDeviceList.getConnections().size(), 0, "No device was connected");
            final Oscilloscope[] oscilloscope = {hantekDeviceList.getConnections().get(0).oscilloscope()};
            final OscilloscopeHandle[] oscilloscopeHandle = {oscilloscope[0].setup()};
            Assertions.assertNotNull(oscilloscope[0], "Failed to get oscilloscope");

            int tries = 20;
            if (!oscilloscope[0].isFirmwarePresent()) {
                oscilloscopeHandle[0].flash_firmware();
                while(oscilloscope[0] == null || !oscilloscope[0].isFirmwarePresent()) {
                    Thread.sleep(100);
                    try {
                        oscilloscope[0] = OscilloscopeManager.findSupportedDevices().getConnections().get(0).oscilloscope();
                        tries--;
                        if(tries == 0) throw new RuntimeException("Failed to find device after firmware flash");
                    } catch (NoSuchElementException e) {
                        oscilloscope[0] = null;
                    }
                }
            }

            InitializeScopeExtension.oscilloscopeHandle = oscilloscope[0].setup();
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
