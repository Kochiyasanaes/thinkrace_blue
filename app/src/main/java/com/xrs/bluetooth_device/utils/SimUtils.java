package com.xrs.bluetooth_device.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.xrs.bluetooth_device.MainActivity;

public class SimUtils {

    /**
     * 中国移动
     */
    public static final int SIM_TYPE_CHINA_MOBILE = 1;
    /**
     * 中国联通
     */
    public static final int SIM_TYPE_CHINA_UNICOM = 2;
    /**
     * 中国电信
     */
    public static final int SIM_TYPE_CHINA_TELECOM = 3;
    /** SIM卡是中国移动 */
    public static boolean isChinaMobile() {
        String imsi = getSimOperator();
        if (imsi == null) return false;
        return imsi.startsWith("46000") || imsi.startsWith("46002") || imsi.startsWith("46007");
    }
    /** SIM卡是中国联通 */
    public static boolean isChinaUnicom() {
        String imsi = getSimOperator();
        if (imsi == null) return false;
        return imsi.startsWith("46001");
    }
    /** SIM卡是中国电信 */
    public static boolean isChinaTelecom() {
        String imsi = getSimOperator();
        if (imsi == null) return false;
        return imsi.startsWith("46003");
    }

    private static String getSimOperator() {
        TelephonyManager tm = (TelephonyManager) MainActivity.sContext.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getSubscriberId();
    }
    /** 获取手机电话号码 */
    @SuppressLint("MissingPermission")
    public static String getPhoneNumbers() {
        TelephonyManager tm = (TelephonyManager) MainActivity.sContext.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getLine1Number();
    }
    //sim卡是否可读
    public static boolean isCanUseSim() {
        try {
            TelephonyManager mgr = (TelephonyManager) MainActivity.sContext.getSystemService(Context.TELEPHONY_SERVICE);

            return TelephonyManager.SIM_STATE_READY == mgr
                    .getSimState();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}