package com.xrs.bluetooth_device;

import static android.location.LocationManager.GPS_PROVIDER;

import static com.google.gson.internal.bind.TypeAdapters.UUID;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.net.sip.SipManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.telephony.IccOpenLogicalChannelResponse;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.internal.telephony.CommandsInterface;
import com.google.gson.Gson;

import com.ic.api.Api;
import com.ic.api.ApiCreator;

import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationManagerOptions;
import com.tencent.map.geolocation.TencentLocationRequest;
import com.xrs.BluetoothManager;
import com.xrs.bluetooth_device.constant.BleConstant;
import com.xrs.bluetooth_device.constant.PropertiesConstant;
import com.xrs.bluetooth_device.function.AlarmTimer;
import com.xrs.bluetooth_device.model.ApnInfo;
import com.xrs.bluetooth_device.model.DeviceDetailsModel;
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


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pub.devrel.easypermissions.EasyPermissions;

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

    private TextToSpeech tts;

    private BluetoothManager bluetoothManager;
    private BlueConnectThread blueConnectThread;
    private static final String TEST_URL = "http://[2607:f8b0:4004:804::200e]";
    SipManager mSipManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        sContext = this;
        wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        wifiUtil = new WifiUtils(this);
        initView();
//        initSocket();
        initBluetooth();
        if (!DeviceUtils.getSystemModel().contains("MT4")){
            initWifi();
        }
        setDiscoverableTimeout();
        PropertiesUtil.setSystemProperties("persist.sys.sosmode",false);
        
        LogUtils.e("BLe",""+DeviceUtils.getSystemModel());
        // 初始化Wifi

        PropertiesUtil.setSystemProperties("persist.sys.ic.wifi_enable","true");

//        startActivity(new Intent(this, BluetoothScanActivity.class));
//        fetchPublicIP();
//        SSLContext ctx = null;
//
//        try {
//            ctx = SSLContext.getInstance("TLS");
//            ctx.init(null, null, null);
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        } catch (KeyManagementException e) {
//            throw new RuntimeException(e);
//        }

//        SSLEngine e = ctx.createSSLEngine();
//        Log.d("TLS", "supported=" + Arrays.toString(e.getSupportedProtocols()));
//        Log.d("TLS", "enabled=" + Arrays.toString(e.getEnabledProtocols()));
//
//
//        TelephonyManager tm =
//                (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//
//        try {
//            boolean ok = false;
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//                ok = tm.hasCarrierPrivileges();
//            }
//            Log.d(TAG, "hasCarrierPrivileges=" + ok);
//
//            if (ok) {
//                IccOpenLogicalChannelResponse resp = null;
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    resp = tm.iccOpenLogicalChannel("A0000000031010");
//                }
//                Log.d(TAG, "iccOpenLogicalChannel=" + resp);
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    if (resp != null && resp.getChannel() >= 0) {
//                        boolean closed = false;
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                            closed = tm.iccCloseLogicalChannel(resp.getChannel());
//                        }
//                        Log.d(TAG, "iccCloseLogicalChannel=" + closed);
//                    }
//                }
//            }
//        } catch (SecurityException e1) {
//            Log.d(TAG, "SecurityException=" + e1.getMessage());
//        } catch (Exception e2) {
//            Log.d(TAG, "Exception=" + Log.getStackTraceString(e2));
//        }

        // 1. 拿到 TencentLocationManager
  //        Intent intent = new Intent("android.intent.action.MAIN");
//        intent.setComponent(new ComponentName(
//                "com.google.android.gms",
//                "org.microg.nlp.ui.BackendSettingsActivity"));
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        apnUtil.getApnList(this);


//        quickNetLoc();
        if (PropertiesUtil.getSystemProperties("persist.sys.isopenapn","0").equals("1")){
            if (DeviceUtils.isSimReady(sContext)) {
                int tip = apnUtil.getAPN(sContext,"30304.mcs");
                if (tip > 0){
                    apnUtil.setAPN(tip,MainActivity.sContext);
                }else {
                    apnUtil.addAPN("30304.mcs","30304.mcs","","","","","310","410",sContext);
                    int tipRe = apnUtil.getAPN(sContext,"30304.mcs");
                    apnUtil.setAPN(tipRe,MainActivity.sContext);
                    }
            }
            AlarmTimer.startIsNetwork(sContext);
        }


//        Intent intent = new Intent();
//        intent.setClassName("mobile.miki", "mobile.miki.mainui.TestMainList");
//
//        try {
//            // 启动目标Activity
//            startActivity(intent);
//        } catch (ActivityNotFoundException e) {
//            // 如果目标Activity不存在，显示提示信息
//            Toast.makeText(this, "目标应用未安装或无法启动", Toast.LENGTH_SHORT).show();
//        }
   /*     bluetoothManager = new BluetoothManager();

        String deviceAddress = "14:99:3E:5C:C0:11"; // 替换为实际的蓝牙设备地址
        blueConnectThread = bluetoothManager.connectToDevice(deviceAddress);
        if (blueConnectThread == null) {
            Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "连接中...", Toast.LENGTH_SHORT).show();
        }*/
//        Intent installIntent = new Intent();
//        installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
//        startActivity(installIntent);
//        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if (status == TextToSpeech.SUCCESS) {
//                    int result = tts.setLanguage(Locale.CHINA);
//                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
//                        Toast.makeText(MainActivity.this, "语言数据缺失或不支持", Toast.LENGTH_SHORT).show();
//                    } else {
//                        tts.speak("你好，欢迎使用语音合成功能！", TextToSpeech.QUEUE_FLUSH, null, null);
//                    }
//                } else {
//                    Toast.makeText(MainActivity.this, "TTS 初始化失败", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        int result = tts.isLanguageAvailable(Locale.CHINA);
//        if (result == TextToSpeech.LANG_MISSING_DATA) {
//            Log.e("tt", "设备缺少中文语音数据");
//        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
//            Log.e("tt", "设备不支持中文语音合成");
//        } else if (result == TextToSpeech.LANG_AVAILABLE) {
//            Log.e("tt", "设备支持中文语音合成");
//        } else if (result == TextToSpeech.LANG_COUNTRY_AVAILABLE) {
//            Log.e("tt", "设备支持中文（中国）语音合成");
//        } else if (result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
//            Log.e("tt", "设备支持中文（中国）变体语音合成");
//        } else {
//            Log.e("tt", "未知返回值: " + result);
//        }

//        Set<Locale> availableLanguages = tts.getAvailableLanguages();
//        Log.e("tt", "支持的语言列表: " + availableLanguages);
  /*      tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.CHINA);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(MainActivity.this, "语言数据缺失或不支持", Toast.LENGTH_SHORT).show();
                    } else {
                        tts.speak("你好，欢迎使用语音合成功能！", TextToSpeech.QUEUE_FLUSH, null, null);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "TTS 初始化失败", Toast.LENGTH_SHORT).show();
                }
            }
        });*/
        
//        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//        this.startActivity(intent);
//        Settings.Secure.setLocationProviderEnabled(getContentResolver(), "gps", true);
//        Settings.Secure.putInt(this.getContentResolver(), Settings.Secure.LOCATION_MODE, Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);

//        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
//        startActivity(intent);
//        Intent intent = new Intent(Intent.ACTION_DIAL);
//        // 可以通过 data 属性设置默认的电话号码，这里以 "tel:" 开头
//        // 如果不设置电话号码，拨号盘将打开并清空
//        intent.setData(Uri.parse("tel:"));
//        // 启动Intent
//        startActivity(intent);
//        Notification notification = new NotificationCompat.Builder(this, "CHANNEL_ID")
//                .setSmallIcon(R.drawable.ic_launcher_background) // 设置通知小图标
//                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher_background)) // 设置通知大图标
//                .setContentTitle("通知标题") // 设置通知标题
//                .setContentText("这是一条通知内容") // 设置通知内容
//                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // 设置通知优先级
//                .build();
//        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel("CHANNEL_ID", "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
//            channel.setDescription("Channel description");
//            notificationManager.createNotificationChannel(channel);
//        }
//        notificationManager.notify(1, notification);
/*        handler = new Handler();
        mediaPlayer = MediaPlayer.create(this, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI);
        mediaPlayer.setLooping(true);
        handler.postDelayed(playSoundRunnable, INTERVAL);*/
//        MicrophoneTest microphoneTest = new MicrophoneTest();
//        microphoneTest.start();

////        startAlarmTimer();
//        Toast toast = Toast.makeText(MainActivity.this, BluetoothAdapter.getDefaultAdapter().getAddress(), Toast.LENGTH_LONG);
//        showMyToast(toast, 20*1000);

//
//        Intent intent = new Intent();
//        // 检查设备是否支持Wi-Fi
//
//
/*
        Settings.Secure.setLocationProviderEnabled(getContentResolver(), GPS_PROVIDER, true);
*/
        Log.e("ss ",PropertiesUtil.getSystemProperties(PropertiesConstant.Properties_Ip));
/*        if (SharedPreferencedUtils.getInteger(sContext,"rootTime",0) < 500) {
            try {
                SharedPreferencedUtils.setInteger(sContext, "rootTime", SharedPreferencedUtils.getInteger(sContext,"rootTime",0) + 1);
                DeviceUtils.setSilentShutdown(sContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/

   /*     Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);*/
        /*test();*/


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
        disableNotifications(this);

     /*   new AppMonitor(this,"com.xrs.watchservice").startMonitoring(); APN = 30304.mcs, MNC=410, MCC=310*/
//        AlarmTimer.startIsNetwork(sContext);
//       if (DeviceUtils.isSimReady(sContext)){
//            int tip = apnUtil.getAPN(sContext,"30304.mcs");
//
//            if (tip > 0){
//                apnUtil.setAPN(tip,MainActivity.sContext);
//            }else {
//                apnUtil.addAPN("30304.mcs","30304.mcs","","","","","310","410",sContext);
//                int tipRe = apnUtil.getAPN(sContext,"30304.mcs");
//                apnUtil.setAPN(tipRe,MainActivity.sContext);
//            }
//        }

 /*       send_at_to_update_lbs();*/


   /*     LedUtils.RedLedEnable(false);*/
/*
        LedUtils.LedAllEnable(false);
*/

//        for (ApnInfo a :apnUtil.getApnList(sContext)
//             ) {
//            LogUtils.e("apn",a.getName()+","+a.getApn()+","+a.getId());
//        }


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
//        new Handler().postDelayed(() -> quickNetLoc(), 1000);
        
    }


    public void fetchPublicIP() {
        new Thread(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL("https://www.showmyip.com");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder content = new StringBuilder();
                    String inputLine;
                    while ((inputLine = reader.readLine()) != null) {
                        content.append(inputLine);
                    }
//                    JSONObject jsonObject = new JSONObject(content.toString());
                    String publicIP = content.toString();
                    Log.e("test","Public IP Address: " + publicIP);
                    // 在这里处理获取到的公网 IP 地址
                } else {
                    System.out.println("Failed to get IP");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }


    public void test() {
        Executor executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                Api api = ApiCreator.getInstance();
                //开启心率血氧的测试
                boolean enablePPG = api.enablePPG();

                Log.d(TAG, "enablePPG ==> " + enablePPG);

                boolean finish = false;
                int testCount = 0;
                while (!finish) {
                    Log.d(TAG, "enablePPG ==> " + api.getStepCount());

                }
            }
        });
    }

    public static void disableNotifications(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For Android O and above
            String channelId = "your_channel_id"; // Replace with your actual channel ID
            notificationManager.deleteNotificationChannel(channelId);
        } else {
            // For older versions
            notificationManager.cancelAll();
        }
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
            Boolean isOpenBle = PropertiesUtil.getSystemProperties("persist.sys.ble.enable","1").equals("1");
            if (isOpenBle/* && (BleConstant.Ble_IsOpen || !DeviceUtils.getSystemModel().contains("HK"))*/){
                // 尝试打开蓝牙
                blueToothUtils.startBlueEnable(BluetoothAdapter.getDefaultAdapter(), this);
                Ble_Action();
            }else { 
                BluetoothAdapter.getDefaultAdapter().disable();
            }

        } catch (Exception e) {
            Log.e("blue", e.toString());
        }
    }

    public static void send_at_to_reset_simcard(Context context) {
        Intent intent = new Intent("com.eqc.intent.action.getTelephony.lbs");
        intent.putExtra("command", "AT+CSQ"); //重新初始化sim卡
        intent.putExtra("arg2", "");
        context.sendBroadcast(intent);
        LogUtils.file("send_at_to_reset_simcard ");
    }

    private void initWifi() {



        wifiUtil.openWifi();
        WifiConfiguration wifiConfiguration = wifiUtil.createWifiInfo("traxbean","88888888",3);
        wifiUtil.addNetWork(wifiConfiguration);
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
            if (SharedPreferencedUtils.getString(sContext,"isOpen","0").equals("1")|| (DeviceUtils.getSystemModel().contains("CRC")|| DeviceUtils.getSystemModel().contains("HK")|| DeviceUtils.getSystemModel().contains("MZT"))){
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
            if (SharedPreferencedUtils.getString(sContext,"isOpen","0").equals("1") || (DeviceUtils.getSystemModel().contains("CRC") || DeviceUtils.getSystemModel().contains("HK")|| DeviceUtils.getSystemModel().contains("MZT"))){
                //启动蓝牙配置服务
                Log.e("blue",":跳过广播");
                blueStart();
                AlarmTimer.startConfirmedFrequencyUpload_BLE(sContext);
            }
            else
            {
                blueStart();
                AlarmTimer.startConfirmedFrequencyUpload_BLE(sContext);
                AlarmTimer.startConfirmedBle_IsOpen(sContext, Long.valueOf(30 * 60 * 1000));
            }
        }
    }

    public int[] readSptGpio() {
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader("/sys/bus/i2c/devices/2-0033/spt_gpio"));
            String line = reader.readLine();
            reader.close();

            String[] parts = line.trim().split(" ");
            int gpioLevel = Integer.parseInt(parts[0]);  // 0=佩戴, 1=未佩戴
            int sptStatus = Integer.parseInt(parts[1]);  // 内部状态

            return new int[]{gpioLevel, sptStatus};
        } catch (Exception e) {
            return null;
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