package com.xrs.bluetooth_device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BlueConnectThread extends Thread {

    private final String TAG;

    private final String bluetoothName;

    private final BluetoothDevice bluetoothDevice;

    private boolean isConnecting;

    private boolean isConnected;

    private BluetoothSocket bluetoothSocket;

    private OutputStream outputStream;

    private static final int MAX_RETRIES = 3; // 最大重试次数
    private static final long RETRY_DELAY_MS = 5000; // 重试间隔时间（毫秒）

    public BlueConnectThread(String TAG, String bluetoothName, BluetoothDevice bluetoothDevice) {
        this.TAG = TAG;
        this.bluetoothName = bluetoothName;
        this.bluetoothDevice = bluetoothDevice;
    }

    @Override
    public void run() {
        Log.i(TAG, "蓝牙[" + this.bluetoothName + "]连接线程 run ....");
        int retryCount = 0;

        while (retryCount < MAX_RETRIES) {
            synchronized (this) {
                if (isConnected) {
                    try {
                        Log.i(TAG, "蓝牙已连接，蓝牙[" + this.bluetoothName + "]连接线程即将进入wait ...");
                        this.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "蓝牙已连接，蓝牙[" + this.bluetoothName + "]连接线程即将进入wait, 异常 ***", e);
                    }
                }
            }

            try {
                int bondState = this.bluetoothDevice.getBondState();
                if (bondState != BluetoothDevice.BOND_BONDED) {
                    Log.e(TAG, "蓝牙[" + this.bluetoothName + "]未配对，需要手动配对");
                    retryCount++;
                    continue;
                } else {
                    Log.d(TAG, "蓝牙[" + this.bluetoothDevice.getName() + "]已配对");
                }
            } catch (Exception e) {
                Log.e(TAG, "蓝牙[" + this.bluetoothName + "]配对异常***", e);
                retryCount++;
                continue;
            }

            try {
                if (this.bluetoothSocket == null || !this.bluetoothSocket.isConnected()) {
                    Log.i(TAG, "开始创建蓝牙[" + this.bluetoothName + "]连接socket ...");
                    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                    this.bluetoothSocket = this.bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                }
            } catch (IOException e) {
                Log.e(TAG, "创建蓝牙[" + this.bluetoothName + "]连接socket异常 ***", e);
                retryCount++;
                continue;
            }

            if (this.bluetoothSocket != null && !isConnecting && !isConnected) {
                isConnecting = true;
                try {
                    Log.i(TAG, "开始连接蓝牙设备[" + this.bluetoothName + "]...");
                    bluetoothSocket.connect();
                    isConnected = bluetoothSocket.isConnected();
                    Log.i(TAG, "蓝牙设备[" + this.bluetoothName + "]连接[" + isConnected + "]");
                    if (isConnected) {
                        Log.i(TAG, "蓝牙设备[" + this.bluetoothName + "]连接成功");
                        sendInitialMessage(); // 发送初始消息
                        break; // 连接成功，退出循环
                    }
                } catch (IOException e) {
                    Log.e(TAG, "连接蓝牙设备[" + this.bluetoothName + "]异常***，即将重新连接...", e);
                    try {
                        if (bluetoothSocket != null) {
                            bluetoothSocket.close();
                            bluetoothSocket = null;
                        }
                    } catch (IOException e2) {
                        Log.e(TAG, "关闭蓝牙socket异常", e2);
                    }
                    isConnected = false;
                } finally {
                    isConnecting = false;
                }
            }

            retryCount++;
            try {
                Thread.sleep(RETRY_DELAY_MS); // 等待一段时间后重试
            } catch (InterruptedException e) {
                Log.e(TAG, "线程中断", e);
            }
        }

        if (!isConnected) {
            Log.e(TAG, "连接失败，已达到最大重试次数");
        }
    }

    private void sendInitialMessage() {
        String initialMessage = "Hello Bluetooth!";
        send(initialMessage);
    }


    public synchronized void startConnect() {
        isConnected = false;
        this.notify();
    }

    public synchronized void stopOut() {
        if (this.outputStream != null) {
            try {
                this.outputStream.close();
            } catch (IOException e) {
                Log.i(TAG, "关闭蓝牙socket[outputStream]异常", e);
            }
        }

        if (this.bluetoothSocket != null) {
            try {
                this.bluetoothSocket.close();
            } catch (IOException e) {
                Log.i(TAG, "关闭蓝牙socket异常", e);
            }
        }
    }

    public synchronized void send(String msg) {
        if (this.bluetoothSocket != null && isConnected) {
            try {
                if (this.outputStream == null) {
                    this.outputStream = this.bluetoothSocket.getOutputStream();
                }
                outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
                Log.i(TAG, "蓝牙发送数据[" + msg + "]成功");
            } catch (IOException e) {
                Log.e(TAG, "蓝牙发送数据[" + msg + "]异常", e);
                isConnected = false;
            }
        }
    }
}