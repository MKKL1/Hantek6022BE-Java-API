package com.mkkl.hantekapi.communication.interfaces;

import com.mkkl.hantekapi.communication.interfaces.endpoints.BulkEndpoint;
import com.mkkl.hantekapi.communication.interfaces.endpoints.Endpoint;
import com.mkkl.hantekapi.communication.interfaces.endpoints.EndpointTypes;
import com.mkkl.hantekapi.communication.interfaces.endpoints.IsochronousEndpoint;
import org.usb4java.LibUsb;

import javax.usb.*;
import java.util.List;

public class ScopeInterface implements AutoCloseable{
    private Endpoint endpoint;
    private UsbInterface usbInterface;
    private final UsbDevice usbDevice;
    private UsbInterface activeSetting;

    public ScopeInterface(UsbDevice usbDevice) {
        this.usbDevice = usbDevice;
    }

    //TODO high-level usb4java api doesn't provide utility to set interface's active setting
    public void setInterface(SupportedInterfaces supportedInterfaces) throws UsbException {
        this.usbInterface = usbDevice.getActiveUsbConfiguration().getUsbInterface((byte)0);
//        usbInterface.claim();
        setAltSetting(usbInterface, supportedInterfaces.getInterfaceId());

        System.out.println(usbInterface.isActive());

        this.activeSetting = usbInterface.getSetting(supportedInterfaces.getInterfaceId());

        System.out.println(usbInterface.getUsbInterfaceDescriptor().bInterfaceNumber());
        System.out.println(activeSetting.getUsbInterfaceDescriptor().bInterfaceNumber());

        if(supportedInterfaces.getEndpointType() == EndpointTypes.Bulk)
            this.endpoint = new BulkEndpoint(activeSetting);
        else if(supportedInterfaces.getEndpointType() == EndpointTypes.Iso)
            this.endpoint = new IsochronousEndpoint(activeSetting);
        //shouldn't be here
        else throw new RuntimeException("Endpoint type not supported");
    }

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public UsbInterface getUsbInterface() {
        return usbInterface;
    }

    public void claim() throws UsbException {
        activeSetting.claim();
    }

    @Override
    public void close() throws UsbException {
        activeSetting.release();
//        usbInterface.release();
    }

    public void setAltSetting(UsbInterface iface, byte alternateSetting) throws UsbException {
        if (!iface.getUsbConfiguration().equals(usbDevice.getActiveUsbConfiguration())) {
            throw new UsbException("The interface does not belong to the open configuration");
        }
        synchronized (this) {
            UsbControlIrp irp = usbDevice.createUsbControlIrp(
                    (byte)(UsbConst.REQUESTTYPE_TYPE_STANDARD | UsbConst.REQUESTTYPE_RECIPIENT_INTERFACE),
                    UsbConst.REQUEST_SET_INTERFACE,
                    alternateSetting,
                    iface.getUsbInterfaceDescriptor().bInterfaceNumber());
            irp.setData(new byte[0]);
            irp.setAcceptShortPacket(true);
            usbDevice.syncSubmit(irp);
            if (irp.isUsbException()) {
                throw irp.getUsbException();
            }
        }
    }
}
