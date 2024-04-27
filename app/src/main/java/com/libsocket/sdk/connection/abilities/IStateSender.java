package com.libsocket.sdk.connection.abilities;

import java.io.Serializable;


public interface IStateSender {

    void sendBroadcast(String action, Serializable serializable);

    void sendBroadcast(String action);
}
