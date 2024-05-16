package com.xrs.bluetooth_device;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import com.xrs.bluetooth_device.constant.BleConstant;
import com.xrs.bluetooth_device.function.AlarmTimer;
import com.xrs.bluetooth_device.model.ApnInfo;
import com.xrs.bluetooth_device.model.DeviceDetailsModel;
import com.xrs.bluetooth_device.receiver.CommonAlarmReceiver;
import com.xrs.bluetooth_device.service.NotificationService;
import com.xrs.bluetooth_device.utils.CameraPreview;
import com.xrs.bluetooth_device.utils.CameraUtil;
import com.xrs.bluetooth_device.utils.FileUtils;
import com.xrs.bluetooth_device.utils.LogUtils;
import com.xrs.bluetooth_device.model.WifiListModel;
import com.xrs.bluetooth_device.service.BlueService;
import com.xrs.bluetooth_device.utils.ApnUtil;
import com.xrs.bluetooth_device.utils.BlueToothUtils;
import com.xrs.bluetooth_device.utils.DeviceUtils;
import com.xrs.bluetooth_device.utils.DownloadUtil;
import com.xrs.bluetooth_device.utils.LedUtils;
import com.xrs.bluetooth_device.utils.NetworkUtil;
import com.xrs.bluetooth_device.utils.OrderUtil;
import com.xrs.bluetooth_device.utils.PropertiesUtil;
import com.xrs.bluetooth_device.utils.SharedPreferencedUtils;
import com.xrs.bluetooth_device.utils.UploadUtil;
import com.xrs.bluetooth_device.utils.WifiUtils;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements SensorEventListener,Thread.UncaughtExceptionHandler {
    private static final String TAG = "blueToothUtil_Device:";
    private static String mConnectedDeviceName = null;
    public static BlueService mBlueService = null;
    public static Context sContext;
    public static FrameLayout cameraFrame;
    private Camera mCamera;
    private TextView cameraTv;
    public static ApnUtil apnUtil = new ApnUtil();
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String  DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final LedUtils ledUtils = new LedUtils();
    public static BlueToothUtils blueToothUtils = new BlueToothUtils();
    public static BluetoothAdapter bluetoothAdapter;
    static List<ScanResult> wifiList = new ArrayList<>();
    static List<WifiListModel> wifiListM = new ArrayList<>();
    public static WifiManager wifiManager;
    public static WifiUtils wifiUtil;
    private LocationManager locationManager;
    static Gson gson = new Gson();

    private static final long INTERVAL = 5000; // 5秒
    private Handler handler;
    private MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        sContext = this;
        initView();
/*        handler = new Handler();
        mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
        mediaPlayer.setLooping(true);
        handler.postDelayed(playSoundRunnable, INTERVAL);*/

     /*   initSocket();*/
        
        initBluetooth();
        initWifi();


        startAlarmTimer();
        Toast toast = Toast.makeText(MainActivity.this, BluetoothAdapter.getDefaultAdapter().getAddress(), Toast.LENGTH_LONG);
        showMyToast(toast, 20*1000);
        setDiscoverableTimeout();


    /*    CameraUtil cameraUtil = new CameraUtil(sContext);
        cameraUtil.getPicture(System.currentTimeMillis()+"");*/


/*        CommonAlarmReceiver sOnBroadcastReciver=new CommonAlarmReceiver();
        IntentFilter recevierFilter=new IntentFilter();
        recevierFilter.addAction(Intent.ACTION_SCREEN_ON);
        recevierFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(sOnBroadcastReciver, recevierFilter);*/
        /*AlarmTimer.startIsNetwork(sContext);*/

    ;
    /*    AlarmTimer.startIsNetwork(sContext);

        if (!SharedPreferencedUtils.getBoolean(sContext,"isReboot",false)){
            try {
                DeviceUtils.setSilentShutdown();
                SharedPreferencedUtils.setBoolean(sContext,"isReboot",true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/


     /*   new AppMonitor(this,"com.xrs.watchservice").startMonitoring(); APN = 30304.mcs, MNC=410, MCC=310*/
/*        AlarmTimer.startIsNetwork(sContext)*/
/*       if (DeviceUtils.isSimReady(sContext)){
            int tip = apnUtil.getAPN(sContext,"30304.mcs");

            if (tip > 0){
                apnUtil.setAPN(tip,MainActivity.sContext);
            }else {
                apnUtil.addAPN("30304.mcs","30304.mcs","","","","","310","410",sContext);
                int tipRe = apnUtil.getAPN(sContext,"30304.mcs");
                apnUtil.setAPN(tipRe,MainActivity.sContext);
            }
        }*/

 /*       send_at_to_update_lbs();*/


   /*     LedUtils.RedLedEnable(false);*/
/*
        LedUtils.LedAllEnable(false);
*/
 /*       LogUtils.e("apn",apnUtil.getCurrentAPN(sContext));
        for (ApnInfo a :apnUtil.getApnList(sContext)
             ) {
            LogUtils.e("apn",a.getName()+","+a.getApn()+","+a.getId());
        }*/


       /* send_at_to_reset_simcard(sContext);*/
    /*    new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e("uploadFile", "start : ");
                UploadUtil.uploadFile(FileUtils.getFileByPath(BleConstant.Log_Path),"http://120.76.153.92:10081//api/UploadLocLog");
                FileUtils.deleteFile(BleConstant.Log_Path);
                blueToothUtils.sendMessage("success","success", mBlueService);
                Log.e("update","1");
            }
        }).start();*/
    }


    

    private Runnable playSoundRunnable = new Runnable() {
        @Override
        public void run() {
            mediaPlayer.start();
            handler.postDelayed(this, INTERVAL);
        }
    };
    public static void send_at_to_update_lbs() {
        Intent intent = new Intent("com.eqc.intent.action.getTelephony.lbs");
        intent.putExtra("command", "ATD15062279663;");
        intent.putExtra("arg2", "");
        sContext.sendBroadcast(intent);
    }

    //获取照片中的接口回调




    private void initView() {
        // 设置1像素防止人看见
        setWindowOne();
    }



    private void initSocket() {
        // 初始化链接
        OrderUtil.getInstance().startSocket();
    }

    private void initBluetooth() {
        try {
            MainActivity.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            // 初始化蓝牙开关状态
            BleConstant.Ble_IsOpen = SharedPreferencedUtils.getString(this, "isOpen", "0").equals("1");
            // 尝试打开蓝牙
            blueToothUtils.startBlueEnable(BluetoothAdapter.getDefaultAdapter(), this);
            Ble_Action();
        } catch (Exception e) {
            Log.e("blue", e.toString());
        }
    }

    public static void send_at_to_reset_simcard(Context context) {
        Intent intent = new Intent("com.eqc.intent.action.getTelephony.lbs");
        intent.putExtra("command", "AT+SFUN=2"); //重新初始化sim卡
        intent.putExtra("arg2", "");
        context.sendBroadcast(intent);
        LogUtils.file("send_at_to_reset_simcard ");
    }

    private void initWifi() {
        // 修改wifi策略
        Settings.Secure.putInt(getContentResolver(), Settings.Secure.LOCATION_MODE, 1);

        // 初始化定位
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // 初始化Wifi
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiUtil = new WifiUtils(this);
        /*wifiUtil.openWifi();
        WifiConfiguration wifiConfiguration = wifiUtil.createWifiInfo("TTVCL","123456789aa",3);
        wifiUtil.addNetWork(wifiConfiguration);*/
    }

    private void startAlarmTimer() {
        // 头一次1min一次写入本地日志
        AlarmTimer.startConfirmedBle_Log(this, Long.valueOf(1 * 60 * 1000));
    }

    public void showMyToast(final Toast toast, final int cnt) {
        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                toast.show();
            }
        }, 0, 3000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                toast.cancel();
                timer.cancel();
            }
        }, cnt );
    }

    public static void setDiscoverableTimeout() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode = BluetoothAdapter.class.getMethod("setScanMode", int.class, int.class);
            setScanMode.setAccessible(true);
            setDiscoverableTimeout.invoke(adapter, 0);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, 0);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("Bluetooth", "setDiscoverableTimeout failure:" + e.getMessage());
        }
    }

    private void Ble_Action(){
        //判断是否有网络
        if(!NetworkUtil.isNetworkAvailable(sContext))
        {
            if (SharedPreferencedUtils.getString(sContext,"isOpen","0").equals("1")){
             /*   blueToothUtils.startBlueEnable(MainActivity.bluetoothAdapter,sContext);*/
                //启动蓝牙配置服务
                Log.e("Ble","开机未连接网络,蓝牙并要求打开");
                blueStart();
                AlarmTimer.startConfirmedFrequencyUpload_BLE(sContext);
            }
            else
            {
                Log.e("Ble","开机未连接网络,蓝牙要求关闭");
                blueStart();
                //发送第一个广播
                AlarmTimer.startConfirmedFrequencyUpload_BLE(sContext);
                AlarmTimer.startConfirmedBle_IsOpen(sContext, Long.valueOf(30 * 60 * 1000));
            }
        }
        else
        {
            if (SharedPreferencedUtils.getString(sContext,"isOpen","0").equals("1")){
                Log.e("Ble","开机连接网络,蓝牙要求打开");
                //启动蓝牙配置服务
                blueStart();
                AlarmTimer.startConfirmedFrequencyUpload_BLE(sContext);
            }
            else
            {
                Log.e("Ble","开机连接网络,蓝牙要求关闭");
                blueStart();
                blueToothUtils.startBlueEnable(MainActivity.bluetoothAdapter,sContext);
                AlarmTimer.startConfirmedFrequencyUpload_BLE(sContext);
                AlarmTimer.startConfirmedBle_IsOpen(sContext, Long.valueOf(30 * 60 * 1000));
            }
        }
    }

    public static void sendBroadcast(String command){
        Intent intent = new Intent();
//显示提示窗口
        intent.setAction("com.enqualcomm.support.SMSCMD");
        intent.putExtra("smsCMD", "command");
        sContext.sendBroadcast(intent);
    }

    private void setWindowOne(){
        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.x = 0;
        layoutParams.y = 0;
        layoutParams.width = 1;
        layoutParams.height = 1;
        layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        window.setAttributes(layoutParams);
    }

    private void setWifiNeverSleep(){

        int wifiSleepPolicy=0;

        wifiSleepPolicy=Settings.System.getInt(getContentResolver(),

                android.provider.Settings.System.WIFI_SLEEP_POLICY,

                Settings.System.WIFI_SLEEP_POLICY_DEFAULT);

        System.out.println("---> 修改前的Wifi休眠策略值 WIFI_SLEEP_POLICY="+wifiSleepPolicy);

        Settings.System.putInt(getContentResolver(),

                android.provider.Settings.System.WIFI_SLEEP_POLICY,

                Settings.System.WIFI_SLEEP_POLICY_NEVER);

        wifiSleepPolicy=Settings.System.getInt(getContentResolver(),

                android.provider.Settings.System.WIFI_SLEEP_POLICY,

                Settings.System.WIFI_SLEEP_POLICY_DEFAULT);

        System.out.println("---> 修改后的Wifi休眠策略值 WIFI_SLEEP_POLICY="+wifiSleepPolicy);

    }

    public static void blueStart(){

        Log.e("BLE","2323");
        if (MainActivity.bluetoothAdapter == null)
        {
            Log.e("BLE","kong");
        }
        if (MainActivity.bluetoothAdapter.isEnabled())
        {
            Log.e("BLE","kwwww");
        }
        String imei = DeviceUtils.getIMEI(sContext);
        try {
            MainActivity.bluetoothAdapter.setName("TB"+imei.substring(imei.length()-5));
        }catch (Exception e)
        {
            MainActivity.bluetoothAdapter.setName("TB");
        }

        /*PropertiesUtil.setSystemBleDiscoverable();*/


        if (mBlueService == null){
            if (Looper.myLooper() == null){
                Log.e("thread","试图赋予looper");
                Looper.prepare();
            }
            Log.e("thread","试图启动服务");
            setupChat();
           /* Looper.loop();*/
        }
    }

    public static void setupChat(){
        Log.e("ble:","启动蓝牙服务");
        mBlueService = new BlueService(sContext,mHandler);
        /*mBlueService.start();*/
    }

    public static void setupChat(Context context){
        Log.e("ble:","重启蓝牙服务");
        mBlueService = new BlueService(sContext,mHandler);
        /*mBlueService.start();*/
    }

    private static final Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            try {
                Log.e("msg:",msg.arg1 + "");
                switch (msg.what){
                    case MESSAGE_STATE_CHANGE:
                        switch (msg.arg1){
                            case BlueService.STATE_CONNECTED:
                                Log.e("msg:","STATE_CONNECTED");
                                break;
                            case BlueService.STATE_CONNECTING:
                                Log.e("msg:","STATE_CONNECTING");
                                break;
                            case BlueService.STATE_LISTEN:
                                Log.e("msg:","STATE_LISTEN");
                            case BlueService.STATE_NONE:
                                Log.e("msg:","STATE_NONE");
                                break;
                        }break;
                    case MESSAGE_WRITE:
                        byte[]writeBuf =(byte[])msg.obj;
                        String writeMessage=new String(writeBuf);
                        Log.e("msg:write",writeMessage);
                        break;
                    case MESSAGE_READ:
                        byte[]readBuf =(byte[])msg.obj;
                        String rMessage=new String(readBuf,0,msg.arg1);
                        Log.e("msg:read",rMessage);
                        String[] a = rMessage.split("::::");
                        if (a.length < 2){
                            break;
                        }
                        String Tag = a[0];
                        String readMessage = a[1];
                        switch (Tag){
                            case "led":
                                sendBroadcast("lfjmm#en_cutalarm_repeat#=true#");
                                LedUtils.LedSwitch(readMessage);
                                break;
                            case "MXB_APN":
                                Log.e("msg:apn",readMessage);
                                String[] b = readMessage.split("&");
                                Log.e("msg:",b.length+"");
                                apnUtil.addAPN(b[0],b[1],(b.length > 2 ? b[2]: "")
                                        ,(b.length > 3 ? b[3]: ""),(b.length > 4 ? b[4]: ""),(b.length > 5 ? b[5]: "")
                                        ,(b.length > 6 ? b[6]: ""),(b.length > 7 ? b[7]: ""),KApplication.sContext);
                                Log.e("APN",apnUtil.getCurrentAPN(MainActivity.sContext));
                                int f = apnUtil.getAPN(sContext,b[0]);
                                apnUtil.setAPN(f,MainActivity.sContext);
                                LogUtils.file("action","APN:"+readMessage);
                                blueToothUtils.sendMessage("success","0",mBlueService);
                                break;
                            case "MXB_WIFI_SCAN":

                                Log.e("msg:wifi","2");
                                if(!wifiManager.isWifiEnabled()){
                                    wifiUtil.openWifi();
                                    Thread.sleep(5000);
                                }
                                Log.e("msg:wifi","3");
                                wifiUtil.startScan();//扫描Wife
                                wifiList = wifiUtil.getWifiList();
                                for (ScanResult scanResult : wifiList) {
                                    wifiListM.add(new WifiListModel(scanResult.SSID,scanResult.capabilities));
                                    Log.e("msg:wifi",scanResult.SSID+";"+scanResult.BSSID+";"+scanResult.capabilities);
                                }
                                String jsonStr = gson.toJson(wifiListM.subList(0,9));
                                blueToothUtils.sendMessage("wifi",jsonStr, mBlueService);
                                break;
                            case "MBX_WIFI_CONNECT":
                                Log.e("msg:wifi",readMessage);
                                String[] wi = readMessage.split("&");
                                String ssid = wi[0];
                                String pwd = wi[2];
                                final int type = wifiUtil.getType(wi[1]);
                                try {
                                    if (wifiUtil.isConn())
                                    {
                                        wifiManager.disconnect();
                                    }

                                    wifiManager.reconnect();

                                    if (PropertiesUtil.getSystemProperties("persist.sys.ic.wifi_enable")!="true") {
                                        PropertiesUtil.setSystemProperties("persist.sys.ic.wifi_enable", true);
                                    }
                                    WifiConfiguration wifiConfiguration = wifiUtil.createWifiInfo(wi[0],wi[2],type);
                                    wifiUtil.addNetWork(wifiConfiguration);
/*                                  Runtime.getRuntime().exec("am broadcast -a com.enqualcomm.support.SMSCMD --es smsCmd #wifictl#=switch,1");
*
                                   /* Log.e("open",PropertiesUtil.getSystemProperties("persist.sys.ic.wifi_enable"));*/
                        /*            String cmd = "am broadcast -a com.enqualcomm.support.SMSCMD --es smsCmd #wifictl#=connect" +
                                            ","+ssid+","+pwd+","+"psk";
                                    LogUtils.file("action:","wifi-connect:ssid"+ssid+",pwd"+pwd);
                                    Log.e("open   ",cmd);
                                    Runtime.getRuntime().exec(cmd);*/
                                   /* Runtime.getRuntime().exec("am broadcast -a com.enqualcomm.support.SMSCMD --es smsCmd #wifictl#=connect,1906,15062279663,psk");*/

                                } catch (Exception e) {
                                    LogUtils.file("wifi",e);
                                }
                                /*boolean conn = wifiUtil.isConn();*/
                                blueToothUtils.sendMessage("wifi-connect",true+"", mBlueService);
                                break;
                            case "MBX_BLE_SCAN_TIME":
                                Log.e("msg:scan_time",readMessage);
                                BleConstant.Ble_Scan_Time = readMessage;
                                blueToothUtils.sendMessage("success","0",mBlueService);
                                break;
                            case "shutdown":
                                blueToothUtils.sendMessage("shutdown","1", mBlueService);
                                try {
                                    DeviceUtils.shutdown();
                                    /*Runtime.getRuntime().exec("shutdown -s -t 0");*/

                                } catch (Exception e) {
                                    blueToothUtils.sendMessage("shutdown","2", mBlueService);
                                    e.printStackTrace();
                                }
                                break;
                            case "gps":
                                break;
                            case"blueOpen":
                                Log.e("msg:blueOpen",readMessage);
                                SharedPreferencedUtils.setString(sContext,"isOpen",readMessage);
                                if (!SharedPreferencedUtils.getString(sContext,"isOpen","0").equals("1")){
                                    AlarmTimer.startConfirmedBle_IsOpen(sContext, Long.valueOf(30 * 60 * 1000));
                                }
                                blueToothUtils.sendMessage("success","0",mBlueService);
                                break;
                            case "version":
                                blueToothUtils.sendMessage("version",DeviceUtils.getVersioncode(sContext,"com.xrs.bluetooth_device"),mBlueService);
                                break;
                            case "upgrade":
                                Log.i("msg:","Download start ");
                                blueToothUtils.sendMessage("upgrade","start", mBlueService);
                                String[] duri = readMessage.split("&");
                                String a_name = duri[0];
                                String a_url = duri[1];
                                String a_packName = duri[2];
                                LogUtils.d(a_name+","+a_url);
                                DownloadUtil.getInstance().download(a_url, "data", new DownloadUtil.OnDownloadListener() {
                                    @Override
                                    public void onDownloadSuccess(String path) {
                                 /*       Log.i("msg:","DownloadSuccess");
                                        DeviceUtils.installPackage("data"+"/"+a_name,getApplicationContext());
                                        try {
                                            Thread.sleep(15000);
                                            doStartApplicationWithPackageName(a_packName);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }*/


                                    }

                                    @Override
                                    public void onDownloading(int progress) {
                                        Log.i("msg:","Downloading");
                                        blueToothUtils.sendMessage("upgrade","Downloading", mBlueService);
                                    }

                                    @Override
                                    public void onDownloadFailed() {
                                        Log.d("msg:","DownloadFailed");
                                    }
                                });
                                break;
                            case "upgradeBle111111":
                                new ProcessBuilder()
                                        .command("su")
                                        .start();
                                Log.i("msg:","Download start ");
                                new ProcessBuilder()
                                        .command("mount","-o","remount","system")
                                        .start();
                                blueToothUtils.sendMessage("upgrade","start", mBlueService);
                                DownloadUtil.getInstance().download("https://api.beehome360.com:8443/apk/jiaokao.apk", "data", new DownloadUtil.OnDownloadListener() {
                                    @Override
                                    public void onDownloadSuccess(String path) {
                                        Log.i("msg:","DownloadSuccess");
                                        DeviceUtils.installPackage("data"+"/jiaokao.apk",sContext);
                                    }

                                    @Override
                                    public void onDownloading(int progress) {
                                        Log.i("msg:","Downloading");
                                        blueToothUtils.sendMessage("upgrade","Downloading", mBlueService);
                                    }

                                    @Override
                                    public void onDownloadFailed() {
                                        Log.d("msg:","DownloadFailed");
                                    }
                                });
                                break;
                            case "deviceDetails":
                                String imei = DeviceUtils.getIMEI(sContext);
                                String imsi = DeviceUtils.getSubscriberId(sContext);
                                String iccid = DeviceUtils.getIccid(sContext);
                                String android_version = DeviceUtils.getSystemModel();
                                DeviceDetailsModel deviceDetailsModel = new DeviceDetailsModel(imei,imsi,iccid,android_version);
                                String json = gson.toJson(deviceDetailsModel);
                                blueToothUtils.sendMessage("deviceDetails",json, mBlueService);
                                break;
                            case "upgradeBle":
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e("uploadFile", "start : ");
                                        UploadUtil.uploadFile(FileUtils.getFileByPath(BleConstant.Log_Path),"http://120.76.153.92:10081/api/UploadLocLog");
                                        FileUtils.deleteFile(BleConstant.Log_Path);
                                        blueToothUtils.sendMessage("success","success", mBlueService);
                                        Log.e("update","1");
                                    }
                                }).start();
                                break;
                            default:
                                if (readMessage.contains("ph:")){
                                    String[] m = readMessage.split(":");
                                    String number = m[1];
                                    Uri uri= Uri.parse("tel:"+ number);
                                    Intent intent =new Intent(Intent.ACTION_DIAL,uri);
                                   /* startActivity(intent);*/
                                }
                                break;
                        }
                        break;
                    case MESSAGE_DEVICE_NAME:
                        mConnectedDeviceName=msg.getData().getString(DEVICE_NAME);
                        Log.e("msg:",mConnectedDeviceName);
                        break;
                    case MESSAGE_TOAST:
                        break;
                }
            }catch (Exception exception){
                Log.e("msg:::::",exception+"");
                blueToothUtils.sendMessage("failed","failed", mBlueService);
                LogUtils.file(msg.what,exception.toString());
            }

        }
    };

    private String isNull(String str)
    {
        if (str == null)
            return "";
        else
            return str;
    }

    public void init() {

    }

    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if(mBlueService != null)
            if(mBlueService.getState() == BlueService.STATE_NONE)
                mBlueService.start();
    }
    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }
    @Override
    public void onSensorChanged(SensorEvent event) {

    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
        LogUtils.file("error:", "Uncaught exception: " + ex.getMessage());
    }
}