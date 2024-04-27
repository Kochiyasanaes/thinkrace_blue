package com.libsocket.impl.blockio.threads;

import android.content.Context;

import com.libsocket.impl.LoopThread;
import com.libsocket.impl.abilities.IReader;
import com.libsocket.sdk.connection.abilities.IStateSender;
import com.libsocket.sdk.connection.interfacies.IAction;
import com.libsocket.utils.SL;

import java.io.IOException;


public class DuplexReadThread extends LoopThread {
    private IStateSender mStateSender;

    private IReader mReader;

    public DuplexReadThread(Context context, IReader reader, IStateSender stateSender) {
        super(context, "duplex_read_thread");
        this.mStateSender = stateSender;
        this.mReader = reader;
    }

    @Override
    protected void beforeLoop() {
        mStateSender.sendBroadcast(IAction.ACTION_READ_THREAD_START);
    }

    @Override
    protected void runInLoopThread() throws IOException {
        mReader.read();
    }

    @Override
    protected void loopFinish(Exception e) {
        if (e != null) {
            SL.e("duplex read error,thread is dead with exception:" + e.getMessage());
        }
        mStateSender.sendBroadcast(IAction.ACTION_READ_THREAD_SHUTDOWN, e);
    }
}
