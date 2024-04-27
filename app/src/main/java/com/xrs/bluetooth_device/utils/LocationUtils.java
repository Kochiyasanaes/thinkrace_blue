package com.xrs.bluetooth_device.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class LocationUtils {

    private LocationManager locationManager;
    static LocationUtils locationUtils;

    public static LocationUtils getInstance() {
        if (locationUtils == null) {
            locationUtils = new LocationUtils();
        }
        return locationUtils;
    }

    public String getLocations(Context context) {
        if (!isOPenGPS(context)){
            Toast.makeText(context, "未开启定位，无法获取地理位置", Toast.LENGTH_SHORT).show();
            return "";
        }
        String strLocation = "";
        DecimalFormat df = new DecimalFormat("#####0.0000");
        if (!checkPermission(context, permission.ACCESS_COARSE_LOCATION)) {
            Toast.makeText(context, "定位权限关闭，无法获取地理位置", Toast.LENGTH_SHORT).show();
        }
        try {
            //获取系统的服务，
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            //创建一个criteria对象
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            //设置不需要获取海拔方向数据
            criteria.setAltitudeRequired(false);
            criteria.setBearingRequired(false);
            //设置允许产生资费
            criteria.setCostAllowed(true);
            //要求低耗电
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            String provider = locationManager.getBestProvider(criteria, true);
            provider = LocationManager.GPS_PROVIDER;
            Log.e("LocationTool", "Location Provider is " + provider);
            @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(provider);

            //第一次获得设备的位置
            if (location != null) {
//                strLocation = df.format(location.getLatitude()) + "," + df.format(location.getLongitude());
                // 耗时操作
                strLocation += location.getLatitude() + ","+location.getLongitude()+"," + location.getTime()+","+location.getProvider();
                if (strLocation.equals("")) {
                    strLocation += "" + convertAddress(context, location.getLatitude(), location.getLongitude());
                }
            }


        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return strLocation;

    }

    public String convertAddress(Context context, double latitude, double longitude) {
        Geocoder mGeocoder = new Geocoder(context, Locale.getDefault());
        StringBuilder mStringBuilder = new StringBuilder();

        try {
            List<Address> mAddresses = mGeocoder.getFromLocation(latitude, longitude, 1);
            if (!mAddresses.isEmpty()) {
                Address address = mAddresses.get(0);
                mStringBuilder.append(address.getLocality()).append(address.getThoroughfare());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mStringBuilder.toString();
    }

    private boolean checkPermission(Context context, permission permName) {
        int perm = context.checkCallingOrSelfPermission("android.permission." + permName.toString());
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    private enum permission {
        ACCESS_COARSE_LOCATION,
        ACCESS_FINE_LOCATION
    }

    private String getLocationAddress(Context mContext, Location location) {
        String addNow = "";
        Geocoder geoCoder = new Geocoder(mContext, Locale.CHINESE);
        try {
            List<Address> addresses = geoCoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(),
                    1);
            Address address = addresses.get(0);
            Log.w("LocationTool", "远程获取定位全部为: " + address.toString());

            if (address.getAddressLine(0) != null && !address.getAddressLine(0).equals("")) {
                addNow = address.getAddressLine(0);
                Log.w("LocationTool", "获取成功第一种: " + addNow);
            } else if (addNow.equals("") && address.getFeatureName() != null && !address.getFeatureName().equals("")) {
                addNow = address.getLocality() + address.getFeatureName();
                Log.w("LocationTool", "获取成功第二种: " + addNow);
            } else {
                int maxLine = address.getMaxAddressLineIndex();
                if (maxLine >= 2) {
                    addNow = address.getAddressLine(1) + address.getAddressLine(2);
                } else {
                    addNow = address.getAddressLine(1);
                }
                Log.w("LocationTool", "获取成功第三种: " + addNow);
            }

        } catch (IOException e) {
            addNow = "";
            e.printStackTrace();
        }
        if (addNow.contains("null")) {
            addNow = addNow.replaceAll("null", "");
        }
        return addNow;
    }
    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    private boolean isOPenGPS(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // GPS定位
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 网络服务定位
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }
        return false;
    }
}
