package com.xrs.bluetooth_device.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.xrs.bluetooth_device.MainActivity;
import com.xrs.bluetooth_device.R;

/**
 * @ClassName NotificationService
 * @Author kotlin
 * @Email 949390151@qq.com
 * @Date 2023/3/29 15:44
 * ^_^^_^^_^^_^^_^^_^^_^
 */
public class NotificationService extends Service {
    private static final String TAG = NotificationService.class.getSimpleName();
    private String notificationId = "serviceid";
    private String notificationName = "servicename";
    private NotificationManager notificationManager;
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("notification", "onCreate()");
        startForeground(1,getNotification());
        stopForeground(true);
    }

    private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }
        Notification notification = builder.build();
        return notification;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
