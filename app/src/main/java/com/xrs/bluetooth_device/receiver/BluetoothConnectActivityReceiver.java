package com.xrs.bluetooth_device.receiver;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.xrs.bluetooth_device.utils.ClsUtils;


public class BluetoothConnectActivityReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.bluetooth.device.action.PAIRING_REQUEST".equals(intent.getAction())) {
            BluetoothDevice mBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            try {
                Bundle extras = intent.getExtras();
                Object type = extras.get("android.bluetooth.device.extra.PAIRING_VARIANT");
                Log.e("msg:",String.valueOf(type));
                //(三星)4.3版本测试手机还是会弹出用户交互页面(闪一下)，如果不注释掉下面这句页面不会取消但可以配对成功。(中兴，魅族4(Flyme 6))5.1版本手机两中情况下都正常
                ClsUtils.setPairingConfirmation(mBluetoothDevice.getClass(), mBluetoothDevice, true);
                Object pairkey = extras.get("android.bluetooth.device.extra.PAIRING_KEY");
                Log.e("msg:",String.valueOf(pairkey));
              /*  ClsUtils.setPairingConfirmation(mBluetoothDevice.getClass() , mBluetoothDevice, false);*/
                abortBroadcast();//如果没有将广播终止，则会出现一个一闪而过的配对框。

                //3.调用setPin方法进行配对...
                boolean ret = ClsUtils.setPin(mBluetoothDevice.getClass(), mBluetoothDevice, String.valueOf(pairkey));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
