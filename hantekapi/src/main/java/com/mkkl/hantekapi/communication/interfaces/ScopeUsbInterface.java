package com.mkkl.hantekapi.communication.interfaces;

import com.mkkl.hantekapi.UsbDevice;
import com.mkkl.hantekapi.communication.interfaces.endpoints.BulkEndpoint;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;
import com.mkkl.hantekapi.communication.interfaces.endpoints.EndpointType;
import com.mkkl.hantekapi.communication.interfaces.endpoints.IsochronousEndpoint;
import org.usb4java.*;

public class ScopeUsbInterface {
    private Endpoint endpoint;
    private final UsbDevice usbDevice;
    private final DeviceHandle deviceHandle;
    private Interface usbInterface;
    private InterfaceDescriptor interfaceDescriptor;
    private final byte interfaceId = 0;

    public ScopeUsbInterface(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
        this.deviceHandle = usbDevice.getDeviceHandle();
    }

    public void setInterface(UsbInterfaceType usbInterfaceType) throws LibUsbException {
        ConfigDescriptor configDescriptor = usbDevice.getConfigDescriptor((byte) 0);
        usbInterface = configDescriptor.iface()[interfaceId];
        interfaceDescriptor = usbInterface.altsetting()[usbInterfaceType.getInterfaceId()];//TODO check

        if(usbInterfaceType.getInterfaceId() != 0) {
            //TODO setting alternate interface doesn't work
            int result = LibUsb.setInterfaceAltSetting(deviceHandle, interfaceId, usbInterfaceType.getInterfaceId());
            if (result < 0) throw new LibUsbException("Failed to set alternate setting on interface", result);
        }

        if(usbInterfaceType.getEndpointType() == EndpointType.Bulk)
            this.endpoint = new BulkEndpoint(deviceHandle, interfaceDescriptor);
        else if(usbInterfaceType.getEndpointType() == EndpointType.Iso)
            this.endpoint = new IsochronousEndpoint(deviceHandle, interfaceDescriptor);
        //shouldn't be here
        else throw new RuntimeException("Endpoint type not supported");
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public Interface getInterface() {
        return usbInterface;
    }

    public InterfaceDescriptor getInterfaceDescriptor() {
        return interfaceDescriptor;
    }

    public void claim() throws LibUsbException {
        int result = LibUsb.claimInterface(deviceHandle, interfaceId);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to claim interface", result);
    }

    public void close() throws LibUsbException {
        int result = LibUsb.releaseInterface(deviceHandle, interfaceId);
        if (result != LibUsb.SUCCESS) throw new LibUsbException("Unable to release interface", result);
    }
}
