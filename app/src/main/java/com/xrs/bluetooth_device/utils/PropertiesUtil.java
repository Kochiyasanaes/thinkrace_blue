package com.xrs.bluetooth_device.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.content.Context.WIFI_SERVICE;



public class PropertiesUtil {

    public static String getSystemProperties(String prop) {
        String str = "";
        Class clazz;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getMethod("get", new Class[]{String.class});
            str = (String) get.invoke(clazz, new Object[]{prop});
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(e.getMessage(), e.toString());
        }
        return str;
    }

    public static String getSystemProperties(String prop, String defaultValue) {

        String str = defaultValue;
        Class clazz = null;
        try {
            clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getMethod("get", new Class[]{String.class});
            str = (String) get.invoke(clazz, new Object[]{prop});
            if (str == null || (str != null && str.trim().isEmpty())) {
                str = defaultValue;
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.e(e.getMessage(), e);
        }
        return str;
    }



    static public void setSystemProperties(String key, int defaultValue) {

        String value = String.valueOf(defaultValue);

        try {

            Class<?> c = Class.forName("android.os.SystemProperties");

            Method set = c.getMethod("set", String.class, String.class);

            set.invoke(c, key, value);

        } catch (Exception e) {

            e.printStackTrace();
            Log.e(e.getMessage(), e.toString());
        }
    }

    static public void setSystemProperties(String key, String defaultValue) {

        String value = defaultValue;

        try {

            Class<?> c = Class.forName("android.os.SystemProperties");

            Method set = c.getMethod("set", String.class, String.class);

            set.invoke(c, key, value);

        } catch (Exception e) {

            e.printStackTrace();
            Log.e(e.getMessage(), e.toString());
        }
    }

    static public void setSystemProperties(String key, Boolean defaultValue) {

        String value = defaultValue.toString();

        try {

            Class<?> c = Class.forName("android.os.SystemProperties");

            Method set = c.getMethod("set", String.class, String.class);

            set.invoke(c, key, value);

        } catch (Exception e) {

            e.printStackTrace();
            Log.e(e.getMessage(), e.toString());
        }
    }

    public static void setSystemBleDiscoverable() {
       Class serviceManager = null;
        try {
            serviceManager = Class.forName("android.bluetooth.BluetoothAdapter");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Method method = null;
        try {
            method = serviceManager.getMethod("setDiscoverableTimeout", String.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            method.invoke(serviceManager.newInstance(), 30);//根据测试，发现这一函数的参数无论传递什么值，都是永久可见的
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mSleep(int ms) {
        try {
            Thread.yield();
            Thread.sleep(ms);
        } catch (Exception e) {
            LogUtils.e(e.getMessage(), e);
        }
    }
}

