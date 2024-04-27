package com.xrs.bluetooth_device;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import android.widget.Switch;

import com.libsocket.constant.TcpConstants;
import com.libsocket.sdk.OkSocket;

import com.xrs.bluetooth_device.constant.PropertiesConstant;
import com.xrs.bluetooth_device.data.GlobalSettings;
import com.xrs.bluetooth_device.function.AlarmTimer;
import com.xrs.bluetooth_device.utils.DeviceUtils;
import com.xrs.bluetooth_device.utils.LogUtils;
import com.xrs.bluetooth_device.utils.PropertiesUtil;
import com.xrs.bluetooth_device.utils.SharedPreferencedUtils;
import com.xrs.bluetooth_device.utils.Utils;



/**
 * @ProjectName: jiaokaodemo
 * @Package: com.xrs.bluetooth_device
 * @ClassName: KApplication
 * @Description:
 * @Author: kotlin
 * @CreateDate: 2022/3/9 16:31
 */
public class KApplication extends MultiDexApplication /*implements KCEventListen*/ {
    private static KApplication instance;
    public static Context sContext;
    // TCP连接状态
    public static boolean bConnect = false;
    // 是否重连标记
    public static boolean bReCon = false;
    public static KApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Utils.init(this);
        LogUtils.e("全局初始化");
        if (sContext == null){
            LogUtils.e("全局初始化111");
        }
        initSDK();
        init();
        Log.e("K", DeviceUtils.getSystemModel());
/*        TcpConstants.DOMAIN = PropertiesUtil.getSystemProperties(PropertiesConstant.Properties_Ip);
        TcpConstants.IP = PropertiesUtil.getSystemProperties(PropertiesConstant.Properties_Ip);
        TcpConstants.PORT = Integer.parseInt(PropertiesUtil.getSystemProperties(PropertiesConstant.Properties_Port));*/
        /*CbtManager
                .getInstance()
                // 初始化
                .init(this)
                // 是否打印相关日志
                .enableLog(true);*/
    }

    //做基本配置
    public void initSDK(){
        GlobalSettings.instance();
        /*        AMapLocationManager.instance().initLocationSDK();//主线程初始化地图*/
        GlobalSettings.instance().saveImei(Utils.getContext());
        GlobalSettings.instance().saveImsi(Utils.getContext());
        AlarmTimer.DEFAULT_Blue_INTERVAL = SharedPreferencedUtils.getLong(sContext,"blueScanTime", Long.valueOf(2 * 60 * 1000));
    }

    //初始化
    public void init(){
        //初始化Okhttp
        OkSocket.initialize(this,true);
        
    }

    public void initNetEase(){

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sContext = base;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
