package com.libsocket.impl.abilities;

import com.libsocket.sdk.OkSocketOptions;
import com.libsocket.sdk.bean.ISendable;


public interface IWriter {
    boolean write() throws RuntimeException;

    void setOption(OkSocketOptions option);

    void offer(ISendable sendable);

    int queueSize();

}
