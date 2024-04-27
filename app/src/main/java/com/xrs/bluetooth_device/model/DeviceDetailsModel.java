package com.xrs.bluetooth_device.model;

/**
 * @ClassName DeviceDetailsModel
 * @Author kotlin
 * @Email 949390151@qq.com
 * @Date 2022/10/21 15:55
 * ^_^^_^^_^^_^^_^^_^^_^
 */
public class DeviceDetailsModel {
    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getImsi() {
        return imsi;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public String getAv() {
        return av;
    }

    public void setAv(String av) {
        this.av = av;
    }

    public DeviceDetailsModel(String imei, String imsi, String iccid, String av) {
        this.imei = imei;
        this.imsi = imsi;
        this.iccid = iccid;
        this.av = av;
    }

    String imei;
    String imsi;
    String iccid;
    String av;//固件版本
}
