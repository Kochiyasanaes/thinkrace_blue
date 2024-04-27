package com.libsocket.impl.blockio.io;

import android.support.annotation.MainThread;

import com.libsocket.impl.abilities.IReader;
import com.libsocket.sdk.OkSocketOptions;
import com.libsocket.sdk.connection.abilities.IStateSender;

import java.io.InputStream;


public abstract class AbsReader implements IReader {

    protected OkSocketOptions mOkOptions;

    protected IStateSender mStateSender;

    protected InputStream mInputStream;

    public AbsReader(InputStream inputStream, IStateSender stateSender) {
        mStateSender = stateSender;
        mInputStream = inputStream;
    }

    @Override
    @MainThread
    public void setOption(OkSocketOptions option) {
        mOkOptions = option;
    }
}
