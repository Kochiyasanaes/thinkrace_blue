package com.xrs.bluetooth_device.data;

import com.libsocket.sdk.ConnectionInfo;


public class RedirectException extends RuntimeException {
    public ConnectionInfo redirectInfo;

    public RedirectException(ConnectionInfo redirectInfo) {
        this.redirectInfo = redirectInfo;
    }
}
