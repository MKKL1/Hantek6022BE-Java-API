package com.mkkl.hantekapi.communication.interfaces;

import com.mkkl.hantekapi.communication.interfaces.endpoints.EndpointType;

public enum UsbInterfaceType {
    BulkTransfer((byte)0, EndpointType.Bulk),
    Iso3072Transfer((byte)1, EndpointType.Iso),
    Iso2048Transfer((byte)2, EndpointType.Iso),
    Iso1024Transfer((byte)3, EndpointType.Iso);

    private final byte interfaceId;
    private final EndpointType endpointType;

    UsbInterfaceType(byte interfaceId, EndpointType endpointType) {
        this.interfaceId = interfaceId;
        this.endpointType = endpointType;
    }

    public byte getInterfaceId() {
        return interfaceId;
    }

    public EndpointType getEndpointType() {
        return endpointType;
    }
}
