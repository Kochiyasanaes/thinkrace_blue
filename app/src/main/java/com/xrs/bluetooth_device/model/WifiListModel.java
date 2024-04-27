package com.xrs.bluetooth_device.model;

public class WifiListModel {
    public String SSID;
    public String BSSID;

    public WifiListModel(String SSID,String BSSID){
        this.BSSID = BSSID;
        this.SSID = SSID;
    }
}
