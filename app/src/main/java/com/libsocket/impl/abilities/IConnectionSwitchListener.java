package com.libsocket.impl.abilities;

import com.libsocket.sdk.ConnectionInfo;
import com.libsocket.sdk.connection.IConnectionManager;


public interface IConnectionSwitchListener {
    void onSwitchConnectionInfo(IConnectionManager manager, ConnectionInfo oldInfo, ConnectionInfo newInfo);
}
