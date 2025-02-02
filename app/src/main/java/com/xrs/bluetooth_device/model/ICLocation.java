package com.xrs.bluetooth_device.model;




import com.xrs.bluetooth_device.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ICLocation {

    public static class GpsSatellitePriv {
        int prn;
        float snr;

        public GpsSatellitePriv(int _prn, float _snr) {
            prn = _prn;
            snr = _snr;
        }

        public int getPrn() {
            return prn;
        }

        public float getSnr() {
            return snr;
        }
    }

    public List<GpsSatellitePriv> numSatelliteList = new ArrayList<GpsSatellitePriv>(); // 卫星信号
    public List<GpsSatellitePriv> max3Satellite = new ArrayList<GpsSatellitePriv>(); // 卫星信号
    private static boolean adding_satlite_flag = false;

    public void set_adding_satlite_flag(boolean flag) {
        adding_satlite_flag = flag;
    }

    public List<GpsSatellitePriv> getNumSatelliteList() {
        if (adding_satlite_flag)
            return null;
        return numSatelliteList;
    }


    private LOCATION_STAT currentStat;
    private GPS_LocationPoint currentLP = null;
    private LBS_Data currentLBS = null;

    public class GPS_LocationPoint {
        public double lat;
        public double lon;
        public float speed;
        public float heading;
        public int used_gps_numbs;
        public String latStr;
        public String lonStr;
        public String latGPSStr;
        public String lonGPSStr;
        public String lat_name;
        public String lon_name;
        public String heading_str;
        public float hdop;
        public String google_location_url_for_last_gps_fixed_position;
    }


    public static class GPS_locationStatus {
        public List<GpsSatellitePriv> numSatelliteList = new ArrayList<GpsSatellitePriv>(); // 卫星信号
        public int used_gps_numbs;
        public float hdop;
        public int satlite_numbss;
        public int search_time_cost;
        public int locating_status;
    }

    public class LBS_Data {
        HashMap<String, Integer> wifi_aps = null;
        HashMap<String, Integer> gsm_stations = null;
    }

    public enum LOCATION_STAT {
        LOCATION_OPEN,
        LOCATION_FIXED,
        LOCATION_SEARCHING,
        LOCATION_GPS_FAILED,
        LOCATION_GPS_TIEMOUT,
        LOCATION_WIFI_FAILED,
        LOCATION_CLOSED,
    }


    public static final int NORMAL_MOD = 1;     //location rate 1 time per 15 minutes
    public static final int LOWPOWER_MOD = 2;   // location rate 1 time per hour
    public static final int REALTIME_MOD = 3; //location rate 1 time per minutes
    public static final int EMERGNECY_MOD = 4; //location rate 1 time per minutes com.ic.work 5 minutes
    public static final int CUSTOM_MOD = 5; //location rate 1 time per minutes
    public static final int CUSTOM_MOD8_GPS = 8; //location rate 1 time per minutes
    public static final int CUSTOM_MOD8_WIFI = 9; //location rate 1 time per minutes

    private int mworkingMod;
    private int location_cycle;

    public static final float MAX_HDOP = 10.0f;
    public static final int MAX_GPS_NOSIGNAL_COUNT = 15;
    public static final int MAX_GPS_SEARCH_TIMEOUT = 120;

    public void setLastWorkingMode(int wkmod) {
        last_workmode = wkmod;
    }

    public void setWorkingMod(int wkmod) {
        mworkingMod = wkmod;
        

    }

    public int getLastWorkingMode() {
        return last_workmode;
    }

    public void setLocation_cycle(int _locatCycle) {
        location_cycle = _locatCycle;

    }

    private static int last_workmode = CUSTOM_MOD8_GPS; //上一次工作模式，主要用来EMERGENCY_MODE 结束后返回 使用。

    public int getWorkingMod() {
        return mworkingMod;
    }

    public int getLocation_cycle() {
        return location_cycle;
    }

    public ICLocation() {
        if (currentLP == null)
            currentLP = new GPS_LocationPoint();

        currentStat = LOCATION_STAT.LOCATION_CLOSED;
        if (currentLBS == null)
            currentLBS = new LBS_Data();
        if (currentLBS.gsm_stations == null)
            currentLBS.gsm_stations = new HashMap<String, Integer>();
        if (currentLBS.wifi_aps == null)
            currentLBS.wifi_aps = new HashMap<String, Integer>();

        init_data();
    }

    public void setLocationStat(LOCATION_STAT stat) {
        this.currentStat = stat;
        //赋值最后一次gps坐标给google查询url
        if(stat == LOCATION_STAT.LOCATION_FIXED)
        {
            String latStr = this.getLocation().latGPSStr;
            String lonStr = this.getLocation().lonGPSStr;
            String latname = this.getLocation().lat_name;
            String lonname = this.getLocation().lon_name;
            if (latname.equals("N"))
            {
                latname="";
            }else{
                latname="-";
            }
            if(lonname.equals("E"))
            {
                lonname="";
            }else{
                lonname="-";
            }
           this.getLocation().google_location_url_for_last_gps_fixed_position = String.format("%s%s,%s%s",latname , latStr, lonname ,lonStr );
        }
    }

    private void init_data() {
//        currentLP.lat=2232.9806f;
//        currentLP.lon=11404.9355f;
//        currentLP.speed=000.1f;
//        currentLP.heading=0.0f;
//
//        currentLBS.wifi_aps.put("74-DE-2B-44-88-8C",97);
//        currentLBS.wifi_aps.put("74-DE-2B-44-88-8D",97);


    }

    public LOCATION_STAT getLocationStat() {
        return currentStat;
    }

    private String LocationTime;

    public void setLocationTime(String time) {

        LocationTime = time;
    }

    public String getLocationTime() {
        return LocationTime;
    }

    private String LocationType;

    public void setLocationType(String type) {

        LocationType = type;
    }

    public String getLocationType() {
        return LocationType;
    }

    public GPS_LocationPoint getLocation() {
        return currentLP;
    }

    public void setHod(float _hot) {
        currentLP.hdop = _hot;
    }

    public float getHdop() {
        return currentLP.hdop;
    }

    public void setSpeed(float _speed) {
        currentLP.speed = _speed;
    }

    public float getSpeed() {
        return currentLP.speed;
    }

    public void setLocation(double lat, double lon) {
        currentLP.lat = lat;
        currentLP.lon = lon;
    }

    public void setLocationlatlonName(String latname, String lonname) {
        currentLP.lat_name = latname;
        currentLP.lon_name = lonname;
    }

    public void setLocation(String lat, String lon) {
        currentLP.latStr = lat;
        currentLP.lonStr = lon;
    }

    public void setLocationGPS(String lat, String lon) {
        currentLP.latGPSStr = lat;
        currentLP.lonGPSStr = lon;
    }

    public void setHeading(float heding) {
        currentLP.heading = heding;
    }


    public void setHeadingStr(String hedingstr) {
        currentLP.heading_str = hedingstr;
    }


    public float getheading() {
        return currentLP.heading;
    }

    public String getheadingStr() {
        return currentLP.heading_str;
    }

    public void setUsedStarts(int starts) {
        currentLP.used_gps_numbs = starts;
    }

    public int getUsedStarts() {
        return currentLP.used_gps_numbs;
    }

    public void setLocation(double lat, double lon, float speed, float heading, int userdgpsnumbers, float hdop) {
        currentLP.lat = lat;
        currentLP.lon = lon;
        currentLP.speed = speed;
        currentLP.heading = heading;
        currentLP.used_gps_numbs = userdgpsnumbers;
        currentLP.latStr = "";
        currentLP.lonStr = "";
        currentLP.hdop = hdop;
    }

    public void setLocation(String lat, String lon, float speed, float heading, int userdgpsnumbers, float hdop) {
        currentLP.lat = 0;
        currentLP.lon = 0;
        currentLP.speed = speed;
        currentLP.heading = heading;
        currentLP.used_gps_numbs = userdgpsnumbers;
        currentLP.latStr = lat;
        currentLP.lonStr = lon;
        currentLP.hdop = 99.9f;
    }

    public void clearLBSdata() {
        currentLBS.gsm_stations.clear();
    }

    public void clearWiFidata() {
        currentLBS.wifi_aps.clear();
    }

    public void append_gsmstation(String station, int rssi) {
        currentLBS.gsm_stations.put(station, rssi);
    }

    public void append_wifiap(String wifi, int rssi) {
        currentLBS.wifi_aps.put(wifi, rssi);
    }


    //        MCC，Mobile Country Code，移动国家代码（中国的为460）；
//        MNC，Mobile Network Code，移动网络号码（中国移动为00，中国联通为01）；
//        LAC，Location Area Code，位置区域码；
//        CID，Cell Identity，基站编号，是个16位的数据（范围是0到65535）。


    private String MCC = "0";
    private String MNC = "0";
    private int LAC = 0;
    private int CID = 0;
    private int rxLevel = 0;
    private boolean dataChanged = false;

    public void setMCC(String mcc) {
        MCC = mcc;
        dataChanged = true;
    }

    public void setMNC(String mnc) {
        LogUtils.d("MNC==> " + mnc);
        MNC = mnc;
        dataChanged = true;
    }

    public void setLAC(int lac) {
        LogUtils.d("lac ==> " + lac);
        LAC = lac;
        dataChanged = true;
    }

    public void setCID(int cid) {
        LogUtils.d("cid===> " + cid);
        CID = cid;
        dataChanged = true;
    }

    public void setRxLevel(int level) {
        rxLevel = level;
    }

    public boolean isDataChanged() {
        return dataChanged;
    }

    public String getMCC() {
        return MCC;
    }

    public String getMNC() {
        return MNC;
    }

    public int getRxLevel() {
        return rxLevel;
    }

    public long getLAC() {
        return LAC;
    }

    public long getGSMStationCID() {
        return CID;
    }

    public LBS_Data getLBSdata() {
        return currentLBS;
    }

}
