package com.xrs;

/**
 * @ClassName BluetoothManager
 * @Author kotlin
 * @Email 949390151@qq.com
 * @Date 2025/4/18 9:24
 * ^_^^_^^_^^_^^_^^_^^_^
 */

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.xrs.bluetooth_device.BlueConnectThread;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

public class BluetoothManager {

    private static final String TAG = "BluetoothManager";

    public BlueConnectThread connectToDevice(String deviceAddress) {
        Log.e(TAG, "准备连接" + deviceAddress);
        // 获取默认的蓝牙适配器
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Log.e(TAG, "设备不支持蓝牙");
            return null;
        }

        // 检查设备地址是否有效
        if (deviceAddress == null || deviceAddress.isEmpty()) {
            Log.e(TAG, "无效的设备地址");
            return null;
        }

        // 根据设备地址获取蓝牙设备
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        if (device == null) {
            Log.e(TAG, "无法找到设备，地址：" + deviceAddress);
            return null;
        }
        Log.e(TAG, "TTT准备连接" + deviceAddress);
        // 创建 BlueConnectThread 实例
        BlueConnectThread blueConnectThread = new BlueConnectThread("MainActivity", device.getName(), device);

        // 启动线程
        blueConnectThread.start();

        // 触发连接
        blueConnectThread.startConnect();

        return blueConnectThread;
    }
}
