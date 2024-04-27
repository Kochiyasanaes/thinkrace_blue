package com.xrs.bluetooth_device.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.telephony.CellIdentityCdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;

import java.util.List;

/**
 * 功能描述：通过手机信号获取基站信息
 * # 通过TelephonyManager 获取lac:mcc:mnc:cell-id
 * # MCC，Mobile Country Code，移动国家代码（中国的为460）；
 * # MNC，Mobile Network Code，移动网络号码（中国移动为0，中国联通为1，中国电信为2）；
 * # LAC，Location Area Code，位置区域码；
 * # CID，Cell Identity，基站编号；
 * # BSSS，Base station signal strength，基站信号强度。
 * @author android_ls
 */

public class GSMCellLocationUtils {

    private static final String TAG = "GSMCellLocation";

    public static String getParseBaseStation(Context context) {
        // 获取基站信息
        TelephonyManager mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        // 返回值MCC + MNC
        String mcc;
        String mnc;
        try {
            String operator = mTelephonyManager.getNetworkOperator();
            mcc = operator.substring(0, 3);
            mnc = operator.substring(3);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        // 中国移动和中国联通获取LAC、CID的方式
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return "";
        }
        int lac;
        int cellId;
        CellLocation cel = mTelephonyManager.getCellLocation();
        if(mTelephonyManager.getPhoneType()==TelephonyManager.PHONE_TYPE_CDMA) {//如果是电信卡的话
            CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) cel;
            cellId = cdmaCellLocation.getBaseStationId();
            lac = cdmaCellLocation.getNetworkId();
        }else{
            GsmCellLocation gsmCellLocation = (GsmCellLocation) cel;
            cellId = gsmCellLocation.getCid();
            lac = gsmCellLocation.getLac();
        }
        LogUtils.i("mare " + " MCC移动国家代码 = " + mcc + "\t MNC移动网络号码 = "
                + mnc + "\t LAC位置区域码 = " + lac + "\t CID基站编号 = " + cellId);


        // 获取邻区基站信息
        List<CellInfo> infos = mTelephonyManager.getAllCellInfo();
        StringBuffer sb = new StringBuffer("总数 : " + infos.size() + "\n");
        for (CellInfo info1 : infos) { // 根据邻区总数进行循环
            CellInfoCdma cellInfoCdma = (CellInfoCdma) info1;
            CellIdentityCdma cellIdentityCdma = cellInfoCdma.getCellIdentity();
            CellSignalStrengthCdma cellSignalStrengthCdma = cellInfoCdma.getCellSignalStrength();
            sb.append(" LAC : " + cellIdentityCdma.getNetworkId()); // 取出当前邻区的LAC
            sb.append(" CID : " + cellIdentityCdma.getBasestationId()); // 取出当前邻区的CID
            sb.append(" BSSS : " + (-113 + 2 * cellSignalStrengthCdma.getDbm()) + "\n"); // 获取邻区基站信号强度
        }
        LogUtils.i(TAG, " 获取邻区基站信息:" + sb.toString());

        return mcc + "," + Integer.valueOf(mnc) + "," + lac + "," + cellId;
    }
    /**
     * 获取手机信号强度，需添加权限 android.permission.ACCESS_COARSE_LOCATION <br>
     * API要求不低于17 <br>
     *
     * @return 当前手机主卡信号强度, 单位 dBm（-1是默认值，表示获取失败）
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static String getMobileDbm(Context context) {
        int dbm = -1;
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        List<CellInfo> cellInfoList;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (ActivityCompat.checkSelfPermission(Utils.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return "";
            }
            cellInfoList = tm.getAllCellInfo();
            if (null != cellInfoList) {
                for (CellInfo cellInfo : cellInfoList) {
                    if (cellInfo instanceof CellInfoGsm) {
                        CellSignalStrengthGsm cellSignalStrengthGsm = ((CellInfoGsm) cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthGsm.getDbm();
                    } else if (cellInfo instanceof CellInfoCdma) {
                        CellSignalStrengthCdma cellSignalStrengthCdma =
                                ((CellInfoCdma) cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthCdma.getDbm();
                    } else if (cellInfo instanceof CellInfoWcdma) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                            CellSignalStrengthWcdma cellSignalStrengthWcdma =
                                    ((CellInfoWcdma) cellInfo).getCellSignalStrength();
                            dbm = cellSignalStrengthWcdma.getDbm();
                        }
                    } else if (cellInfo instanceof CellInfoLte) {
                        CellSignalStrengthLte cellSignalStrengthLte = ((CellInfoLte) cellInfo).getCellSignalStrength();
                        dbm = cellSignalStrengthLte.getDbm();
                    }
                }
            }
        }
        String tempDbm = "";
        dbm = Math.abs(dbm);
        if(dbm < 10) {
            tempDbm = "00"+dbm;
        } else if(dbm < 100) {
            tempDbm = "0" + dbm;
        } else {
            tempDbm = String.valueOf(dbm);
        }
        return tempDbm;
    }
}
