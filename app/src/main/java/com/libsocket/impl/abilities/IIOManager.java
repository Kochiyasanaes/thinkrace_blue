package com.libsocket.impl.abilities;

import com.libsocket.sdk.OkSocketOptions;
import com.libsocket.sdk.bean.ISendable;


public interface IIOManager {
    void resolve();

    void setOkOptions(OkSocketOptions options);

    void send(ISendable sendable);

    void close();

}
