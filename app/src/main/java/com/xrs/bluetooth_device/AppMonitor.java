package com.xrs.bluetooth_device;

/**
 * @ClassName AppMonitor
 * @Author kotlin
 * @Email 949390151@qq.com
 * @Date 2023/9/1 9:59
 * ^_^^_^^_^^_^^_^^_^^_^
 */
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.xrs.bluetooth_device.utils.LogUtils;

import java.util.List;

public class AppMonitor {
    private Context context;
    private String packageName;
    private Handler handler;
    private Runnable runnable;

    public AppMonitor(Context context, String packageName) {
        LogUtils.e("jiankong",packageName+"准备开始监控");
        this.context = context;
        this.packageName = packageName;
        this.handler = new Handler();
        this.runnable = new Runnable() {
            @Override
            public void run() {
                LogUtils.e("jiankong",packageName);
                checkAppRunning();
                handler.postDelayed(this, 10*60*1000); // 每秒检查一次
            }
        };
    }

    public void startMonitoring() {
        handler.post(runnable);
    }

    public void stopMonitoring() {
        handler.removeCallbacks(runnable);
    }

    private void checkAppRunning() {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = activityManager.getRunningTasks(1);

        if (!runningTasks.isEmpty()) {
            ComponentName topActivity = runningTasks.get(0).topActivity;
            String topPackageName = topActivity.getPackageName();

            if (topPackageName.equals(packageName)) {
                LogUtils.e("jiankong",packageName+"正在运行");
                // 应用程序正在运行
            } else {
                // 应用程序已经消亡，执行打开操作
                LogUtils.e("jiankong",packageName+"重新启动");
                openApp();
            }
        }
    }

    private void openApp() {
        Intent intent = new Intent();
        intent.setClassName(packageName, packageName+".MainActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}