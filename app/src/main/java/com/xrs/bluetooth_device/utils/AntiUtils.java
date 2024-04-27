package com.xrs.bluetooth_device.utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class AntiUtils {

    public static final String TAG = "AntiUtils";

    public static void CheckAnti(Context context) {
        SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager == null) {
            Log.d(TAG, "Proximity 设备不支持1");
            return;
            //throw new UnsupportedOperationException("设备不支持");
        }
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (mSensor == null) {
            Log.d(TAG, "Proximity 设备不支持1 as no sensor..");
        }

        boolean isRegister = mSensorManager.registerListener(mSensorEventListener, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (!isRegister) {
            Log.d(TAG, "Proximity 设备不支持2");
            //throw new UnsupportedOperationException("设备不支持");
        }
    }

    private static SensorEventListener mSensorEventListener = new SensorEventListener() {

        private long anti_event_receivedTime;
        private int current_anti_status;
        private int ant_status_event_reported;
        private long cut_event_receivedTime;
        private int cut_status_event_reported;
        private int current_cut_status;

        public static final int STATUS_CUT_OFF = 12; //12 表示表带非法解锁
        public static final int STATUS_LOCK_ON = 2; // 2 表示表带 锁定

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            int event = (int) sensorEvent.values[0];
            if(event==2 || event == 12){
                current_cut_status = event;
                cut_status_event_reported++;
                Log.d(TAG,"近距离 " + current_cut_status + " sensor event " + cut_status_event_reported + " times !");
                cut_event_receivedTime = System.currentTimeMillis();
                //STATUS_CUT_OFF
                //STATUS_LOCK_ON
            }else {
                current_anti_status = event;
                ant_status_event_reported++;
                Log.d(TAG,"近距离 " + current_anti_status + " sensor event " + ant_status_event_reported + " times !");
                anti_event_receivedTime = System.currentTimeMillis();
                //0 佩戴， 大于0 表示脱腕
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }


    };


}
