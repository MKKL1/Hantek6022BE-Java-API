package com.mkkl.hantekapi;

public enum Scopes {
    DSO6021(0x6021),
    DSO6022BE(0x6022),
    DSO6022BL(0x602A);
    private final short productId;

    Scopes(int productId) {
        this.productId = (short) productId;
    }

    public short getProductId() {
        return productId;
    }
}
