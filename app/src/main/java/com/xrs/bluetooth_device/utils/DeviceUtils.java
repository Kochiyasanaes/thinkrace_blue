package com.xrs.bluetooth_device.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.xrs.bluetooth_device.KApplication;
import com.xrs.bluetooth_device.MainActivity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class DeviceUtils {
    private static Context mContext;

    public static String getImei(Context context){
        mContext = context;
        TelephonyManager tm = (TelephonyManager)mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    public boolean setMobileDataEnable(boolean enable) {
        try {
            Class serviceManager = Class.forName("com.android.internal.telephony.PhoneFactory");
            Method method = serviceManager.getMethod("getDefaultPhone");
            method.setAccessible(true);
            //拿到phoneProxy的实例化对象
            Object phoneProxyObject = method.invoke(serviceManager.newInstance());
            Class phoneProxy = phoneProxyObject.getClass();
            //调用phone方法
            Method method1 = phoneProxy.getMethod("setDataRoamingEnabled",boolean.class);
            method1.setAccessible(true);
            method1.invoke(phoneProxyObject,new Object[]{true});
        } catch (ClassNotFoundException e) {
            LogUtils.d("ClassNotFoundException"+e.getCause());
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            LogUtils.d("NoSuchMethodException"+e.getCause()+e.getMessage()+e.toString());
            e.printStackTrace();
        } catch (InstantiationException e) {
            LogUtils.d("InstantiationException"+e.getCause());
            e.printStackTrace();
        }
        catch (IllegalAccessException e) {
            LogUtils.d("IllegalAccessException"+e.getCause());
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            LogUtils.d("InvocationTargetException"+e.getCause());
            e.printStackTrace();
        } catch (Exception e) {
            LogUtils.d("InvocationTargetException"+e.getCause());
            e.printStackTrace();
        }
        return enable;
    }

    public static String getDeviceInfo(Context context) {
        StringBuffer sb = new StringBuffer();
        sb.append("imei： " + getIMEI(context) + "\n");
        sb.append("imsi： " + getSubscriberId(context) + "\n");
        sb.append("iccid： " + getIccid(context) + "\n");
        sb.append(" -----------------------------------------------------------------------------------------" + "\n");

        Log.d("ConfigActivity", "getConfigInfos : " + sb.toString());

        return sb.toString();
    }

    public static String getIMEI(Context ctx) {
        if (checkPermission(Manifest.permission.READ_PHONE_STATE)) {
            try {
                TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
                String imei = tm.getDeviceId();
                LogUtils.d("getIMEI : " + imei);
                return imei == null ? "" : imei;
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }

    public static String getMacAddress() {
        try {
            // 把当前机器上访问网络的接口存入 List集合中
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!"wlan0".equalsIgnoreCase(nif.getName())) {
                    continue;
                }
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null || macBytes.length == 0) {
                    continue;
                }
                StringBuilder result = new StringBuilder();
                for (byte b : macBytes) {
                    //每隔两个字符加一个:
                    result.append(String.format("%02X:", b));
                }
                if (result.length() > 0) {
                    //删除最后一个:
                    result.deleteCharAt(result.length() - 1);
                }
                return result.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    /**
     * IMSI 全称为 International Mobile Subscriber Identity，中文翻译为国际移动用户识别码。
     * 它是在公众陆地移动电话网（PLMN）中用于唯一识别移动用户的一个号码。在GSM网络，这个号码通常被存放在SIM卡中
     *
     * @return
     */
    public static String getSubscriberId(Context ctx) {

        if (checkPermission(Manifest.permission.READ_PHONE_STATE)) {
            try {
                TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
                if (isSimReady(ctx)) {
                    return tm.getSubscriberId();
                }
            } catch (Exception e) {
            }
        }
        return "";
    }

    /**
     * ICCID:ICC identity集成电路卡标识，这个是唯一标识一张卡片物理号码的
     *
     * @return
     */
    public static String getIccid(Context ctx) {
        TelephonyManager tm = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        if (isSimReady(ctx)) {
            if (ActivityCompat.checkSelfPermission(Utils.getContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return "";
            }
            return tm.getSimSerialNumber();
        }
        return "";
    }

    /**
     * 获取当前手机系统版本号
     *
     * @return  系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号
     *
     * @return  手机型号
     */
    public static String getSystemModel() {
        return android.os.Build.MODEL;
    }


    /**
     * 判断SIM卡是否准备好
     *
     * @param context
     * @return
     */
    public static boolean isSimReady(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            int simState = tm.getSimState();
            if (simState == TelephonyManager.SIM_STATE_READY) {
                return true;
            }
        } catch (Exception e) {
            Log.w("PhoneHelper", "021:" + e.toString());
        }
        return false;
    }

    public static boolean isSystemApplication(Context context, String packageName){
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo packageInfo = manager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS);
            // 1
            if(new File("/data/app/"+packageInfo.packageName+".apk").exists()){
                return true;
            }
            // 2
            if(packageInfo.versionName!=null && packageInfo.applicationInfo.uid>10000){
                return true;
            }
            // 3
            if((packageInfo.applicationInfo.flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM)!=0){
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getBatteryLevel(Context paramContext) {
        Intent intent = paramContext.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        int battery = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if(battery < 10) {
            return "00"+battery;
        } else if(battery < 100){
            return "0"+battery;
        }
        return String.valueOf(battery);
    }

    /**
     * 判断设备是否具有某权限
     */
    public static boolean checkPermission(String permName) {
        PackageManager pm = Utils.getContext().getPackageManager();
        return PackageManager.PERMISSION_GRANTED == pm.checkPermission(permName, Utils.getContext().getPackageName());
    }

    public static void installPackage(String path,Context context) {
        String packageName = context.getApplicationContext().getPackageName();
        try {

            new ProcessBuilder()
                    .command("chmod","666",path)
                    .start();
            new ProcessBuilder()
                    .command("pm", "install", "-r", path)
                    .start();
            BlueToothUtils blueToothUtils = new BlueToothUtils();
            blueToothUtils.sendMessage("upgrade","success", MainActivity.mBlueService);
            Log.e("msg:","install success "+packageName+"/"+path);



        } catch (IOException e) {
            Log.e("msg:","install failed  " + e.toString());
            e.printStackTrace();
        } catch (Exception exception) {
            Log.e("msg:","install failed  " + exception.toString());
            exception.printStackTrace();
        }
    }

    public static int getVersioncode(Context context,String PackageName) throws Exception {
        PackageManager packagemanager = context.getPackageManager();
        PackageInfo packinfo = packagemanager.getPackageInfo(PackageName,0);
        int version = packinfo.versionCode;
        return version;
    }

    public static void shutdown() {
        try {
            Log.e("test", "do shutdown!");
            //获得ServiceManager类
            Class ServiceManager = Class
                    .forName("android.os.ServiceManager");

            //获得ServiceManager的getService方法
            Method getService = ServiceManager.getMethod("getService", java.lang.String.class);

            //调用getService获取RemoteService
            Object oRemoteService = getService.invoke(null, Context.POWER_SERVICE);

            //获得IPowerManager.Stub类
            Class cStub = Class
                    .forName("android.os.IPowerManager$Stub");
            //获得asInterface方法
            Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
            //调用asInterface方法获取IPowerManager对象
            Object oIPowerManager = asInterface.invoke(null, oRemoteService);
            //获得shutdown()方法
      /*      Method shutdown = oIPowerManager.getClass().getMethod("shutdown", boolean.class,String.class, boolean.class);
            //调用shutdown()方法
            shutdown.invoke(oIPowerManager, false, "1",true);*/
            Method shutdown = oIPowerManager.getClass().getMethod("shutdown", boolean.class, boolean.class);
            //调用shutdown()方法
            shutdown.invoke(oIPowerManager, false, true);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ss",e.toString());
        }
    }

    public static void reboot() {
        try {
            Log.e("test", "do reboot!");
            // 获取ServiceManager类
            Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");

            // 获取ServiceManager的getService方法
            Method getServiceMethod = serviceManagerClass.getMethod("getService", String.class);

            // 调用getService获取PowerManagerService
            Object powerManagerService = getServiceMethod.invoke(null, Context.POWER_SERVICE);

            // 获取IPowerManager.Stub类
            Class<?> powerManagerStubClass = Class.forName("android.os.IPowerManager$Stub");

            // 获取asInterface方法
            Method asInterfaceMethod = powerManagerStubClass.getMethod("asInterface", android.os.IBinder.class);

            // 调用asInterface方法获取IPowerManager对象
            Object powerManager = asInterfaceMethod.invoke(null, powerManagerService);

            // 获取reboot()方法
            Method rebootMethod = powerManager.getClass().getMethod("reboot", boolean.class, String.class, boolean.class);

            // 调用reboot()方法执行重启操作
            rebootMethod.invoke(powerManager, false, null, false);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ss", e.toString());
        }
    }

    public static void setSilentShutdown(Context context) {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                // 设置静音
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

                // 执行重启
                PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (powerManager != null) {
                    powerManager.reboot(null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ss", e.toString());
        }
    }
}
