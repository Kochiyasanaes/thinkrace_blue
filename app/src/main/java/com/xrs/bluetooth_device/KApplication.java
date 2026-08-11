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
import com.xrs.bluetooth_device.utils.ApnUtil;
import com.xrs.bluetooth_device.utils.CrashHandler;
import com.xrs.bluetooth_device.utils.DeviceUtils;
import com.xrs.bluetooth_device.utils.ICLogger;
import com.xrs.bluetooth_device.utils.LogUtils;
import com.xrs.bluetooth_device.utils.PropertiesUtil;
import com.xrs.bluetooth_device.utils.SharedPreferencedUtils;
import com.xrs.bluetooth_device.utils.Utils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;


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
    public static ApnUtil KapnUtil = new ApnUtil();
    private static String Tag = "KApplication";    // TCP连接状态
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
        CrashHandler.getInstance().init(this);
        ICLogger.i("蓝牙工具 全局初始化");
        Security.addProvider(new BouncyCastleProvider());
        if (sContext == null) {
            ICLogger.i( "全局初始化失败");
        }
        try {
            init();
        }catch (Exception e){
            LogUtils.e(Tag,e.toString());
        }
    }

    //初始化
    public void init() {
        GlobalSettings.instance();
        GlobalSettings.instance().saveImei(Utils.getContext());
        GlobalSettings.instance().saveImsi(Utils.getContext());
        AlarmTimer.DEFAULT_Blue_INTERVAL = SharedPreferencedUtils.getLong(sContext, "blueScanTime", Long.valueOf(2 * 60 * 1000));

        //初始化Okhttp
        OkSocket.initialize(this, true);
        Log.e("device:",DeviceUtils.getSystemModel());
        switch (DeviceUtils.getSystemModel()) {
            case "sl8541e_1h10_gofu":
                break;
            case "l009D-US-stk":
            case "l009-US-stk":
                TcpConstants.DOMAIN = PropertiesUtil.getSystemProperties(PropertiesConstant.Properties_Ip);
                TcpConstants.IP = PropertiesUtil.getSystemProperties(PropertiesConstant.Properties_Ip);
                TcpConstants.PORT = Integer.parseInt(PropertiesUtil.getSystemProperties(PropertiesConstant.Properties_Port));

//                if (DeviceUtils.isSimReady(sContext)) {
//                    int tip = KapnUtil.getAPN(sContext, "30304.mcs");
//                    if (tip > 0) {
//                        KapnUtil.setAPN(tip, sContext);
//                    } else {
//                        KapnUtil.addAPN("30304.mcs", "30304.mcs", "", "", "", "", "310", "410", sContext);
//                        int tipRe = MainActivity.apnUtil.getAPN(sContext, "30304.mcs");
//                        Log.e(Tag,"增加apn");
//                        KapnUtil.setAPN(tipRe, sContext);
//                    }
//                }
//                AlarmTimer.startIsNetwork(sContext);
                break;
            // 更多的 case ...
            default:
                TcpConstants.DOMAIN = PropertiesUtil.getSystemProperties(PropertiesConstant.Properties_Ip);
                TcpConstants.IP = PropertiesUtil.getSystemProperties(PropertiesConstant.Properties_Ip);
                TcpConstants.PORT = Integer.parseInt(PropertiesUtil.getSystemProperties(PropertiesConstant.Properties_Port));
//                if (DeviceUtils.isSimReady(sContext)) {
//                    int tip = KapnUtil.getAPN(sContext, "30304.mcs");
//                    if (tip > 0) {
//                        KapnUtil.setAPN(tip, sContext);
//                    } else {
//                        KapnUtil.addAPN("30304.mcs", "30304.mcs", "", "", "", "", "310", "410", sContext);
//                        int tipRe = MainActivity.apnUtil.getAPN(sContext, "30304.mcs");
//                        Log.e(Tag,"增加apn");
//                        KapnUtil.setAPN(tipRe, sContext);
//                    }
//                }
                break;
        }
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
