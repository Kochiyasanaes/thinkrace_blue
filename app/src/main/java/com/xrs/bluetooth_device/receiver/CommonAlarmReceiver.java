package com.xrs.bluetooth_device.receiver;

import static android.content.Intent.ACTION_BOOT_COMPLETED;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.nfc.Tag;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.libsocket.constant.SPConstant;
import com.libsocket.sdk.OkSocket;
import com.libsocket.sdk.connection.IConnectionManager;
import com.xrs.bluetooth_device.KApplication;
import com.xrs.bluetooth_device.MainActivity;
import com.xrs.bluetooth_device.R;
import com.xrs.bluetooth_device.constant.BleConstant;
import com.xrs.bluetooth_device.constant.ReceiverConstant;
import com.xrs.bluetooth_device.data.GlobalSettings;
import com.xrs.bluetooth_device.data.HandShake;
import com.xrs.bluetooth_device.data.MsgType;
import com.xrs.bluetooth_device.function.AlarmTimer;
import com.xrs.bluetooth_device.service.BlueService;
import com.xrs.bluetooth_device.utils.ApnUtil;
import com.xrs.bluetooth_device.utils.CameraPreview;
import com.xrs.bluetooth_device.utils.CameraUtil;
import com.xrs.bluetooth_device.utils.DeviceUtils;
import com.xrs.bluetooth_device.utils.FileUtil;
import com.xrs.bluetooth_device.utils.ImageUploader;
import com.xrs.bluetooth_device.utils.LogUtils;
import com.xrs.bluetooth_device.utils.NetworkUtil;
import com.xrs.bluetooth_device.utils.OrderUtil;
import com.xrs.bluetooth_device.utils.PropertiesUtil;
import com.xrs.bluetooth_device.utils.SPUtils;
import com.xrs.bluetooth_device.utils.SharedPreferencedUtils;
import com.xrs.bluetooth_device.utils.WifiUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Timer;
import java.util.TimerTask;

public class CommonAlarmReceiver extends BroadcastReceiver {
    public static List<String>  offlineMessageList = new ArrayList<>();
    String wifiStr = "";
    ImageUploader imageUploader = new ImageUploader();
    Map<String,BluetoothDevice> map = new HashMap<>();
    static Boolean isReSim = false;
    static int i = 0;
    static Boolean isWifiCon = false;
    static int tip = 0;
    public static Boolean isNetWork = true;
    String MsgText = "";
    static boolean isCharge = false;
    @Override
    public void onReceive(final Context context, Intent intent) {
        int  networkType= SPUtils.getInstance().getInt("NetworkType",-1);
        LogUtils.e("BLe","111");
        String action = intent.getAction();
        String logTxt = " CommonAlarmReceiver " + action;
        Log.e("cmd_receive",logTxt);
        IConnectionManager iConnectionManager = OrderUtil.getInstance().getIConnectionManager();
        try {
            switch (action) {
                case ReceiverConstant.ACTION_CONNECTIVITY_CHANGE: //网络变化

                    LogUtils.e( "网络状态改变:111"+ NetworkUtil.isNetworkAvailable(context));
                    //获得网络连接服务
                    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
                    //获取wifi连接状态

                    NetworkInfo.State state = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
                    //判断是否正在使用wifi网络
                    Handler handler = new Handler();
                    if (NetworkUtil.isNetworkAvailable(context)) {
                        i = 0;
                        Log.e("网络","有网");
                        isReSim = false;
                        isNetWork = true;
                        SharedPreferencedUtils.setBoolean(context,"isReboot",false);
                        PropertiesUtil.setSystemProperties("persist.sys.server_connected",true);
                        Log.e("网络类型",NetworkUtil.getNetworkType(context) + "");
                        if (NetworkUtil.getNetworkType(context)==1 && !isWifiCon){
                            String content = MsgType.IWAPWL
                                    + GlobalSettings.MSG_CONTENT_SEPERATOR
                                    +GlobalSettings.instance().getImei()
                                    +GlobalSettings.MSG_CONTENT_SEPERATOR
                                    +"1"
                                    +GlobalSettings.MSG_CONTENT_SEPERATOR
                                    +MainActivity.wifiUtil.getSSID()
                                    +"#";
                            Log.e("wifi","转变为wifi连接："+content);
                            OrderUtil.getInstance().sendMsgRe(content);
                            isWifiCon = true;
                        }else if (isWifiCon && NetworkUtil.getNetworkType(context)!=1){
                            String content = MsgType.IWAPWL
                                    + GlobalSettings.MSG_CONTENT_SEPERATOR
                                    +GlobalSettings.instance().getImei()
                                    +GlobalSettings.MSG_CONTENT_SEPERATOR
                                    +"0"
                                    +GlobalSettings.MSG_CONTENT_SEPERATOR
                                    +"#";
                            Log.e("wifi","转变非wifi连接："+content);
                            OrderUtil.getInstance().sendMsgRe(content);
                            isWifiCon = false;
                        }
                        handler.removeCallbacksAndMessages(null);
                    }else {
                            isNetWork = false;
                            isReSim = true;
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    LogUtils.e("是否准备了Sim卡：" + isReSim);
                                    if (isReSim) {
                                        LogUtils.e("开始重置 sim 卡");
                                        send_at_to_reset_simcard(context);
                                    }
                                    handler.postDelayed(this, 60 * 1000); // 每分钟执行一次任务
                                }
                            };
                            handler.postDelayed(runnable, 60 * 1000); // 第一次延迟 60 秒执行任务

                            PropertiesUtil.setSystemProperties("persist.sys.server_connected",false);

                    }
                    //获取GPRS状态
                    state = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
                    //判断是否在使用GPRS网络
                    if (state == NetworkInfo.State.CONNECTED) {
                        LogUtils.e( "ACTION_CONNECTIVITY_CHANGE=TYPE_MOBILE");
                    }
                    if (networkType != NetworkUtil.getNetworkType(context)) {
                        SPUtils.getInstance().put("NetworkType",NetworkUtil.getNetworkType(context));
                        if (NetworkUtil.isNetworkAvailable(context) && iConnectionManager != null) {
                            OrderUtil.getInstance().stopSocket();
                            OrderUtil.getInstance().startSocket();
                        }
                    } else {
                        if (NetworkUtil.isNetworkAvailable(context) && iConnectionManager != null) {
                            if (!iConnectionManager.isConnect()) {
                                OrderUtil.getInstance().startSocket();
                            }
                        }
                    }
                    break;
                case "com.ic.action.keyevent":
                    Log.e("keycode",intent.getIntExtra("keycode", -1) + "");
                    break;
                case ReceiverConstant.ACTION_Network: //是否有网络
                    AlarmTimer.startIsNetwork(context);
                    Log.e("网络","没网");
                    if (DeviceUtils.isSimReady(context)){
                        Log.e("网络","有卡");
                        i++;
                        Log.e("网络",i+(SharedPreferencedUtils.getBoolean(context,"isReboot",false) + ""));
                        if (i == 5){
                            try {
                                DeviceUtils.setSilentShutdown(context);
                                SharedPreferencedUtils.setBoolean(context,"isReboot",true);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                case ReceiverConstant.ACTION_USB_STATE:
                    Log.e("ttt","111");
                    Boolean data = intent.getBooleanExtra("connected",false);
                    long currentTime = System.currentTimeMillis();
                    if (data && !isCharge){
                        MsgText = MsgType.IWAPUB
                                +GlobalSettings.MSG_CONTENT_SEPERATOR
                                +GlobalSettings.instance().getImei()
                                +GlobalSettings.MSG_CONTENT_SEPERATOR
                                +System.currentTimeMillis()
                                +GlobalSettings.MSG_CONTENT_SEPERATOR
                                +"1"
                                +"#";
                        OrderUtil.getInstance().sendMsgRe(MsgText);
                        isCharge = true;
                    }else if (isCharge){
                        MsgText = MsgType.IWAPUB
                                +GlobalSettings.MSG_CONTENT_SEPERATOR
                                +GlobalSettings.instance().getImei()
                                +GlobalSettings.MSG_CONTENT_SEPERATOR
                                +System.currentTimeMillis()
                                +GlobalSettings.MSG_CONTENT_SEPERATOR
                                +"0"
                                +"#";
                        OrderUtil.getInstance().sendMsgRe(MsgText);
                        isCharge = false;
                    }
                    break;
                case ReceiverConstant.ACTION_FALL_ALERT:
                    MsgText = MsgType.IWAPFD
                            +GlobalSettings.MSG_CONTENT_SEPERATOR
                            +GlobalSettings.instance().getImei()
                            +GlobalSettings.MSG_CONTENT_SEPERATOR
                            +System.currentTimeMillis()
                            +"#";
                    OrderUtil.getInstance().sendMsgRe(MsgText);
                    break;
                case ReceiverConstant.ACTION_STILL_ALERT:
                    MsgText = MsgType.IWAPST
                            +GlobalSettings.MSG_CONTENT_SEPERATOR
                            +GlobalSettings.instance().getImei()
                            +GlobalSettings.MSG_CONTENT_SEPERATOR
                            +System.currentTimeMillis()
                            +"#";
                    OrderUtil.getInstance().sendMsgRe(MsgText);
                    break;
                case ReceiverConstant.Action_srvPushTxt:
                    String textPush = intent.getStringExtra("txtMsg");
                    Log.e("pushTxt:",textPush);
                    if (textPush.startsWith(">") && textPush.contains("*")) {
                            int startIndex = textPush.indexOf(">") + 2;
                            int endIndex = textPush.indexOf("<", startIndex);
                            String extractedText = textPush.substring(startIndex, endIndex - 1).trim();
                            String[] parts = extractedText.split("@");
                            if (parts.length >= 2) {
                              String title = parts[0];
                              String cmd = parts[1];
                                switch (title) {
                                    case "ble":
                                        try {
                                            Log.e("ble",cmd);
                                            if (cmd.equals("0")){
                                                SharedPreferencedUtils.setString(context,"isOpen","0");
                                                BleConstant.Ble_Is30S = false;
                                                if (MainActivity.bluetoothAdapter != null){
                                                    MainActivity.bluetoothAdapter.disable();
                                                }else {
                                                    MainActivity.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                                    MainActivity.bluetoothAdapter.disable();
                                                }

                                                if (MainActivity.mBlueService != null){
                                                    MainActivity.mBlueService.stop();
                                                    MainActivity.mBlueService = null;
                                                }


                                                OrderUtil.getInstance().stopSocket();
                                            }else {
                                                if (SharedPreferencedUtils.getString(context,"isOpen","0").equals("0")){
                                                    SharedPreferencedUtils.setString(context,"isOpen","1");
                                                }
                                                AlarmTimer.DEFAULT_Blue_INTERVAL = Long.parseLong(cmd) * 1000;
                                                AlarmTimer.startConfirmedFrequencyUpload_BLE(context);
                                                SharedPreferencedUtils.setLong(context,"blueScanTime",AlarmTimer.DEFAULT_Blue_INTERVAL);
                                            }

                                        }catch (Exception e){
                                            Log.e("blue",e.toString());
                                        }
                                        break;
                                    case "factorymode":
                                        Log.e("pushTxt:factorymode ",cmd);
                                        if (cmd.equals("0")){
                                            PropertiesUtil.setSystemProperties("persist.sys.isopentest",false);
                                        }else if (cmd.equals("1")){
                                            PropertiesUtil.setSystemProperties("persist.sys.isopentest",true);
                                        }
                                    break;
                                    default:
                                        break;
                                }
                             }
                        }
                    Log.e("push",textPush);
                    break;
                case ReceiverConstant.Action_CMD:
                    String title = intent.getStringExtra("title");
                    String cmd = intent.getStringExtra("cmd");
                    try {
                        switch (title){
                            case "wifi":{
                                String[] wifi = cmd.split("\\|");
                                if (cmd.equals("1")){
                                    MainActivity.wifiUtil.openWifi();
                                    break;
                                }else if (cmd.equals("0")){
                                    MainActivity.wifiUtil.closeWifi();
                                    break;
                                }
                                MainActivity.wifiUtil.openWifi();
                                Log.e("cmd",wifi[0]);
                                Log.e("cmd",wifi[1]);
                                if (MainActivity.wifiUtil.isConn())
                                {
                                    MainActivity.wifiManager.disconnect();
                                }

                                MainActivity.wifiManager.reconnect();
                                if (PropertiesUtil.getSystemProperties("persist.sys.ic.wifi_enable")!="true") {
                                    PropertiesUtil.setSystemProperties("persist.sys.ic.wifi_enable", true);
                                }
                                WifiConfiguration wifiConfiguration = MainActivity.wifiUtil.createWifiInfo(wifi[0],wifi[1],3);
                                MainActivity.wifiUtil.addNetWork(wifiConfiguration);
                            }
                            break;
                            case "photo":
                                try {
                                    CameraUtil cameraUtil = new CameraUtil(context);
                                    cameraUtil.getPicture(System.currentTimeMillis()+"");
                                }catch (Exception ex){
                                    Log.e("photo:",ex.toString());
                                }
                                break;
                            case "isWear":{
                                if (cmd.equals("0")){
                                    MsgText = MsgType.IWAPWR
                                            +GlobalSettings.MSG_CONTENT_SEPERATOR
                                            +GlobalSettings.instance().getImei()
                                            +GlobalSettings.MSG_CONTENT_SEPERATOR
                                            +"0"
                                            +GlobalSettings.MSG_CONTENT_SEPERATOR
                                            +System.currentTimeMillis()
                                            +"#";
                                    OrderUtil.getInstance().sendMsgRe(MsgText);
                                }else if (cmd.equals("1")){
                                    MsgText = MsgType.IWAPWR
                                            +GlobalSettings.MSG_CONTENT_SEPERATOR
                                            +GlobalSettings.instance().getImei()
                                            +GlobalSettings.MSG_CONTENT_SEPERATOR
                                            +"1"
                                            +GlobalSettings.MSG_CONTENT_SEPERATOR
                                            +System.currentTimeMillis()
                                            +"#";
                                    OrderUtil.getInstance().sendMsgRe(MsgText);
                                }
                                break;
                            }
                            case "ble":
                                try {
                                    Log.e("ble",cmd);
                                    if (cmd.equals("0")){
                                        SharedPreferencedUtils.setString(context,"isOpen","0");
                                        BleConstant.Ble_Is30S = false;
                                        if (MainActivity.bluetoothAdapter != null){
                                            MainActivity.bluetoothAdapter.disable();
                                        }else {
                                            MainActivity.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                                            MainActivity.bluetoothAdapter.disable();
                                        }

                                        if (MainActivity.mBlueService != null){
                                            MainActivity.mBlueService.stop();
                                            MainActivity.mBlueService = null;
                                        }


                                        OrderUtil.getInstance().stopSocket();
                                    }else {
                                        if (SharedPreferencedUtils.getString(context,"isOpen","0").equals("0")){
                                            SharedPreferencedUtils.setString(context,"isOpen","1");
                                        }
                                        AlarmTimer.DEFAULT_Blue_INTERVAL = Long.parseLong(cmd) * 1000;
                                        AlarmTimer.startConfirmedFrequencyUpload_BLE(context);
                                        SharedPreferencedUtils.setLong(context,"blueScanTime",AlarmTimer.DEFAULT_Blue_INTERVAL);
                                    }

                                }catch (Exception e){
                                    Log.e("blue",e.toString());
                                }
                                break;
                            default:
                                break;
                        }
                    }catch (Exception e){
                        LogUtils.file(e.toString());
                    }

                    break;
                case ReceiverConstant.CONFIRMED_Ble_IsOpen:
                    BleConstant.Ble_Is30S = false;
                    if (SharedPreferencedUtils.getString(context,"isOpen","3").equals("3")){
                        SharedPreferencedUtils.setString(context,"isOpen","0");
                        Log.e("BLE","默认开关为0，除非有值");
                    }
                    if (!SharedPreferencedUtils.getString(context,"isOpen","0").equals("0"))
                    {
                        if (MainActivity.mBlueService != null){
                            MainActivity.mBlueService.stop();
                        }

                        if (MainActivity.bluetoothAdapter != null){
                            MainActivity.bluetoothAdapter.disable();
                        }else {
                            MainActivity.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                            MainActivity.bluetoothAdapter.disable();
                        }
                    }
                    break;
                case ReceiverConstant.CONFIRMED_FREQUENCY_UPLOAD_BLE:
                    LogUtils.e("BLe","2");
                    AlarmTimer.startConfirmedFrequencyUpload_BLE(context);

                    if (SharedPreferencedUtils.getString(context,"isOpen","0").equals("0")&& !BleConstant.Ble_Is30S)
                    {
                        LogUtils.e("BLe","跳过蓝牙");
                        break;
                    }

                    try {
                        if (OrderUtil.getInstance().getIConnectionManager() == null)
                        {
                            Log.e("ble:","启动网络");
                            OrderUtil.getInstance().startSocket();
                        }

                        if (MainActivity.mBlueService == null){
                            Log.e("ble:","蓝牙服务消亡");
                            MainActivity.setupChat(context);
                        }

              /*          if (MainActivity.mBlueService.mConnectThread == null)
                        {
                            Log.e("ble:","蓝牙线程未启动");
                            MainActivity.mBlueService.start();
                        }
*/
                        if(!MainActivity.bluetoothAdapter.isEnabled()){
                            LogUtils.e("ble","执行蓝牙打开");
                            MainActivity.blueToothUtils.startBlueEnable(MainActivity.bluetoothAdapter,MainActivity.sContext);
                        }
                        getMac();
                    }catch (Exception e)
                    {
                        LogUtils.e("thread","线程崩溃" + e.toString() );
                        final Intent intent1 = MainActivity.sContext.getPackageManager().getLaunchIntentForPackage(MainActivity.sContext.getPackageName());
                        intent.addFlags(intent1.FLAG_ACTIVITY_CLEAR_TOP);
                        MainActivity.sContext.startActivity(intent);
                    }

                    break;
                case ReceiverConstant.ACTION_BOOT:
                    LogUtils.e("restart","3");
                    Intent splashIntent = new Intent(context, MainActivity.class);
                    splashIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(splashIntent);
                    break;
                case ReceiverConstant.ACTION_LOG:
                    //10min一次写入本地日志
                    AlarmTimer.startConfirmedBle_Log(context,Long.valueOf(10 * 60 * 1000));
                    if (MainActivity.wifiUtil.isConn() == true){
                        wifiStr = MainActivity.wifiUtil.getWifiInfo();
                    }else {
                        wifiStr = "wifi网络未链接";
                    }
                    String msg = "apn:"+MainActivity.apnUtil.getCurrentAPN(context)+ "\n" +"wifi:"+
                            wifiStr;
                    //写入日志
                    break;
                case ReceiverConstant.ACTION_SMS_RECEIVED:
                    Object[] pdus = (Object[]) intent.getExtras().get("pdus");
                    for(Object pdu:pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte [])pdu);
                        String sender = smsMessage.getDisplayOriginatingAddress();

                        String content = smsMessage.getDisplayMessageBody();
                        String phoneNumber;

                        LogUtils.e("ACTION_SMS_RECEIVED","sender:"+sender+",content:"+content);
                        if (content.contains("#BLUE=ON")){
                            SharedPreferencedUtils.setString(context,"isOpen","1");
                            if (!SharedPreferencedUtils.getString(context,"isOpen","0").equals("1")){
                                AlarmTimer.startConfirmedBle_IsOpen(context, Long.valueOf(30 * 60 * 1000));
                            }
                            OkSocket.sendSMS(sender,"success");
                        }else if (content.contains("#BLUE=OFF")){
                            SharedPreferencedUtils.setString(context,"isOpen","0");
                            if (!SharedPreferencedUtils.getString(context,"isOpen","0").equals("1")){
                                AlarmTimer.startConfirmedBle_IsOpen(context, Long.valueOf(1 * 60 * 1000));
                            }
                            OkSocket.sendSMS(sender,"success");
                        }
                    }
                    break;
                default:
                    break;
            }
        }catch (Exception e)
        {
            LogUtils.e("alarm:",e.toString());
            Log.e("msg",e.toString());
        }

    }

    private FrameLayout cameraFrame;
    private Camera mCamera;
    private TextView cameraTv;
    private void capturePhoto(Context context) {
        cameraFrame = MainActivity.cameraFrame;

        int numberOfCameras = Camera.getNumberOfCameras();// 获取摄像头个数
        Log.e("tt",numberOfCameras+"");
        //遍历摄像头信息
        for (int cameraId = 0; cameraId < numberOfCameras; cameraId++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, cameraInfo);
            Log.e("tt",cameraInfo.facing+"");
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {//前置摄像头
                mCamera = Camera.open(cameraId);//打开摄像头
            }
        }


        CameraPreview mPreview = new CameraPreview(context, mCamera);
        cameraFrame.addView(mPreview);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000); // 设置1秒后自动拍照，可调节
                    //得到照相机的参数
                    Camera.Parameters parameters = mCamera.getParameters();
                    //图片的格式
                    parameters.setPictureFormat(ImageFormat.JPEG);
                    //预览的大小是多少
                    parameters.setPreviewSize(800, 400);
                    //设置对焦模式，自动对焦
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                    //对焦成功后，自动拍照
                    mCamera.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(boolean success, Camera camera) {
                            if (success) {
                                //获取照片
                                mCamera.takePicture(null, null, mPictureCallback);
                            }
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            FileOutputStream fos = null;
            String mFilePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "tt005.png";
            Log.e("ttpath",mFilePath);
            //文件
            File tempFile = new File(mFilePath);
            try {
                //
                fos = new FileOutputStream(tempFile);
                fos.write(data);
                imageUploader.uploadImage(mFilePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //实现连续拍多张的效果
//    mCamera.startPreview();
//    if (fos != null) {
//     try {
//      fos.close();
//     } catch (IOException e) {
//      e.printStackTrace();
//     }
//    }
            }

        }
    };

    String ble = "";
    public void getMac()
    {
        String content = "";


        Log.e("thread","还没崩溃");
        try {
            if (MainActivity.bluetoothAdapter == null){
                Log.e("thread","准备重启");
            /*    final Intent intent = MainActivity.sContext.getPackageManager().getLaunchIntentForPackage(MainActivity.sContext.getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                MainActivity.sContext.startActivity(intent);*/
                Intent splashIntent = new Intent(MainActivity.sContext, MainActivity.class);
                splashIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.sContext.startActivity(splashIntent);
            /*    final Intent intent = MainActivity.sContext.getPackageManager().getLaunchIntentForPackage((MainActivity.APP_NAME));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                MainActivity.sContext.startActivity(intent);*/
                return ;
            }

        }catch (Exception e)
        {
            Log.e("thread:",e.toString());
            if (MainActivity.mBlueService == null){
                Log.e("thread","蓝牙线程崩溃");
            }
        };

        if (MainActivity.bluetoothAdapter == null){
            Log.e("thread","蓝牙还是崩溃了");
            return ;
        }
        /*   AlarmTimer.startConfirmedFrequencyUpload_BLE(MainActivity.sContext);*/
        startBleScan();
        return ;
    }


    public void startBleScan() {
        // 注册广播接收器

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        MainActivity.sContext.registerReceiver(mReceiver, filter);

        // 启动蓝牙设备扫描
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.startDiscovery();
        }

        // 停止扫描定时器
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 停止蓝牙设备扫描
                if (bluetoothAdapter != null && bluetoothAdapter.isDiscovering()) {
                    bluetoothAdapter.cancelDiscovery();
                }

                // 注销广播接收器
                MainActivity.sContext.unregisterReceiver(mReceiver);

                Log.e("BLE",ble);

                if (ble.length() > 0){
                    ble = ble.substring(0,ble.length() - 1);
                }
                map.clear();

                String contentRe = MsgType.IWAPBL
                        + GlobalSettings.MSG_CONTENT_SEPERATOR
                        +GlobalSettings.instance().getImei()
                        +GlobalSettings.MSG_CONTENT_SEPERATOR
                        +ble
                        +GlobalSettings.MSG_CONTENT_SEPERATOR
                        +BluetoothAdapter.getDefaultAdapter().getAddress()
                        +GlobalSettings.MSG_CONTENT_SEPERATOR
                        +System.currentTimeMillis()
                        +"#";
                Log.e("BLE:t:",tip + "");
                if(ble != "" || tip++>4){
                    OrderUtil.getInstance().sendMsgRe(contentRe);
                    tip = 0;
                }
                Log.e("BLE", "注销广播");
            }
        }, Long.parseLong(BleConstant.Ble_Scan_Time) * 1000);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("BLE","开始广播");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    if (device.getName() != null && !map.containsKey(device.getAddress())) {
                        Log.e("blue",device.getName());
                        ble += device.getName() + "|" +
                                device.getAddress() + "|" +
                                intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE) + "&";;
                        map.put(device.getAddress(), device);
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.e("BLE", "扫描结束");
                }
                Log.e("BLE", "扫描开始");
            }
    };

    public static void send_at_to_reset_simcard(Context context) {
        Intent intent = new Intent("com.eqc.intent.action.getTelephony.lbs");
        intent.putExtra("command", "AT+SFUN=2"); //重新初始化sim卡
        intent.putExtra("arg2", "");
        context.sendBroadcast(intent);
        LogUtils.file("send_at_to_reset_simcard ");
    }

    public void upLoadHeart(int heartRate,int sysP,int diaP){
        if(heartRate != 0 && sysP != 0 && diaP != 0){
            OrderUtil.getInstance().uploadHeartRateAndBloodPressure(String.valueOf(heartRate),String.valueOf(sysP),String.valueOf(diaP));
//            if(heartRate> Settings.System.getInt(Utils.getContext().getContentResolver(), GlobalSettings.HeartRateMax,0)
//                    && Settings.System.getInt(Utils.getContext().getContentResolver(), GlobalSettings.HeartRateMax,0)>0){
//                OrderUtil.getInstance().heartBloodPressure("08");
//            }
//            if(heartRate< Settings.System.getInt(Utils.getContext().getContentResolver(), GlobalSettings.HeartRateMin,0)
//                    &&Settings.System.getInt(Utils.getContext().getContentResolver(), GlobalSettings.HeartRateMin,0)>0){
//                OrderUtil.getInstance().heartBloodPressure("09");
//            }
//            if(sysP>Settings.System.getInt(Utils.getContext().getContentResolver(), GlobalSettings.SystolicMax,0)
//                    && Settings.System.getInt(Utils.getContext().getContentResolver(), GlobalSettings.SystolicMax,0)>0){
//                OrderUtil.getInstance().heartBloodPressure("10");
//            }
//            if(sysP< Settings.System.getInt(Utils.getContext().getContentResolver(), GlobalSettings.SystolicMin,0)
//                    && Settings.System.getInt(Utils.getContext().getContentResolver(), GlobalSettings.SystolicMin,0)>0){
//                OrderUtil.getInstance().heartBloodPressure("11");
//            }
//            if(diaP>Settings.System.getInt(Utils.getContext().getContentResolver(), GlobalSettings.DiastolicMax,0)
//                    && Settings.System.getInt(Utils.getContext().getContentResolver(), GlobalSettings.DiastolicMax,0)>0){
//                OrderUtil.getInstance().heartBloodPressure("12");
//            }
//            if(diaP< Settings.System.getInt(Utils.getContext().getContentResolver(), GlobalSettings.DiastolicMin,0)
//                    && Settings.System.getInt(Utils.getContext().getContentResolver(), GlobalSettings.DiastolicMin,0)>0){
//                OrderUtil.getInstance().heartBloodPressure("13");
//            }
        }
    }

}
