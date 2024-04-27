package com.libsocket.sdk.protocol;

import java.nio.ByteOrder;

public class IWNormalHeaderProtocol implements IHeaderProtocol {

    @Override
    public int getHeaderLength() {
        return 2;
    }

    @Override
    public int getBodyLength(byte[] header, ByteOrder byteOrder) {
        return header.length;
    }
}