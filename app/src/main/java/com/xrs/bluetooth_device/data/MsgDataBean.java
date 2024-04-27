package com.xrs.bluetooth_device.data;

import com.libsocket.sdk.bean.ISendable;

import java.nio.charset.Charset;


public class MsgDataBean implements ISendable {
    private String content = "";

    public MsgDataBean(String content) {
        this.content = content;
    }

    @Override
    public byte[] parse() {

        return content.getBytes(Charset.defaultCharset());
    }
}
