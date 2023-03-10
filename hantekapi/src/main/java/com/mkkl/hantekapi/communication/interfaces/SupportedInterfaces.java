package com.mkkl.hantekapi.communication.interfaces;

import com.mkkl.hantekapi.communication.interfaces.endpoints.EndpointTypes;

public enum SupportedInterfaces {
    BulkTransfer((byte)0, EndpointTypes.Bulk),
    Iso3072Transfer((byte)1, EndpointTypes.Iso),
    Iso2048Transfer((byte)2, EndpointTypes.Iso),
    Iso1024Transfer((byte)3, EndpointTypes.Iso);

    private final byte interfaceId;
    private final EndpointTypes endpointType;

    SupportedInterfaces(byte interfaceId, EndpointTypes endpointType) {
        this.interfaceId = interfaceId;
        this.endpointType = endpointType;
    }

    public byte getInterfaceId() {
        return interfaceId;
    }

    public EndpointTypes getEndpointType() {
        return endpointType;
    }
}
