package com.libsocket.impl.abilities;

import android.support.annotation.MainThread;
import android.support.annotation.WorkerThread;

import com.libsocket.sdk.OkSocketOptions;


public interface IReader {

    @WorkerThread
    void read() throws RuntimeException;

    @MainThread
    void setOption(OkSocketOptions option);
}
