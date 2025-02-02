package com.xrs.bluetooth_device.utils;

import android.app.AlarmManager;
import android.content.Context;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeUtil {
    private static long HOURS12 = 0L;

    private static long HOURS24 = 0L;
    public static String default_device_date_str = "20230101000000";
    public static Date default_device_date = parseDatetiime(default_device_date_str, TimeZone.getDefault());



    public static boolean isInSleepRange;

    public static String sGpsDate = "220101";

    public static String sGpsTime = "000000";

    static {
        HOURS12 = 43200000L;
        HOURS24 = 86400000L;
        isInSleepRange = false;
    }

    public static void check_and_set_default_time(Context paramContext) {
        if (System.currentTimeMillis() / 60000L < default_device_date.getTime() / 60000L) {
            setSysDefaultDateTime(paramContext, default_device_date);
            LogUtils.d("check_and_set_default_time set DefaultTime");
        } else {
            LogUtils.d("check_and_set_default_time time is currect , skip DefaultTime");
        }
    }

    public static Date convertTimezone(Date paramDate, TimeZone paramTimeZone) {
        Calendar calendar = Calendar.getInstance();
        long l = paramDate.getTime();
        calendar.setTimeZone(paramTimeZone);
        calendar.setTimeInMillis(l);
        int i = calendar.get(15);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sourceZoneOffset ==> ");
        stringBuilder.append(i);
        LogUtils.d(stringBuilder.toString());
        l += i;
        stringBuilder = new StringBuilder();
        stringBuilder.append("targetTime ==> ");
        stringBuilder.append(l);
        LogUtils.d(stringBuilder.toString());
        return new Date(l);
    }

    public static long diffBetween2DateInMinutes(String paramString1, String paramString2) {
        long l = parseDatetiime(paramString1, TimeZone.getDefault()).getTime();
        return (parseDatetiime(paramString2, TimeZone.getDefault()).getTime() - l) / 60000L;
    }

    private static void doTimeSet(Context paramContext, Date datetime, int timezone, boolean paramBoolean) {
        AlarmManager alarmManager = (AlarmManager) paramContext.getSystemService(Context.ALARM_SERVICE);
        String timezone_name = "";
        if (timezone > 0) {
            timezone_name = String.format("GMT+%1$02d:00", Math.abs(timezone));
        } else {
            timezone_name = String.format("GMT-%1$02d:00", Math.abs(timezone));
        }
        LogUtils.d("dataTime ==> " + datetime.getTime());
        LogUtils.d("timezone_name ==> " + timezone_name);
        Date date = convertTimezone(datetime, TimeZone.getTimeZone(timezone_name));
        long time = date.getTime();
        LogUtils.d("time ==> " + time);
        alarmManager.setTimeZone(getOlsonTimeZoneIdByGmtOffset(timezone_name));
        alarmManager.setTime(time);
    }

    public static String getOlsonTimeZoneIdByGmtOffset(String gmtOffset) {
        // 检查GMT偏移字符串的格式，确保它以"GMT"开头
        if (!gmtOffset.startsWith("GMT")) {
            throw new IllegalArgumentException("Invalid GMT offset format");
        }

        // 提取偏移量，并分割成小时和分钟
        String offsetPart = gmtOffset.substring(3);
        String[] offsetParts = offsetPart.split(":");
        int offsetHours = Integer.parseInt(offsetParts[0]);
        int offsetMinutes = offsetParts.length > 1 ? Integer.parseInt(offsetParts[1]) : 0;

        // 计算总偏移量（以毫秒为单位）
        int totalOffsetMillis = (offsetHours * 60 + offsetMinutes) * 60 * 1000;

        // 获取偏移量的符号
        String sign = gmtOffset.startsWith("GMT+") ? "+" : "-";

        // 遍历所有可用的时区ID，找到与偏移量匹配的时区
        for (String id : TimeZone.getAvailableIDs(totalOffsetMillis)) {
            TimeZone tz = TimeZone.getTimeZone(id);
            int tzOffsetMillis = tz.getRawOffset();
            if (tzOffsetMillis == totalOffsetMillis) {
                return id;
            }
        }

        // 如果没有找到匹配的时区，返回原始的GMT偏移量字符串
        // 这可能不会是一个有效的Olson时区ID，但作为回退选项
        return gmtOffset.substring(3, gmtOffset.length() - 3).replace(":", "");
    }

    public static String getCurrentSleepRecordDate() {
        return getCurrentSleepRecordDate(System.currentTimeMillis());
    }

    public static String getCurrentSleepRecordDate(long paramLong) {
        String str = getSystemTime4(paramLong);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str);
            stringBuilder.append("210000");
            Date date = simpleDateFormat.parse(stringBuilder.toString());

            if (date != null) {
                long l1 = date.getTime();
                long l2 = HOURS12;
                long l3 = l1 - HOURS24;
                long l4 = l2 + l3;
                if (paramLong > l1 && paramLong < l1 + l2) {
                    isInSleepRange = true;
                    return getSystemTime4(l1);
                }
                if (paramLong > l3 && paramLong < l4) {
                    isInSleepRange = true;
                    return getSystemTime4(l3);
                }
                if (paramLong > l4 && paramLong < l1) {
                    isInSleepRange = false;
                    return getSystemTime4(l3);
                }
            }
        } catch (ParseException parseException) {
            parseException.printStackTrace();
            parseException = null;
        }

        return null;
    }

    public static String getCurrentSleepRecordDate(String paramString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        try {
            Date date = simpleDateFormat.parse(paramString);
            return getCurrentSleepRecordDate(date.getTime());
        } catch (ParseException parseException) {
            parseException.printStackTrace();
            return null;
        }
    }

    public static String getDefaultTimeZone() {
        return TimeZone.getDefault().getDisplayName();
    }

    public static String getLocalTime() {
        return getLocalTime(System.currentTimeMillis());
    }

    public static String getLocalTime(long paramLong) {
        return (new SimpleDateFormat("yyyy/MM/dd,HH:mm:ss")).format(new Date(paramLong));
    }

    public static String getLocalTime2() {
        return getLocalTime2(System.currentTimeMillis());
    }

    public static String getLocalTime2(long paramLong) {
        return (new SimpleDateFormat("MMddHHmmss")).format(new Date(paramLong));
    }

    public static String getLocalTimeForGoogle(long paramLong) {
        return (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date(paramLong));
    }

    public static String getShortDate() {
        return (new SimpleDateFormat("yyMMdd")).format(Calendar.getInstance().getTime());
    }

    public static String getShortTime() {
        return (new SimpleDateFormat("HHmmss")).format(Calendar.getInstance().getTime());
    }

    public static String getSystemDateYYMMDD() {
        return (new SimpleDateFormat("yyMMdd")).format(new Date(System.currentTimeMillis()));
    }

    public static String getSystemTime() {
        return getSystemTime(System.currentTimeMillis());
    }

    public static String getSystemTime(long paramLong) {
        return (new SimpleDateFormat("yyyy-MM-dd;HH:mm:ss")).format(new Date(paramLong));
    }

    public static String getSystemTime2() {
        return getSystemTime2(System.currentTimeMillis());
    }

    public static String getSystemTime2(long paramLong) {
        return (new SimpleDateFormat("yyyyMMdd;HHmmss")).format(new Date(paramLong));
    }

    public static String getSystemTime3() {
        return getSystemTime3(System.currentTimeMillis());
    }

    public static String getSystemTime3(long paramLong) {
        return (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date(paramLong));
    }

    public static String getSystemTime4() {
        return getSystemTime4(System.currentTimeMillis());
    }

    public static String getSystemTime4(long paramLong) {
        return (new SimpleDateFormat("yyyyMMdd")).format(new Date(paramLong));
    }

    public static String getSystemTimeForCameraMark() {
        return (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date(System.currentTimeMillis()));
    }

    public static String getSystemTimeHHmmss() {
        return (new SimpleDateFormat("HHmmss")).format(new Date(System.currentTimeMillis()));
    }

    public static String getSystemTimeYYMMDDHHMMSS() {
        return (new SimpleDateFormat("yyMMddHHmmss")).format(new Date(System.currentTimeMillis()));
    }

    public static String getSystemTimeyyyyMMddHHmmss() {
        return getSystemTime3(System.currentTimeMillis());
    }

    static String getTimeZoneID_ByDisplayName(String paramString) {
        String str = TimeZone.getDefault().getID();
        if (paramString.equals("GMT+03:30")) {
            str = "Asia/Tehran";
        } else if (paramString.equals("GMT+04:30")) {
            str = "Asia/Kabul";
        } else if (paramString.equals("GMT+05:30")) {
            str = "Asia/Colombo";
        } else if (paramString.equals("GMT+06:30")) {
            str = "Asia/Rangoon";
        } else if (paramString.equals("GMT+09:30")) {
            str = "Australia/Darwin";
        } else if (paramString.equals("GMT+09:00")) {
            str = "Asia/Tokyo";
        }
        return str;
    }

    public static String getUTCSystemDateYYMMDD() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMdd");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        return (System.currentTimeMillis() < 1640966400000L) ? sGpsDate : simpleDateFormat.format(new Date());
    }

    public static String getUTCSystemTimeHHmmss() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HHmmss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        if (System.currentTimeMillis() < 1640966400000L)
            return sGpsTime;
        String str = simpleDateFormat.format(new Date());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("getUTCSystemTimeHHmmss ==>   ");
        stringBuilder.append(str);
        LogUtils.d(stringBuilder.toString());
        return str;
    }

    public static String getUTCTimeForCameraMark() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        return simpleDateFormat.format(new Date());
    }

    public static String getUTCTimestampForCameraMarkRe() {
        // 获取当前时间的毫秒数（时间戳）
        long timestamp = System.currentTimeMillis();

        // 将时间戳转换为UTC时间的ISO 8601格式的字符串
        // 注意：这里我们不需要SimpleDateFormat，因为timestamp已经是UTC时间戳
        return String.valueOf(timestamp);
    }

    public static String getWeek() {
        String str1 = getSystemTime4();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(simpleDateFormat.parse(str1));
        } catch (ParseException parseException) {
            parseException.printStackTrace();
            LogUtils.e(parseException.getMessage(), parseException);
        }
        int i = calendar.get(7);
        String str2 = "";
        if (i == 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("");
            stringBuilder.append("周天");
            str2 = stringBuilder.toString();
        }
        str1 = str2;
        if (calendar.get(7) == 2) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str2);
            stringBuilder.append("周一");
            str1 = stringBuilder.toString();
        }
        str2 = str1;
        if (calendar.get(7) == 3) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str1);
            stringBuilder.append("周二");
            str2 = stringBuilder.toString();
        }
        str1 = str2;
        if (calendar.get(7) == 4) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str2);
            stringBuilder.append("周三");
            str1 = stringBuilder.toString();
        }
        str2 = str1;
        if (calendar.get(7) == 5) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str1);
            stringBuilder.append("周四");
            str2 = stringBuilder.toString();
        }
        str1 = str2;
        if (calendar.get(7) == 6) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str2);
            stringBuilder.append("周五");
            str1 = stringBuilder.toString();
        }
        str2 = str1;
        if (calendar.get(7) == 7) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(str1);
            stringBuilder.append("周六");
            str2 = stringBuilder.toString();
        }
        return str2;
    }

    public static boolean is24Hour(Context paramContext) {
        return DateFormat.is24HourFormat(paramContext);
    }

    public static boolean isDateTimeAuto(Context paramContext) {
        boolean bool = false;
        try {
            int i = Settings.Global.getInt(paramContext.getContentResolver(), "auto_time");
            if (i > 0)
                bool = true;
            return bool;
        } catch (Settings.SettingNotFoundException settingNotFoundException) {
            settingNotFoundException.printStackTrace();
            return false;
        }
    }

    public static boolean isTimeZoneAuto(Context paramContext) {
        boolean bool = false;
        try {
            int i = Settings.Global.getInt(paramContext.getContentResolver(), "auto_time_zone");
            if (i > 0)
                bool = true;
            return bool;
        } catch (Settings.SettingNotFoundException settingNotFoundException) {
            settingNotFoundException.printStackTrace();
            return false;
        }
    }

    public static Date parseDatetiime(String paramString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
        try {
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("parseDatetiime ");
            stringBuilder.append(paramString);
            LogUtils.i(stringBuilder.toString());
            return simpleDateFormat.parse(paramString);
        } catch (ParseException parseException) {
            parseException.printStackTrace();
            LogUtils.e(parseException.getMessage(), parseException);
            return null;
        }
    }

    public static Date parseDatetiime(String paramString1, String paramString2) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            StringBuilder stringBuilder2 = new StringBuilder();

            stringBuilder2.append(paramString1);
            stringBuilder2.append(" ");
            stringBuilder2.append(paramString2);
            paramString1 = stringBuilder2.toString();
            StringBuilder stringBuilder1 = new StringBuilder();

            stringBuilder1.append("parseDatetiime ");
            stringBuilder1.append(paramString1);
            LogUtils.i(stringBuilder1.toString());
            return simpleDateFormat.parse(paramString1);
        } catch (ParseException parseException) {
            parseException.printStackTrace();
            LogUtils.e(parseException.getMessage(), parseException);
            return null;
        }
    }

    public static Date parseDatetiime(String paramString, TimeZone paramTimeZone) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        simpleDateFormat.setTimeZone(paramTimeZone);
        try {
            String str2 = paramTimeZone.getID();
            StringBuilder stringBuilder2 = new StringBuilder();

            stringBuilder2.append("format id ==> ");
            stringBuilder2.append(str2);
            LogUtils.d(stringBuilder2.toString());
            Date date = simpleDateFormat.parse(paramString);
            StringBuilder stringBuilder3 = new StringBuilder();

            stringBuilder3.append(paramString);
            stringBuilder3.append(" date1 ==> ");
            stringBuilder3.append(date.getTime());
            LogUtils.d(stringBuilder3.toString());
            String str1 = simpleDateFormat.format(Long.valueOf(date.getTime()));
            StringBuilder stringBuilder1 = new StringBuilder();

            stringBuilder1.append("format ==> ");
            stringBuilder1.append(str1);
            LogUtils.d(stringBuilder1.toString());
            return date;
        } catch (ParseException parseException) {
            parseException.printStackTrace();
            Log.d("test", parseException.getMessage(), parseException);
            return null;
        }
    }

    public static void set24Hour(Context paramContext, boolean paramBoolean) {
        if (paramBoolean) {
            Settings.System.putString(paramContext.getContentResolver(), "time_12_24", "24");
        } else {
            Settings.System.putString(paramContext.getContentResolver(), "time_12_24", "12");
        }
    }

    public static void setAutoDateTime(Context paramContext, int paramInt) {
        Settings.Global.putInt(paramContext.getContentResolver(), "auto_time", paramInt);
    }

    public static void setAutoTimeZone(Context paramContext, int paramInt) {
        Settings.Global.putInt(paramContext.getContentResolver(), "auto_time_zone", paramInt);
    }

    public static void setSysDateTime(Context paramContext, Date paramDate, int paramInt) {
        setSysDateTime(paramContext, paramDate, paramInt, true);
    }

    public static void setSysDateTime(Context mContext, Date datetime, int timezone, boolean addtimezoneToTime) {
        //Log.d(TAG, "set tm="+when + ", now tm="+now);
        LogUtils.d("timezone ==> " + timezone);
        Calendar d = Calendar.getInstance();
        LogUtils.d("====before " + d.getTime().toString() + " time zone " + d.getTimeZone());
        doTimeSet(mContext, datetime, timezone, addtimezoneToTime);
        Calendar c = Calendar.getInstance();
        LogUtils.d("====aftert " + c.getTime().toString() + " time zone " + c.getTimeZone());
        //当前系统时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long l = System.currentTimeMillis();
        LogUtils.d("current time ==> " + l);
        TimeZone timeZone = simpleDateFormat.getTimeZone();
        LogUtils.d("timeZone after ==> " + timeZone.getID());
        String format = simpleDateFormat.format(l);
        LogUtils.d("format == " + format);
        String utcSystemTimeHHmmss = getUTCSystemTimeHHmmss();
        LogUtils.d("utcSystemTimeHHmmss ==> " + utcSystemTimeHHmmss);
    }

    public static void setSysDateTime(Context paramContext, Date paramDate, String paramString, boolean paramBoolean) {
        String str;
        AlarmManager alarmManager = (AlarmManager)paramContext.getSystemService(Context.ALARM_SERVICE);
        Locale.getDefault().getDisplayCountry();
        boolean bool = paramString.startsWith("e");
        int i = 0;
        if (bool || paramString.startsWith("E")) {
            str = String.format("GMT+%1$s:%2$s", new Object[] { paramString.substring(1, 3), paramString.substring(3, 5) });
        } else {
            str = String.format("GMT-%1$s:%2$s", new Object[] { paramString.substring(1, 3), paramString.substring(3, 5) });
        }
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append("====set timezone ");
        stringBuilder1.append(str);
        LogUtils.i(stringBuilder1.toString());
        TimeZone timeZone = TimeZone.getTimeZone(str);
        stringBuilder1 = new StringBuilder();
        stringBuilder1.append("====set timezone ");
        stringBuilder1.append(timeZone.getDisplayName());
        LogUtils.i(stringBuilder1.toString());
        Calendar calendar = Calendar.getInstance();
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("====origin time ");
        stringBuilder2.append(calendar.getTime().toString());
        stringBuilder2.append(" time zone ");
        stringBuilder2.append(calendar.getTimeZone());
        LogUtils.i(stringBuilder2.toString());
        calendar.setTime(paramDate);
        try {
            int j = Integer.parseInt(paramString.substring(1, 3));
            try {
                int k = Integer.parseInt(paramString.substring(3, 5));
                i = k;
            } catch (Exception E) {}
        } catch (Exception exception) {
            boolean bool1 = false;
            LogUtils.e(exception.getMessage(), exception);
        }

    }

    public static void setSysDefaultDateTime(Context paramContext, Date paramDate) {
        AlarmManager alarmManager = (AlarmManager)paramContext.getSystemService(Context.ALARM_SERVICE);
        Locale.getDefault().getDisplayCountry();
        TimeZone timeZone = TimeZone.getDefault();
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("====set timezone ");
        stringBuilder2.append(timeZone.getDisplayName());
        LogUtils.i(stringBuilder2.toString());
        Calendar calendar2 = Calendar.getInstance();
        stringBuilder2 = new StringBuilder();
        stringBuilder2.append("====origin time ");
        stringBuilder2.append(calendar2.getTime().toString());
        stringBuilder2.append(" time zone ");
        stringBuilder2.append(calendar2.getTimeZone());
        LogUtils.i(stringBuilder2.toString());
        calendar2.setTime(paramDate);
        calendar2.setTimeZone(timeZone);
        long l1 = calendar2.getTimeInMillis();
        if (l1 / 1000L < 2147483647L)
            alarmManager.setTime(l1);
        Calendar calendar1 = Calendar.getInstance();
        long l2 = calendar1.getTimeInMillis();
        if (l2 - l1 > 1000L) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("====set time failed now(");
            stringBuilder.append(l2);
            stringBuilder.append(")-when(");
            stringBuilder.append(l1);
            stringBuilder.append(") > 1000 !");
            LogUtils.i(stringBuilder.toString());
        }
        StringBuilder stringBuilder1 = new StringBuilder();
        stringBuilder1.append("====set time to ");
        stringBuilder1.append(calendar1.getTime().toString());
        stringBuilder1.append(" time zone ");
        stringBuilder1.append(calendar1.getTimeZone());
        LogUtils.i(stringBuilder1.toString());
    }

    public static void setSysTime(Context paramContext, int paramInt1, int paramInt2, int paramInt3) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(11, paramInt1);
        calendar.set(12, paramInt2);
        calendar.set(13, paramInt3);
        calendar.set(14, 0);
        long l = calendar.getTimeInMillis();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("====set time ");
        stringBuilder.append(calendar.toString());
        LogUtils.i(stringBuilder.toString());
        if (l / 1000L < 2147483647L)
            ((AlarmManager)paramContext.getSystemService(Context.ALARM_SERVICE)).setTime(l);
    }

    public static void setTimeZone(int paramInt) {
        String str;
        Calendar calendar = Calendar.getInstance();
        if (paramInt > 0) {
            str = String.format("GMT+%1$02d:00", new Object[] { Integer.valueOf(Math.abs(paramInt)) });
        } else {
            str = String.format("GMT-%1$02d:00", new Object[] { Integer.valueOf(Math.abs(paramInt)) });
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("====set timezone ");
        stringBuilder.append(str);
        LogUtils.i(stringBuilder.toString());
        TimeZone timeZone = TimeZone.getTimeZone(str);
        calendar.setTimeZone(timeZone);
        TimeZone.setDefault(timeZone);
    }

    public static void setTimeZone(Context paramContext, int paramInt) {
        String str;
        Calendar.getInstance();
        if (paramInt > 0) {
            str = String.format("GMT+%1$02d:00", new Object[] { Integer.valueOf(Math.abs(paramInt)) });
        } else {
            str = String.format("GMT-%1$02d:00", new Object[] { Integer.valueOf(Math.abs(paramInt)) });
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("====set timezone ");
        stringBuilder.append(str);
        LogUtils.i(stringBuilder.toString());
        updateTimeZone(paramContext, str);
    }

    public static void setTimeZone(Context paramContext, String paramString) {
        if (paramString.startsWith("e") || paramString.startsWith("E")) {
            paramString = String.format("GMT+%1$s:%2$s", new Object[] { paramString.substring(1, 3), paramString.substring(3, 5) });
        } else {
            paramString = String.format("GMT-%1$s:%2$s", new Object[] { paramString.substring(1, 3), paramString.substring(3, 5) });
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("====set timezone ");
        stringBuilder.append(paramString);
        LogUtils.i(stringBuilder.toString());
        updateTimeZone(paramContext, paramString);
    }

    public static void setTimeZone(String paramString) {
        Calendar.getInstance().setTimeZone(TimeZone.getTimeZone(paramString));
    }

    public static void testUTCSystemtime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long l1 = System.currentTimeMillis();
        long l2 = TimeZone.getDefault().getRawOffset();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("UTC currentTime = ");
        stringBuilder.append(simpleDateFormat.format(new Date(l1 - l2)));
        LogUtils.i(stringBuilder.toString());
        l1 = Calendar.getInstance().getTimeInMillis();
        stringBuilder = new StringBuilder();
        stringBuilder.append("currentTime = ");
        stringBuilder.append(simpleDateFormat.format(new Date(l1)));
        LogUtils.i(stringBuilder.toString());
        (new Date()).getTime();
    }

    static void updateTimeZone(Context paramContext, String paramString) {
        StringBuilder stringBuilder1;
        String str;
        AlarmManager alarmManager = (AlarmManager)paramContext.getSystemService(Context.ALARM_SERVICE);
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("updateTimeZone:");
        stringBuilder2.append(paramString);
        LogUtils.d(stringBuilder2.toString());
        if (paramString == null) {
            LogUtils.d("updateTimeZone name is null");
            return;
        }
        String[] arrayOfString = TimeZone.getAvailableIDs();
        TimeZone timeZone = TimeZone.getDefault();
        for (byte b = 0; b < arrayOfString.length; b++) {
            String str1 = arrayOfString[b];
            TimeZone timeZone1 = TimeZone.getTimeZone(str1);
            timeZone1.getID();
            if (paramString.equals(timeZone1.getDisplayName(false, 0)) && timeZone1.getID().contains("Etc/GMT")) {
                paramString = TimeZone.getTimeZone(str1).getDisplayName();
                str = TimeZone.getTimeZone(str1).getID();
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("I have find timeZoneDisplayName:");
                stringBuilder.append(paramString);
                stringBuilder.append("  ID:");
                stringBuilder.append(str);
                paramString = stringBuilder.toString();
                stringBuilder = new StringBuilder();
                stringBuilder.append("===tmp:");
                stringBuilder.append(paramString);
                LogUtils.d(stringBuilder.toString());
                stringBuilder1 = new StringBuilder();
                stringBuilder1.append("setTimeZone:");
                stringBuilder1.append(timeZone1.getDisplayName());
                LogUtils.d(stringBuilder1.toString());
                alarmManager.setTimeZone(timeZone1.getID());
                return;
            }

        }
        stringBuilder2 = new StringBuilder();
        stringBuilder2.append(paramString);
        stringBuilder2.append(" not found , set to default timezone ");
        stringBuilder2.append(timeZone.getID());
        LogUtils.d(stringBuilder2.toString());
        alarmManager.setTimeZone(getTimeZoneID_ByDisplayName(paramString));
    }


}

