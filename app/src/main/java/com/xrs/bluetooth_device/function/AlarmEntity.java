package com.xrs.bluetooth_device.function;

import android.app.AlarmManager;

import com.xrs.bluetooth_device.constant.ReceiverConstant;


public class AlarmEntity {

    /**
     * 实时定位 固定频率定位上传
     */
    public enum Type {
        LocateStart, CONFIRMED_FREQUENCY_UPLOAD, HeartBeat,CONFIRMED_FREQUENCY_UPLOAD_BLE,IsOpen,Log,IsNetwork
    }

    public Type type;

    public String action;

    public long firstStartTime = 0;

    public long interval;

    public boolean isRepeat;

    public int alarmManagerType = AlarmManager.RTC_WAKEUP;//休眠时会运行

    public AlarmEntity(Type type) {
        this.type = type;
        String action;
        switch (type) {
            case LocateStart:
                action = ReceiverConstant.LOCATION_START;
                break;
            case CONFIRMED_FREQUENCY_UPLOAD:
                action = ReceiverConstant.CONFIRMED_FREQUENCY_UPLOAD;
                break;
            case CONFIRMED_FREQUENCY_UPLOAD_BLE:
                action = ReceiverConstant.CONFIRMED_FREQUENCY_UPLOAD_BLE;
                break;
            case IsOpen:
                action = ReceiverConstant.CONFIRMED_Ble_IsOpen;
                break;
            case IsNetwork:
                action = ReceiverConstant.ACTION_Network;
                break;
            case HeartBeat:
                action = ReceiverConstant.ACTION_HEARTBEAT;
                break;
            case Log:
                action = ReceiverConstant.ACTION_LOG;
                break;
            default:
                action = null;
                break;
        }
        this.action = action;
    }

    public Type getType() {
        return type;
    }

    public boolean isRepeat() {
        return isRepeat;
    }

    public void setRepeat(boolean repeat) {
        isRepeat = repeat;
    }

    public long getInterval() {
        return interval;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public String getAction() {
        return action;
    }

    public long getFirstStartTime() {
        return firstStartTime;
    }

    public void setFirstStartTime(long firstStartTime) {
        this.firstStartTime = firstStartTime;
    }

    public int getAlarmManagerType() {
        return alarmManagerType;
    }

    public void setAlarmManagerType(int alarmManagerType) {
        this.alarmManagerType = alarmManagerType;
    }

    @Override
    public String toString() {
        return "AlarmEntity{" +
                "type=" + type +
                ", action='" + action + '\'' +
                ", firstStartTime=" + firstStartTime +
                ", interval=" + interval +
                ", isRepeat=" + isRepeat +
                ", alarmManagerType=" + alarmManagerType +
                '}';
    }
}
