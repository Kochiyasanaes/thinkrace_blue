package com.xrs.bluetooth_device.constant;

/**
 * @author mare
 * @Description:
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/8/31
 * @time 11:29
 */
public class ReceiverConstant {
    /**
     * 网络变化广播
     */
    public static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    /**
     * 短信接收
     */
    public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    /**
     * 实时定位开始闹钟
     */
    public static final String LOCATION_START = "com.thinkrace.intent.ACTION_ALARM_LOCATION_START";
    /**
     * 固定频率定时上传闹钟
     **/
    public static final String CONFIRMED_FREQUENCY_UPLOAD = "com.thinkrace.intent.ACTION_ALARM_CONFIRMED_FREQUENCY_UPLOAD";
    /**
     * 固定频率定时上传蓝牙
     **/
    public static final String CONFIRMED_FREQUENCY_UPLOAD_BLE = "com.thinkrace.intent.ACTION_ALARM_CONFIRMED_FREQUENCY_UPLOAD_BLE";
    /**
     * 开关蓝牙广播
     **/
    public static final String CONFIRMED_Ble_IsOpen = "com.thinkrace.intent.ACTION_ALARM_CONFIRMED_Ble_IsOpen";
    /**
     * 固定频率定时上传闹钟
     */
    public static final String ACTION_HEARTBEAT = "com.thinkrace.intent.HEARTBEAT";
    /**
     * 文本信息
     */
    public static final String ACTION_MSG = "com.thinkrace.intent.MSG";
    /**
     * SOS报警
     */
    public static final String ACTION_SOS =  "android.intent.action.mysos";
    /**
     * 获取步数
     */
    public static final String ACTION_STEP =  "com.zdt.stepdata";
    /**
     * 获取心率广播
     */
    public static final String ACTION_HEART =  "com.hearttest.data";

    /**
     * 关机广播
     */
    public static final String ACTION_WATCH_REBOOT =  "com.thinkrace.watchservice.reboot";

    public static final String ACTION_BOOT="android.intent.action.BOOT_COMPLETED";

    public static final String ACTION_LOG="com.thinkrace.intent.ACTION_LOG";

    public static final String ACTION_Network="com.thinkrace.intent.ACTION_Network";

    public static final String ACTION_FALL_ALERT="com.xrs.FALL_ALERT";

    public static final String ACTION_STILL_ALERT="com.xrs.STILL_ALERT";

    public static final String ACTION_USB_STATE="android.hardware.usb.action.USB_STATE";

    public static final String Action_CMD = "com.xrs.CMD";

    public static final String Action_srvPushTxt = "action.ic.srvPushTxt";
}
