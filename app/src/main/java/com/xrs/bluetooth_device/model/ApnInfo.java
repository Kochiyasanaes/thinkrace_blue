package com.xrs.bluetooth_device.model;

/**
 * @ClassName ApnInfo
 * @Author kotlin
 * @Email 949390151@qq.com
 * @Date 2023/8/22 16:10
 * ^_^^_^^_^^_^^_^^_^^_^
 */
public class ApnInfo {
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getApn() {
        return apn;
    }

    public String getMcc() {
        return mcc;
    }

    public String getMnc() {
        return mnc;
    }

    private int id;
    private String name;
    private String apn;
    private String mcc;
    private String mnc;

    public ApnInfo(int id, String name, String apn, String mcc, String mnc) {
        this.id = id;
        this.name = name;
        this.apn = apn;
        this.mcc = mcc;
        this.mnc = mnc;
    }

    // 添加需要的 getter 和 setter 方法
}
