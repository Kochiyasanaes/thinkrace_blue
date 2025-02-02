package com.xrs.bluetooth_device.utils;

import static android.location.LocationManager.GPS_PROVIDER;

import android.location.Criteria;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;

import com.xrs.bluetooth_device.model.ICLocation;
import com.xrs.bluetooth_device.model.ICLocation.GPS_locationStatus;


import java.text.DecimalFormat;

public class NMEAListener implements NmeaListener, LocationListener {
    private static boolean listening = false;

    public boolean isListening() {
        return listening;
    }

    private LocationManager locationManager;
    private String locationInfo;

    private double longitude;
    private double latitude;

    private boolean noStop = false;


    private int SearchTimeout = 120;
    private int noSignalCount = 0;
    private int searchCout = 0;
    private GPS_CURRENT_STATUS gpsMode;
    int numberOfSatellites;
    Runtime runtime;

    private static int MaxFixedTimes = 3;

    private static int locationFixedCount = 0;


    private static location_callback mCallback = null;

    public interface location_callback {
        public void on_locating_changed(GPS_locationStatus gps_status);
    }

    public static void set_callback(location_callback callback) {
        mCallback = callback;
    }

    private GPS_locationStatus gps_status = new GPS_locationStatus();

    public GPS_locationStatus get_gps_status() {
        return gps_status;
    }


    public void setNoStop(boolean nostop) {
   /*     if (Config.noLocationCycle == false)
            noStop = nostop;
        else
            LogUtils.i("skip setNoStop " + nostop);*/
        LogUtils.i("setNoStop " + nostop);
        if (noStop == false) {
            switch (8) {
                case ICLocation.CUSTOM_MOD:
                case ICLocation.CUSTOM_MOD8_GPS:
                case ICLocation.EMERGNECY_MOD:
                case ICLocation.REALTIME_MOD:
                    //GPS 定位模式不关闭GPS，防止 1分钟定位超时后，重新关闭开启GPS。
                    //不给GPS继续定位的机会，造成第一次定位困难！
                    break;
                case ICLocation.LOWPOWER_MOD:
                case ICLocation.NORMAL_MOD:
                case ICLocation.CUSTOM_MOD8_WIFI:
                default:
                    LogUtils.i("非GPS定位模式，关闭GPS");
                    //非GPS定位模式的话，需要关闭GPS。
                    stopListen();
                    break;
            }

        }
    }

    public NMEAListener(LocationManager paramLocationManager, Runtime paramRuntime) {
        this.locationManager = paramLocationManager;
        this.runtime = paramRuntime;
        HandlerThread handlerThread = new HandlerThread("ic:nmea-thread");
        handlerThread.start();
        temHandler = new Handler(handlerThread.getLooper(), message -> {

            if (message.what == MSG_WHAT_GPS_TIMEOUT || message.what == MSG_WHAT_GPS_NOSTAR) {
                //no start
                //do nothing

            } else if (message.what == MSG_WHAT_GPS_FIXED) {
                //fixed
                 /*
                只有当前GPS已经定位，且location不为空才给 runtime赋值GPS坐标。
                延迟三秒后才设置runtime的定位标志，以便稳定坐标值。
                 */
//                runtime.getLocationInstance().setLocationStat(ICLocation.LOCATION_STAT.LOCATION_FIXED);
            }

            if (!noStop) {
                LogUtils.i("handleMessage MSG_WHAT_GPS_TIMEOUT/ MSG_WHAT_GPS_NOSTAR/ MSG_WHAT_GPS_FIXED " + message.what);

                stopListen();
            }
            return true;
        });
    }

    static int satindex = 0;

    public void onNmeaReceived(long paramLong, String nmeastr) {
//    EqcAlgorithmNative.native_EqcAlgorithmMethod5(paramString);
    LogUtils.i("Nmea :"+nmeastr+" "+paramLong);

//        if (Config.LOG_TOFILE)
//            LogUtils.logToFile("Nmea", nmeastr);

        if (nmeastr.startsWith("$RD_RSSI")) {
            //每组NMEA数据的头，只适用于 SL8521E，9820E
            gps_status.numSatelliteList.clear();
            gps_status.search_time_cost = searchCout;
//            runtime.getLocationInstance().numSatelliteList.clear(); //清除 SNR >0的卫星
//            runtime.getLocationInstance().max3Satellite.clear(); //清除 最大3个星值
        }

        if (nmeastr.startsWith("$GPVTG")) {
            //每组NMEA数据的尾，只适用于 SL8521E，9820E
            searchCout++;
            //最后一页，打印搜星情况，并作一些判断。
            StringBuilder sb = new StringBuilder();
//            sb.append("搜索" + searchCout + "次，搜索到卫星个数" + gps_status.satlite_numbss + ", [SNR>0] =" + runtime.getLocationInstance().numSatelliteList.size());
            sb.append(", Max 5 SNR [");
//            for (int i = 0; i < runtime.getLocationInstance().max3Satellite.size(); i++) {
////                sb.append(runtime.getLocationInstance().max3Satellite.get(i).getSnr() + ",");
//            }
            sb.append("]");
//            if (Config.LOG_TOFILE)
//                LogUtils.logToFile("GPS", sb.toString());

            LogUtils.i(sb.toString());
            // 卫星总数小于2 ，或者 snr小于20
//            if (runtime.getLocationInstance().numSatelliteList.size() < 3 && (runtime.getLocationInstance().max3Satellite.size() > 0 ? (runtime.getLocationInstance().max3Satellite.get(0).getSnr() < 20 ? true : false) : true)) {
//                noSignalCount++;
//            } else {
//                noSignalCount = 0;//只要有一次满足有信号，则重新开始计数。
//            }


            StringBuilder sb2 = new StringBuilder();
            if (!noStop) {
                if (searchCout > ICLocation.MAX_GPS_SEARCH_TIMEOUT) {
                    sb2.append(" 搜索" + searchCout + "次未定位，超过" + ICLocation.MAX_GPS_SEARCH_TIMEOUT + "限制退出GPS定位");

//                    runtime.getLocationInstance().setLocationStat(ICLocation.LOCATION_STAT.LOCATION_GPS_TIEMOUT);

                    stop(MSG_WHAT_GPS_TIMEOUT);
                }
                if (noSignalCount > ICLocation.MAX_GPS_NOSIGNAL_COUNT) {
                    sb2.append(" 搜索" + searchCout + "次，超过" + noSignalCount + "次没有收到星，");
//                    runtime.getLocationInstance().setLocationStat(ICLocation.LOCATION_STAT.LOCATION_GPS_TIEMOUT);
                    stop(MSG_WHAT_GPS_NOSTAR);
                }
            }
            LogUtils.i(sb2.toString());
//            if (Config.LOG_TOFILE)
//                LogUtils.logToFile("GPS", sb2.toString());


            if (mCallback != null)
                mCallback.on_locating_changed(get_gps_status());
        }
        if (nmeastr.startsWith("$GPGLL,") || nmeastr.startsWith("$GNGLL,")) {
            String tmplist[] = nmeastr.split(",");
            //      GPGLL编辑
            //（地理定位信息）
            //      例：$GPGLL,4250.5589,S,14718.5084,E,092204.999,A*2D
            //      字段0：$GPGLL，语句ID，表明该语句为Geographic Position（GLL）地理定位信息
            //      字段1：纬度ddmm.mmmm，度分格式（前导位数不足则补0）
            //      字段2：纬度N（北纬）或S（南纬）
            //      字段3：经度dddmm.mmmm，度分格式（前导位数不足则补0）
            //      字段4：经度E（东经）或W（西经）
            //      字段5：UTC时间，hhmmss.sss格式
            //      字段6：状态，A=定位，V=未定位
            //      字段7：校验值（$与*之间的数异或后的值）
            String Lat = tmplist[1];
            String lon = tmplist[3];
            String fixed = tmplist[6];
            String utctime = tmplist[5];
            try {
                TimeUtil.sGpsTime = utctime.substring(0, 6);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //      LogUtils.i("GPGLL Lat="+Lat+",Lon="+lon+",fixed="+fixed);

        }
        else if (nmeastr.startsWith("$GPRMC,") || nmeastr.startsWith("$GNRMC,")) {
            //      GPRMC编辑
            //（推荐定位信息数据格式）
            //      例：$GPRMC,024813.640,A,3158.4608,N,11848.3737,E,10.05,324.27,150706,,,A*50
            //      字段0：$GPRMC，语句ID，表明该语句为Recommended Minimum Specific GPS/TRANSIT Data（RMC）推荐最小定位信息
            //      字段1：UTC时间，hhmmss.sss格式
            //      字段2：状态，A=定位，V=未定位
            //      字段3：纬度ddmm.mmmm，度分格式（前导位数不足则补0）
            //      字段4：纬度N（北纬）或S（南纬）
            //      字段5：经度dddmm.mmmm，度分格式（前导位数不足则补0）
            //      字段6：经度E（东经）或W（西经）
            //      字段7：速度，节，Knots
            //      字段8：方位角，度
            //      字段9：UTC日期，DDMMYY格式
            //      字段10：磁偏角，（000 - 180）度（前导位数不足则补0）
            //      字段11：磁偏角方向，E=东W=西
            //      字段12：模式，A=自动，D=差分，E=估测，N=数据无效（3.0协议内容）
            //      字段13：校验值（$与*之间的数异或后的值）
            String tmplist[] = nmeastr.split(",");
            String utctime = tmplist[1];
            String Lat = tmplist[3];
            String lon = tmplist[5];
            String fixed = tmplist[2];
            String speed = tmplist[7];
            String date = tmplist[9];
            String heading = tmplist[8];
            try {
                TimeUtil.sGpsTime = utctime.substring(0, 6);
                TimeUtil.sGpsDate = date;
            } catch (Exception e) {
                e.printStackTrace();
            }
            //      LogUtils.i("GPRMC GPSTime="+utctime+",Lat="+Lat+",Lon="+lon+",fixed="+fixed+",speed="+speed);
            //      if (Config.LOG_TOFILE)
            //        FileOutputUtil.fileOutput(this.runtime.getCacheDir(), "GPS", "GPRMC GPSTime="+utctime+",Lat="+Lat+",Lon="+lon+",fixed="+fixed+",speed="+speed);
            if (fixed.trim().equals("A")) {
                if (speed.trim().isEmpty()) speed = "0.0";
//                runtime.getLocationInstance().setSpeed(Float.valueOf(speed));
                //方向角是指的当前运动方向相对于正北的磁偏角,0度是正北
                if(heading.trim().isEmpty() ) heading = "0.0";
                float heading_float = 0.0f;
                try{
                    heading_float = Float.parseFloat(heading);
                } catch (Exception e)
                {
                    LogUtils.e(e.getMessage(),e);
                }
//                runtime.getLocationInstance().setHeading(heading_float);
//                runtime.getLocationInstance().setHeadingStr(heading);

            }





        }
        else if (nmeastr.startsWith("$GPGSV,") || nmeastr.startsWith("$GLGSV,") || nmeastr.startsWith("$GAGSV,") || nmeastr.startsWith("$GBGSV,") || nmeastr.startsWith("$BDGSV,")) {

            //    GPGSV编辑
            //            (可见卫星信息)
            //    例：$GPGSV,3,1,10,20,78,331,45,01,59,235,47,22,41,069,,13,32,252,45*70
            //    字段0：$GPGSV，语句ID，表明该语句为GPS Satellites in View（GSV）可见卫星信息
            //    字段1：本次GSV语句的总数目（1 - 3）
            //    字段2：本条GSV语句是本次GSV语句的第几条（1 - 3）
            //    字段3：当前可见卫星总数（00 - 12）（前导位数不足则补0）
            //    字段4：PRN 码（伪随机噪声码）（01 - 32）（前导位数不足则补0）
            //    字段5：卫星仰角（00 - 90）度（前导位数不足则补0）
            //    字段6：卫星方位角（00 - 359）度（前导位数不足则补0）
            //    字段7：信噪比（00－99）dbHz
            //    字段8：PRN 码（伪随机噪声码）（01 - 32）（前导位数不足则补0）
            //    字段9：卫星仰角（00 - 90）度（前导位数不足则补0）
            //    字段10：卫星方位角（00 - 359）度（前导位数不足则补0）
            //    字段11：信噪比（00－99）dbHz
            //    字段12：PRN 码（伪随机噪声码）（01 - 32）（前导位数不足则补0）
            //    字段13：卫星仰角（00 - 90）度（前导位数不足则补0）
            ////    字段14：卫星方位角（00 - 359）度（前导位数不足则补0）
            ////    字段15：信噪比（00－99）dbHz
            //    字段16：校验值（$与*之间的数异或后的值）

            String tmplist[] = nmeastr.split(",");
            int maxPageCount = 0;
            int currentPage = 0;
            int SatlitesCount = 0;

            try {
                maxPageCount = Integer.valueOf(tmplist[1]);
            } catch (Exception e) {
            }
            try {
                currentPage = Integer.valueOf(tmplist[2]);
            } catch (Exception e) {
            }
            try {
                SatlitesCount = Integer.valueOf(tmplist[3]);
            } catch (Exception e) {
            }
            if (maxPageCount > 0 && currentPage == 1) {
                //第一页把数组初始化一遍。
                satindex = 0;
                gps_status.satlite_numbss = SatlitesCount;
            }
            int satcount = (tmplist.length - 4) / 4;
//      LogUtils.i("Nmea :"+nmeastr+" "+satcount);
//            runtime.getLocationInstance().set_adding_satlite_flag(true);
            for (int i = 0; i < satcount; i++) {
                satindex++;
                String prn = tmplist[i * 4 + 4];
                String snr = tmplist[i * 4 + 4 + 3];
                if (snr.contains("*")) {
                    snr = snr.substring(0, snr.indexOf("*"));
                }
                if (snr.trim().isEmpty()) {
                    snr = "0";
                }
//        LogUtils.i("Sat=" + satindex + ",prn=" + prn + ",snr=" + snr);

                int intprn = 0;
                try {
                    intprn = Integer.valueOf(prn);
                } catch (Exception e) {
                }
                float floatsnr = 0;
                try {
                    floatsnr = Float.valueOf(snr);
                } catch (Exception e) {
                }
                if (floatsnr > 0) {
                    //排序取得最大3个SNR
                    get3MaxSnrSatellite(new ICLocation.GpsSatellitePriv(intprn, floatsnr));
//                    runtime.getLocationInstance().numSatelliteList.add(new ICLocation.GpsSatellitePriv(intprn, floatsnr));
                    gps_status.numSatelliteList.add(new ICLocation.GpsSatellitePriv(intprn, floatsnr));
                }
            }

//            runtime.getLocationInstance().set_adding_satlite_flag(false);

        }
        else if (nmeastr.startsWith("$GPGGA,") || nmeastr.startsWith("$GNGGA,")) {

//    GPGGA编辑
//（定位信息）
//    例：$GPGGA,092204.999,4250.5589,S,14718.5084,E,1,04,24.4,12.2,M,19.7,M,,0000*1F
//    字段0：$GPGGA，语句ID，表明该语句为Global Positioning System Fix Data（GGA）GPS定位信息
//    字段1：UTC 时间，hhmmss.sss，时分秒格式
//    字段2：纬度ddmm.mmmm，度分格式（前导位数不足则补0）
//    字段3：纬度N（北纬）或S（南纬）
//    字段4：经度dddmm.mmmm，度分格式（前导位数不足则补0）
//    字段5：经度E（东经）或W（西经）
//    字段6：GPS状态，0=不可用(FIX NOT valid)，1=单点定位(GPS FIX)，2=差分定位(DGPS)，3=无效PPS，4=实时差分定位（RTK FIX），5=RTK FLOAT，6=正在估算
//    字段7：正在使用的卫星数量（00 - 12）（前导位数不足则补0）
//    字段8：HDOP水平精度因子（0.5 - 99.9）
//    字段9：海拔高度（-9999.9 - 99999.9）
//    字段10：单位：M（米）
//    字段11：地球椭球面相对大地水准面的高度 WGS84水准面划分
//    字段12：WGS84水准面划分单位：M（米）
//    字段13：差分时间（从接收到差分信号开始的秒数，如果不是差分定位将为空）
//    字段14：差分站ID号0000 - 1023（前导位数不足则补0，如果不是差分定位将为空）
//    字段15：校验值（$与*之间的数异或后的值）

            String tmplist[] = nmeastr.split(",");
            String utctime = tmplist[1];
            String Lat = tmplist[2];
            String lon = tmplist[4];
            String latname = tmplist[3];
            String lonname = tmplist[5];
            String fixed = tmplist[6];
            String usedStarts = tmplist[7];
            String hdop = tmplist[8];
            try {
                TimeUtil.sGpsTime = utctime.substring(0, 6);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ((fixed.trim().equals("1")) || (fixed.trim().equals("2")) || (fixed.trim().equals("6"))) {


                // 转换GPS坐标为度，以便在各种应用中可以方便查找。
                try {
                    latitude = Double.valueOf(Lat);
                    longitude = Double.valueOf(lon);
                } catch (Exception e) {
                }
                // 转换单位,从分转换到度

                double intdu = (int) (longitude / 100f); //小数转换到分为单位
                double du = (longitude - intdu * 100) / 60f;
                double last = intdu + du;
                longitude = last;
                intdu = (int) (latitude / 100f); //小数转换到分为单位
                du = (latitude - intdu * 100) / 60f;
                last = intdu + du;
                latitude = last;

                // 经纬度 保留4位小数点
                DecimalFormat df = new DecimalFormat("#.0000000");
                String strLatitude = df.format(latitude);
                String strLongitude = df.format(longitude);

                //定位
                StringBuilder sb = new StringBuilder();
                sb.append("GPGGA, location time= " + TimeUtil.getLocalTime() + ", FixedCount=" + locationFixedCount + ", GPSTime=" + utctime + ", Lat(" + latname + ")=" + Lat + " (" + strLatitude + "), Lon(" + lonname + ")=" + lon + " (" + strLongitude + "), fixed=" + fixed + ", hdop=" + hdop + ", usedStarts=" + usedStarts);
                LogUtils.i(sb.toString());
                //定位了，
                if (hdop != null && hdop.trim().length() > 0 && Float.valueOf(hdop) < ICLocation.MAX_HDOP) {
                    locationFixedCount++;
                } else {
                    LogUtils.i("HDOP is larger than " + ICLocation.MAX_HDOP + " , skip this location!");
                }
                if (locationFixedCount > MaxFixedTimes) {
                       /*
                        只有当前GPS已经定位，且location不为空才给 runtime赋值GPS坐标。
                        延迟MaxFixedTimes秒后才设置runtime的定位标志，以便稳定坐标值。
                      */
//                    runtime.getLocationInstance().setLocation(Lat, lon);
//                    runtime.getLocationInstance().setLocationGPS(strLatitude, strLongitude);
//                    runtime.getLocationInstance().setLocationlatlonName(latname, lonname);
//                    runtime.getLocationInstance().setUsedStarts(Integer.valueOf(usedStarts.trim()));
//                    runtime.getLocationInstance().setHod(Float.valueOf(hdop.trim()));
//                    runtime.getLocationInstance().setLocationStat(ICLocation.LOCATION_STAT.LOCATION_FIXED);

                    LogUtils.i("定位" + locationFixedCount + "次 > " + MaxFixedTimes + ", set LOCATION_FIXED ");
//                    if (Config.LOG_TOFILE)
//                        LogUtils.logToFile("GPS", "定位" + locationFixedCount + "次 > " + MaxFixedTimes + ", set LOCATION_FIXED ");
//                    if (!noStop) {
//                        stopListen();
//                    }
                }
                gps_status.locating_status = 1; //fixed
            } else {
                gps_status.locating_status = 0; //notfixed
            }
            try {
                gps_status.hdop = Float.valueOf(hdop.trim());
                gps_status.used_gps_numbs = Integer.valueOf(usedStarts.trim());
            } catch (Exception e) {
            }

        }
        else if (nmeastr.startsWith("$GPGSA,") || nmeastr.startsWith("$GNGSA,")) {

//    GPGSA编辑
//（ 当前卫星信息）
//    例：$GPGSA,A,3,01,20,19,13,,,,,,,,,40.4,24.4,32.2*0A
//    字段0：$GPGSA，语句ID，表明该语句为GPS DOP and Active Satellites（GSA）当前卫星信息
//    字段1：定位模式(选择2D/3D)，A=自动选择，M=手动选择
//    字段2：定位类型，1=未定位，2=2D定位，3=3D定位
//    字段3：PRN码（伪随机噪声码），第1信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段4：PRN码（伪随机噪声码），第2信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段5：PRN码（伪随机噪声码），第3信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段6：PRN码（伪随机噪声码），第4信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段7：PRN码（伪随机噪声码），第5信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段8：PRN码（伪随机噪声码），第6信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段9：PRN码（伪随机噪声码），第7信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段10：PRN码（伪随机噪声码），第8信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段11：PRN码（伪随机噪声码），第9信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段12：PRN码（伪随机噪声码），第10信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段13：PRN码（伪随机噪声码），第11信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段14：PRN码（伪随机噪声码），第12信道正在使用的卫星PRN码编号（00）（前导位数不足则补0）
//    字段15：PDOP综合位置精度因子（0.5 - 99.9）
//    字段16：HDOP水平精度因子（0.5 - 99.9）
//    字段17：VDOP垂直精度因子（0.5 - 99.9）
//    字段18：校验值（$与*之间的数异或后的值）

//      String tmplist[]=nmeastr.split(",");
//      String locationmod=tmplist[1];
//      String fixed=tmplist[2];
//      String hdop=tmplist[16];
//      if (!fixed.trim().equals("1")){
//        //定位
//        LogUtils.i("GPGSA fixed="+fixed+",hdop="+hdop);
//      }
        }
    }

    public void onProviderDisabled(String paramString) {
    }

    public void onProviderEnabled(String paramString) {
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    public void onStatusChanged(String paramString, int paramInt, Bundle paramBundle) {
    }

    public boolean gpsColdStart() {
//冷启动
        Bundle bundle = null;
        locationManager.sendExtraCommand("gps", "force_xtra_injection", bundle); //command
        locationManager.sendExtraCommand("gps", "force_time_injection", bundle);
        boolean b1 = locationManager.sendExtraCommand("gps", "delete_aiding_data", bundle);
        return b1;

    }

    public boolean gpsWormStart() {

//温启动
        Bundle bundleWarm = new Bundle();
        bundleWarm.putBoolean("ephemeris", true);
        locationManager.sendExtraCommand("gps", "force_xtra_injection", bundleWarm); //command
        locationManager.sendExtraCommand("gps", "force_time_injection", bundleWarm);
        boolean b2 = locationManager.sendExtraCommand("gps", "delete_aiding_data", bundleWarm);
        return b2;
    }

    public boolean gpsHotStart() {
//热启动
        Bundle bundleHot = new Bundle();
        bundleHot.putBoolean("almanac", true);
        locationManager.sendExtraCommand("gps", "force_xtra_injection", bundleHot); //command
        locationManager.sendExtraCommand("gps", "force_time_injection", bundleHot);
        boolean b3 = locationManager.sendExtraCommand("gps", "delete_aiding_data", bundleHot);
        return b3;
    }


    private final int MSG_WHAT_GPS_FIXED = 300;
    private final int MSG_WHAT_GPS_TIMEOUT = 100;
    private final int MSG_WHAT_GPS_NOSTAR = 200;
    private final int MSG_WHAT_GPS_FIXED_NO_STOP = 400;

    Handler temHandler;

    private void get3MaxSnrSatellite(ICLocation.GpsSatellitePriv gst) {

        float snr = gst.getSnr();
//        for (int k = 0; k < runtime.getLocationInstance().max3Satellite.size(); k++) {
//            if (runtime.getLocationInstance().max3Satellite.get(k).getPrn() == gst.getPrn())
//                runtime.getLocationInstance().max3Satellite.remove(k);
//        }
//        runtime.getLocationInstance().max3Satellite.add(gst);//先添加进来。

        //从大到小排序
//        for (int i = 0; i < runtime.getLocationInstance().max3Satellite.size(); i++) {
//            for (int j = i; j < runtime.getLocationInstance().max3Satellite.size(); j++) {
//                if (runtime.getLocationInstance().max3Satellite.get(i).getSnr() < runtime.getLocationInstance().max3Satellite.get(j).getSnr()) {
//                    ICLocation.GpsSatellitePriv ts = runtime.getLocationInstance().max3Satellite.get(j);
//                    runtime.getLocationInstance().max3Satellite.remove(j);
//                    runtime.getLocationInstance().max3Satellite.add(i, ts);//大的放前面
//                }
//            }
//        }
//        if (runtime.getLocationInstance().max3Satellite.size() > 5) { //超过三个，谁小就删掉
//            runtime.getLocationInstance().max3Satellite.remove(5);
//        }

    }

    //停止定位
    private void stop(int id) {

        // 经纬度 保留6位小数点
//    DecimalFormat df = new DecimalFormat("#.000000");
//    String strLatitude = df.format(latitude);
//    String strLongitude = df.format(longitude);
//    runtime.setLatitude(strLatitude);
//    runtime.setLongitude(strLongitude);
//
//    LogUtils.i("Location " +runtime.getLocationInstance().getLocationStat().name());
        //发送停止消息
        temHandler.sendEmptyMessage(id);
    }

    boolean isFrist = true;


    public boolean startListen() {

        if (this.listening == false) {
//            Settings.Secure.setLocationProviderEnabled(runtime.getContextProxy().getResolver(), GPS_PROVIDER, true);

//            runtime.getLocationInstance().setLocationStat(ICLocation.LOCATION_STAT.LOCATION_OPEN);

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);// 设置为最大精度
            criteria.setAltitudeRequired(false);//不要求海拔信息
            criteria.setBearingRequired(false);// 不要求方位信息
            criteria.setCostAllowed(true);//是否允许付费
            criteria.setPowerRequirement(Criteria.POWER_LOW);// 对电量的要求

            String provider = locationManager.getBestProvider(criteria, true);


            LogUtils.i("GPS StartListen with Provider " + provider);

//            if (Config.LOG_TOFILE)
//                LogUtils.logToFile("GPS", "GPS StartListen with Provider " + provider);

            this.locationManager.requestLocationUpdates(provider, 0L, 0.0F, this);

            this.listening = true;
            noSignalCount = 0;
            longitude = 0;
            latitude = 0;
            searchCout = 0;
            locationFixedCount = 0;
            gpsMode = GPS_CURRENT_STATUS.GPS_LISTENING;
//      locationManager.addGpsStatusListener(statusListener);//监听搜星状态
            locationManager.addNmeaListener(this); //监听NMEA 协议数据。

            //做个热启动
            return true;//gpsHotStart();
        } else {
            LogUtils.i(" location listening， skip !");
//
//            runtime.getLocationInstance().setLocationStat(ICLocation.LOCATION_STAT.LOCATION_SEARCHING);
//
//            if (Config.LOG_TOFILE) LogUtils.logToFile("GPS", " location listening， skip !");

            return true;
        }
    }

    public boolean isGpsOpening() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void stopListen() {
        this.locationManager.removeNmeaListener(this);
        this.locationManager.removeUpdates(this);
        LogUtils.i("stopListen 定位 ");
//        if (Config.LOG_TOFILE) LogUtils.logToFile("GPS", "stopListen 定位 ");
        if (listening == true) {
            this.listening = false;
//            Settings.Secure.setLocationProviderEnabled(runtime.getContextProxy().getResolver(), GPS_PROVIDER, false);
            LogUtils.i("stopListen 定位 done ");
        } else {
            if (isGpsOpening()) {
//                Settings.Secure.setLocationProviderEnabled(runtime.getContextProxy().getResolver(), GPS_PROVIDER, false);
                LogUtils.i("flag stoped but gps still opening , stop GPS!");
            } else {
                LogUtils.i("stopListen 定位 already stop skip stopListen");
            }
        }
    }

    public enum GPS_CURRENT_STATUS {
        GPS_LISTENING,
        GPS_FIXED,
        GPS_NOTFIXED,
        GPS_TIMEOUT,
        GPS_NOSINGAL,
    }
}
