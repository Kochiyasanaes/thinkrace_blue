package com.libsocket.impl.blockio.threads;

import android.content.Context;

import com.libsocket.impl.LoopThread;
import com.libsocket.impl.abilities.IWriter;
import com.libsocket.sdk.connection.abilities.IStateSender;
import com.libsocket.sdk.connection.interfacies.IAction;
import com.libsocket.utils.SL;

import java.io.IOException;


public class DuplexWriteThread extends LoopThread {
    private IStateSender mStateSender;

    private IWriter mWriter;

    public DuplexWriteThread(Context context, IWriter writer,
                             IStateSender stateSender) {
        super(context, "duplex_write_thread");
        this.mStateSender = stateSender;
        this.mWriter = writer;
    }

    @Override
    protected void beforeLoop() {
        mStateSender.sendBroadcast(IAction.ACTION_WRITE_THREAD_START);
    }

    @Override
    protected void runInLoopThread() throws IOException {
        mWriter.write();
    }

    @Override
    protected void loopFinish(Exception e) {
        if (e != null) {
            SL.e("duplex write error,thread is dead with exception:" + e.getMessage());
        }
        mStateSender.sendBroadcast(IAction.ACTION_WRITE_THREAD_SHUTDOWN, e);
    }

    public boolean isNeedSend() {
        if (mWriter != null) {
            int size = mWriter.queueSize();
            if (size >= 3) {//需要输出了
                return true;
            }
        }
        return false;
    }
}
