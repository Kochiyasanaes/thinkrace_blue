package com.libsocket.impl;

import android.os.Handler;
import android.os.Message;

import com.libsocket.impl.exceptions.PurifyException;
import com.libsocket.sdk.OkSocket;
import com.libsocket.sdk.connection.IConnectionManager;
import com.libsocket.utils.ActivityStack;
import com.libsocket.utils.SL;
import com.libsocket.utils.ScreenListener;

import java.util.ArrayList;
import java.util.List;


public class EnvironmentalManager implements ScreenListener.ScreenStateListener{
    public static final long DELAY_CONNECT_MILLS = 1000;
    private ScreenListener mScreenListener;

    private static class InstanceHolder {
        private static EnvironmentalManager INSTANCE = new EnvironmentalManager();
    }

    /**
     * 后台存活时间(毫秒)
     * -1为永久存活,取值范围[1000,Long.MAX]
     */
    private long mBackgroundLiveMills = -1;

    private ManagerHolder mHolder;

    private boolean isInit;

    private List<IConnectionManager> mPurifyList = new ArrayList<>();

    private boolean isPurify = false;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    isPurify = true;
                    mPurifyList.clear();
                    List<IConnectionManager> list = mHolder.getList();
                    for (IConnectionManager manager : list) {
                        manager.disconnect(new PurifyException("environmental disconnect"));
                    }
                    mPurifyList.addAll(list);
                    break;
            }
            return false;
        }
    });

    private EnvironmentalManager() {

    }

    public static EnvironmentalManager getIns() {
        return InstanceHolder.INSTANCE;
    }

    public void init(ManagerHolder holder) {
        if (isInit) {
            return;
        }
        isInit = true;
        this.mHolder = holder;
        ActivityStack.addStackChangedListener(mChangedAdapter);
        mBackgroundLiveMills = -1;
        registerScreenListener();
    }

    public void unregisterListener() {
        if(mScreenListener != null){
            mScreenListener.unregisterListener();
        }
    }

    private void registerScreenListener() {
        if(mScreenListener == null){
            mScreenListener = new ScreenListener(OkSocket.getContext()) ;
        }
        mScreenListener.begin(this);
    }

    @Override
    public void onScreenOn() {
        SL.i("屏幕开");
    }

    @Override
    public void onScreenOff() {
        SL.i("屏幕关");
    }

    @Override
    public void onUserPresent() {
        SL.i("解锁");
        for (IConnectionManager manager : mPurifyList) {
            if(!manager.isConnect())
                manager.connect();
        }
    }

    private ActivityStack.OnStackChangedAdapter mChangedAdapter = new ActivityStack.OnStackChangedAdapter() {
        @Override
        public void onAppPause() {
            mHandler.removeCallbacksAndMessages(null);
            if (mBackgroundLiveMills > 0) {
                long backLiveMills = mBackgroundLiveMills;
                backLiveMills = backLiveMills < DELAY_CONNECT_MILLS ? DELAY_CONNECT_MILLS : backLiveMills;
                mHandler.sendEmptyMessageDelayed(0, backLiveMills);
            }
        }

        @Override
        public void onAppResume() {
            mHandler.removeCallbacksAndMessages(null);
            if (isPurify) {
                isPurify = false;
                restore();
            }
        }
    };

    private void restore() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (IConnectionManager manager : mPurifyList) {
                    manager.connect();
                }
            }
        }, DELAY_CONNECT_MILLS);

    }

    public void setBackgroundLiveMills(long backgroundLiveMills) {
        mBackgroundLiveMills = backgroundLiveMills;
    }

    public long getBackgroundLiveMills() {
        return mBackgroundLiveMills;
    }
}
