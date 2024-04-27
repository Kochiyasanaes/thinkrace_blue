package com.xrs.bluetooth_device.data;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Looper;
import android.util.Log;


import com.libsocket.sdk.bean.ISendable;
import com.xrs.bluetooth_device.MainActivity;
import com.xrs.bluetooth_device.constant.BleConstant;
import com.xrs.bluetooth_device.function.AlarmTimer;
import com.xrs.bluetooth_device.utils.BlueToothUtils;
import com.xrs.bluetooth_device.utils.LogUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


public class HandShake implements ISendable {
    private String content = "";
    private String ble = "";
    private Map<String,BluetoothDevice> map = new HashMap<>();
    public HandShake() {
  /*      Log.e("thread","还没崩溃");
        try {
            if (MainActivity.bluetoothAdapter == null){
                Log.e("thread","准备重启");
            *//*    final Intent intent = MainActivity.sContext.getPackageManager().getLaunchIntentForPackage(MainActivity.sContext.getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                MainActivity.sContext.startActivity(intent);*//*
                Intent splashIntent = new Intent(MainActivity.sContext, MainActivity.class);
                splashIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.sContext.startActivity(splashIntent);
            *//*    final Intent intent = MainActivity.sContext.getPackageManager().getLaunchIntentForPackage((MainActivity.APP_NAME));
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                MainActivity.sContext.startActivity(intent);*//*
                return;
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
            return;
        }

     *//*   AlarmTimer.startConfirmedFrequencyUpload_BLE(MainActivity.sContext);*//*
        BluetoothAdapter.LeScanCallback callback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if (device.getName() != null && (device.getName().contains("VD") || device.getName().contains("VG") && !map.containsKey(device.getAddress()))){
                    ble += device.getName() + "|" +
                            device.getAddress() + "|" +
                                rssi + "&";
                    map.put(device.getAddress(),device);
                }
            }
        };
        MainActivity.bluetoothAdapter.startLeScan(callback);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                MainActivity.bluetoothAdapter.stopLeScan(callback);
            }
        }, Long.parseLong(BleConstant.Ble_Scan_Time) * 1000);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.e("BLE",ble);

        if (ble.length() > 0){
            ble = ble.substring(0,ble.length() - 1);
        }
        map.clear();

        //连接成功发一次登录包
        content = MsgType.IWAPBL
                + GlobalSettings.MSG_CONTENT_SEPERATOR
                +GlobalSettings.instance().getImei()
                +GlobalSettings.MSG_CONTENT_SEPERATOR
                +ble
                +GlobalSettings.MSG_CONTENT_SEPERATOR
                +BluetoothAdapter.getDefaultAdapter().getAddress();*/
       /* Log.e("thread content:",content);*/
    }


    @Override
    public byte[] parse() {
        return content.getBytes(Charset.defaultCharset());
    }
}
