package com.xrs.bluetooth_device.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.xrs.bluetooth_device.KApplication;
import com.xrs.bluetooth_device.MainActivity;
import com.xrs.bluetooth_device.model.ApnInfo;

import java.util.ArrayList;
import java.util.List;


public class ApnUtil {
    public static boolean hasAPN;
    // 新增一个cmnet接入点
    public int addAPN(String name, String apnName,String proxy,
                      String port,String user,String password,
                      String mcc,String mnc,Context context) {
        int id = -1;
        context = KApplication.sContext;
        String NUMERIC = getSIMInfo(context);
        if (NUMERIC == null) {
            return -1;
        }
        if (mcc == "" || mnc == ""){
            mcc = NUMERIC.substring(0, 3);
            mnc = NUMERIC.substring(3, 5);
        }

        ContentResolver resolver = context.getContentResolver();

        ContentValues values = new ContentValues();
        values.put("name", name);                                  //apn中文描述
        values.put("apn", apnName);                                     //apn名称
        values.put("type", "default");                            //apn类型
        values.put("numeric", NUMERIC);
        values.put("mcc", mcc);
        values.put("mnc", mnc);
        values.put("proxy", proxy);                                        //代理
        values.put("port", port);                                         //端口
        values.put("mmsproxy", "");                                     //彩信代理
        values.put("mmsport", "");                                      //彩信端口
        values.put("user", user);                                         //用户名
        values.put("server", "");                                       //服务器
        values.put("password", password);                                     //密码
        values.put("mmsc", "");                                          //MMSC
        Cursor c = null;
        Uri newRow = resolver.insert(Uri.parse("content://telephony/carriers"), values);
        if (newRow != null) {
            Log.e("APN","1");
            c = resolver.query(newRow, null, null, null, null);
            int idIndex = c.getColumnIndex("_id");
            c.moveToFirst();
            id = c.getShort(idIndex);
        }
        if (c != null)
            c.close();
        Log.e("APN id",id+"");
        return id;
    }

    public int getAPN(Context context, String apnName) {
        ContentResolver resolver = context.getContentResolver();
        Cursor c = resolver.query(Uri.parse("content://telephony/carriers"), new String[]{"_id", "name",
                "apn"}, "name ='" + apnName + "'", null, null);
        // 该项APN存在
        if (c != null && c.moveToNext()) {
            int id = c.getShort(c.getColumnIndex("_id"));
            String name = c.getString(c.getColumnIndex("name"));
            String apn = c.getString(c.getColumnIndex("apn"));
            Log.e("SetApnReceiver", "APN has exist " + id + name + apn);
            return id;
        } else {
            Log.e("SetApnReceiver", "APN has not exist ");
        }

        return -1;
    }

    public List<ApnInfo> getApnList(Context context) {
        List<ApnInfo> apnList = new ArrayList<>();

        Uri apnUri = Uri.parse("content://telephony/carriers");
        Cursor cursor = context.getContentResolver().query(apnUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String apn = cursor.getString(cursor.getColumnIndex("apn"));
                String mcc = cursor.getString(cursor.getColumnIndex("mcc"));
                String mnc = cursor.getString(cursor.getColumnIndex("mnc"));
                int id = cursor.getInt(cursor.getColumnIndex("_id"));

                ApnInfo apnInfo = new ApnInfo(id, name, apn, mcc, mnc);
                apnList.add(apnInfo);
            } while (cursor.moveToNext());

            cursor.close();
        }

        return apnList;
    }




    public String getCurrentAPN(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor c = resolver.query(Uri.parse("content://telephony/carriers/preferapn"), null, null, null, null);
        // 该项APN存在
        if (c != null && c.moveToNext()) {
            int id = c.getShort(c.getColumnIndex("_id"));
            String name = c.getString(c.getColumnIndex("name"));
            String apn = c.getString(c.getColumnIndex("apn"));
            String user = c.getString(c.getColumnIndex("user"));
            String pass = c.getString(c.getColumnIndex("password"));
            Log.e("SetApnReceiver", "current APN " + id +"," + name +","+ apn);
            Log.e("SetApnReceiver", "current APN " + user + pass);
            return "name:" + name +
                    ",apn:" + apn + "";
        } else {
            Log.e("SetApnReceiver", "current APN is null");
        }
        return "";
    }

    protected String getSIMInfo(Context context) {
        TelephonyManager iPhoneManager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);

        return iPhoneManager.getSimOperator();
    }

    // 设置接入点
    public void setAPN(int id,Context context) {
        ContentResolver resolver =context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put("apn_id", id);
        resolver.update(Uri.parse("content://telephony/carriers/preferapn"), values, null, null);
        Log.e("apn id","tt");
    }

    //查询是否存在
    public boolean checkAPN(String apnName,Context context) {
        // 检查当前连接的APN
        Cursor cr = context.getContentResolver().query(Uri.parse("content://telephony/carriers"), null, null, null, null);
        while (cr != null && cr.moveToNext()) {
            Log.e("开始搜索apn:",cr.getString(cr.getColumnIndex("apn")));
            if(cr.getString(cr.getColumnIndex("apn")).equals(apnName)){
                Log.e("搜索到apn:",cr.getString(cr.getColumnIndex("apn")));
                ApnUtil.hasAPN=true;
                return true;
            }
        }
        return false;
    }
}
