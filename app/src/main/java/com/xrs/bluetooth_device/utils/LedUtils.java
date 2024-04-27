package com.xrs.bluetooth_device.utils;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class LedUtils {

    public static final String TAG = "LedUtils";

    static final String LED_ALL_EN = "/sys/class/input/input0/subled/enable";
    static final String LED_RED_EN = "/sys/class/input/input0/subled/enableled6";
    static final String LED_GREEN_EN = "/sys/class/input/input0/subled/enableled5";

    public static void LedSwitch(String tag){
        switch (tag){
            case "101":
                RedLedEnable(true);
                break;
            case "102":
                RedLedEnable(false);
                break;
            case "103":
                GreenLedEnable(true);
                break;
            case "104":
                GreenLedEnable(false);
                break;
        }
    }

    static void write_data_to_file(String FilePath, String data) {
        Log.d(TAG, FilePath + " " + data);
        File fp = new File(FilePath);
        int i = 0;
        while (!fp.exists()) {
            i++;
            fp = new File(FilePath.replace("input0", "input" + i));
            if (i > 20) {
                Log.d(TAG, "no file supported!");
                return;
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fp);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            Log.i("sys",e.toString());
            e.printStackTrace();
        }
    }

    /**
     * 亮起绿灯
     * @param en
     */
    static public void GreenLedEnable(boolean en) {
        if (en)
            write_data_to_file(LED_GREEN_EN, "1");
        else
            write_data_to_file(LED_GREEN_EN, "0");
    }

    /**
     * 亮起红灯
     * @param en
     */
    static public void RedLedEnable(boolean en) {
        if (en)
            write_data_to_file(LED_RED_EN, "1");
        else
            write_data_to_file(LED_RED_EN, "0");
    }

    /**
     * 所有灯亮
     * @param enable
     */
    static public void LedAllEnable(boolean enable) {
        if (enable)
            write_data_to_file(LED_ALL_EN, "1");
        else
            write_data_to_file(LED_ALL_EN, "0");
    }

}
