package com.xrs.bluetooth_device.utils;



import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import com.xrs.bluetooth_device.service.BlueService;

import java.lang.reflect.Method;

public class BlueToothUtils {


    public void startBlueEnable(BluetoothAdapter bluetoothAdapter, Context context){
        //检测是否有蓝牙
        if(bluetoothAdapter == null){

            return;
        }
        //蓝牙如果已经开启则直接返回
        if (bluetoothAdapter.isEnabled()){
            ensureDiscoverable(bluetoothAdapter);
            //setDiscoverableTimeout(bluetoothAdapter);
            return;
        }
        //开始蓝牙
        try {
            bluetoothAdapter.enable();
            Thread.sleep(5000);
        }catch (Exception exception){
            Log.e("msg:blue",exception+"");
        }

       /* ensureDiscoverable(bluetoothAdapter);*/
        //setDiscoverableTimeout(bluetoothAdapter);
    }

    private void ensureDiscoverable(BluetoothAdapter mBluetoothAdapter){
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,0);
           /* startActivity(discoverableIntent);*/
        }
    }

    private void ensureDiscoverableForever(){
        Class serviceManager = null;
        try {
        //得到这个class的类
            serviceManager = Class.forName("android.bluetooth.BluetoothAdapter");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //声明一个方法
        Method method = null;
        try {
        //得到指定的类中的方法
            method = serviceManager.getMethod("setDiscoverableTimeout", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
        //调用这个方法
            method.invoke(serviceManager.newInstance(), 30);//根据测试，发现这一函数的参数无论传递什么值，都是永久可见的
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setDiscoverableTimeout(BluetoothAdapter adapter) {
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

    public void sendMessage(String message, BlueService mBlueService){
        message =  message;
        if(mBlueService.getState() != BlueService.STATE_CONNECTED){
            Log.e("msg:","blue not connected");
            return;
        }

        if(message.length() > 0){
            byte[] send=message.getBytes();
            mBlueService.write(send);
        }
    }

    public void sendMessage(String Tag, String message, BlueService mBlueService){
        message = Tag +"::::"+ message;
        if(mBlueService.getState() != BlueService.STATE_CONNECTED){
            Log.e("msg:","blue not connected");
            return;
        }

        if(message.length() > 0){
            byte[] send=message.getBytes();
            mBlueService.write(send);
        }
    }

    public void sendMessage(String Tag, int messageR, BlueService mBlueService){
        String message = Tag + "::::" + messageR + "";
        if(mBlueService.getState() != BlueService.STATE_CONNECTED){
            Log.e("msg:","blue not connected");
            return;
        }

        if(message.length() > 0){
            byte[] send=message.getBytes();
            mBlueService.write(send);
        }
    }

}
