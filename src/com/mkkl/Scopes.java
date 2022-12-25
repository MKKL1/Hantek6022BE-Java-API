package com.mkkl;

public enum Scopes {
    //TODO find ids
    DDS120(0x0),
    DSO6021(0x6021),
    DSO6022BE(0x6022),
    DSO6022BL(0x602A),
    fx2lib(0x0),
    modded(0x0),
    stock(0x0);

    private final int productId;

    Scopes(int productId) {
        this.productId = productId;
    }
}
