package com.xrs.bluetooth_device.function;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


import com.libsocket.constant.SPConstant;
import com.xrs.bluetooth_device.utils.LogUtils;
import com.xrs.bluetooth_device.utils.SPUtils;

import java.util.Calendar;


public class AlarmTimer {

    /**
     * 默认定位上传间隔
     */
    private static final long DEFAULT_LOCATE_INTERVAL =  120 * 1000;
    /**
     * 跟踪模式时间间隔
     */
    private static final long TRACKING_MODE_INTERVAL = 5 * 1000;
    /**
     * 跟踪模式最大定位次数
     */
    public static final int TRACKING_MODE_TIMES_MAX = 60;
    /**
     * 实时定位次数
     */
    public static int TRACKING_TIMES = 0;
    /**
     * 心跳包间隔
     */
    public static final long HEARTBEAT_TIME = 3 * 60 * 1000;

    /**
     * 默认蓝牙上传间隔
     */
    public static  long DEFAULT_Blue_INTERVAL =  2 * 60 * 1000;
    private static final long NetWork_INTERVAL =  2 * 60 *1000;
    /**
     * 开启实时定位
     *
     */
    public static void setLocationAlarmStart(Context context) {
        AlarmEntity trackingEntity = new AlarmEntity(AlarmEntity.Type.LocateStart);
        cancelAlarmTimer(context, trackingEntity);//连续操作，结束上一次闹钟
        trackingEntity.setRepeat(true);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        trackingEntity.setFirstStartTime(calendar.getTimeInMillis());
        trackingEntity.setInterval(TRACKING_MODE_INTERVAL);
        setAlarmTimer(context, trackingEntity);
    }

    /**
     * 开启固定频率上传
     *
     */
    public static void startConfirmedFrequencyUpload(Context context) {
        AlarmEntity locateEntity = new AlarmEntity(AlarmEntity.Type.CONFIRMED_FREQUENCY_UPLOAD);
        cancelAlarmTimer(context, locateEntity);//连续操作，结束上一次闹钟
        locateEntity.setRepeat(true);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        locateEntity.setFirstStartTime(calendar.getTimeInMillis());
        long interval = SPUtils.getInstance().getLong(SPConstant.CURRENT_INTERVAL, DEFAULT_LOCATE_INTERVAL);
        locateEntity.setInterval(interval);
        setAlarmTimer(context, locateEntity);
    }

    public static void startConfirmedFrequencyUpload_BLE(Context context) {
        AlarmEntity locateEntity = new AlarmEntity(AlarmEntity.Type.CONFIRMED_FREQUENCY_UPLOAD_BLE);
        cancelAlarmTimer(context, locateEntity);//连续操作，结束上一次闹钟
        locateEntity.setRepeat(true);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        locateEntity.setFirstStartTime(calendar.getTimeInMillis());
        Log.e("tt",DEFAULT_Blue_INTERVAL+"");
        long interval = SPUtils.getInstance().getLong(SPConstant.CURRENT_INTERVAL, DEFAULT_Blue_INTERVAL);
        locateEntity.setInterval(interval);
        setAlarmTimer(context, locateEntity);
    }

    public static void startIsNetwork(Context context) {
        AlarmEntity locateEntity = new AlarmEntity(AlarmEntity.Type.IsNetwork);
        cancelAlarmTimer(context, locateEntity);//连续操作，结束上一次闹钟
        locateEntity.setRepeat(true);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        locateEntity.setFirstStartTime(calendar.getTimeInMillis());
        long interval = SPUtils.getInstance().getLong(SPConstant.CURRENT_INTERVAL, NetWork_INTERVAL);
        locateEntity.setInterval(interval);
        setAlarmTimer(context, locateEntity);
    }

    public static void cancelIsNetwork(Context context) {
        AlarmEntity locateEntity = new AlarmEntity(AlarmEntity.Type.IsNetwork);
        cancelAlarmTimer(context, locateEntity);
    }

    public static void startConfirmedBle_IsOpen(Context context,Long time) {
        AlarmEntity locateEntity = new AlarmEntity(AlarmEntity.Type.IsOpen);
        cancelAlarmTimer(context, locateEntity);//连续操作，结束上一次闹钟
        locateEntity.setRepeat(true);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        locateEntity.setFirstStartTime(calendar.getTimeInMillis());
        long interval = SPUtils.getInstance().getLong(SPConstant.CURRENT_INTERVAL, time);
        locateEntity.setInterval(interval);
        setAlarmTimer(context, locateEntity);
    }

    public static void startConfirmedBle_Log(Context context,Long time) {
        AlarmEntity locateEntity = new AlarmEntity(AlarmEntity.Type.Log);
        cancelAlarmTimer(context, locateEntity);//连续操作，结束上一次闹钟
        locateEntity.setRepeat(true);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        locateEntity.setFirstStartTime(calendar.getTimeInMillis());
        long interval = SPUtils.getInstance().getLong(SPConstant.CURRENT_INTERVAL, time);
        locateEntity.setInterval(interval);
        setAlarmTimer(context, locateEntity);
    }

    /**
     * 开启心跳包上传闹钟
     *
     */
    public static void startHeartBeat(Context context) {
        AlarmEntity heartbeatEntity = new AlarmEntity(AlarmEntity.Type.HeartBeat);
        cancelAlarmTimer(context, heartbeatEntity);//连续操作，结束上一次闹钟
        heartbeatEntity.setRepeat(true);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        heartbeatEntity.setFirstStartTime(calendar.getTimeInMillis());
        heartbeatEntity.setInterval(HEARTBEAT_TIME);
        setAlarmTimer(context, heartbeatEntity);
    }

    /**
     * 设置闹钟
     *
     */
    private static void setAlarmTimer(Context ctx, AlarmEntity entity) {
        String action = entity.getAction();
        Intent myIntent = new Intent();
        myIntent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        myIntent.setPackage("com.xrs.bluetooth_device"); // 替换为目标应用程序的包名
        myIntent.setAction(action);
        int alarmManagerType = entity.getAlarmManagerType();
        PendingIntent sender = PendingIntent.getBroadcast(ctx, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        long firstStartTime = entity.getFirstStartTime();
        firstStartTime = firstStartTime <= 0 ? System.currentTimeMillis() : firstStartTime;
        boolean isRepeat = entity.isRepeat();
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        if(alarm == null) {
            LogUtils.d("alarm is null");
            LogUtils.e("loc:", "闹钟为空了 ");
            return;
        }
        LogUtils.d(entity.toString());
        long interval = entity.getInterval();
        if (isRepeat) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + interval, sender);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarm.setExact(alarmManagerType, System.currentTimeMillis() + interval, sender);
            } else {
                alarm.setRepeating(alarmManagerType, System.currentTimeMillis(), interval, sender);
            }
        } else {
            alarm.set(alarmManagerType, firstStartTime + interval, sender);
        }
        LogUtils.e("loc:", "闹钟设置完成 " + entity.getType().toString());
    }

    /**
     * 取消闹钟
     *
     */
    public static void cancelAlarmTimer(Context ctx, AlarmEntity entity) {
        Intent myIntent = new Intent();
        myIntent.setAction(entity.getAction());
        PendingIntent sender = PendingIntent.getBroadcast(ctx, 0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        assert alarm != null;
        alarm.cancel(sender);
        LogUtils.e("loc:", "闹钟取消了 ");
        LogUtils.d("取消闹钟设置完成 " + entity.getType().toString());
    }

}
