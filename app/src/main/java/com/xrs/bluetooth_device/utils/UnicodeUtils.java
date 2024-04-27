package com.xrs.bluetooth_device.utils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;


/**
 * @author mare
 * @Description:TODO unicode和utf-8互转工具类
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/9/18
 * @time 20:24
 */
public class UnicodeUtils {
    /**
     * 将utf-8的汉字转换成unicode格式汉字码
     *
     * @param string
     * @return
     */
    public static String stringToUnicode(String string) {
        if (TextUtils.isEmpty(string)) {
            return null;
        }
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            unicode.append("\\u" + Integer.toHexString(c));
        }
        String str = unicode.toString();

        return str.replaceAll("\\\\", "0x");
    }

    /**
     * 将utf-8的汉字转换成unicode格式并且不含有0xu的汉字码
     *
     * @param string
     * @return
     */
    public static String stringToUnicodeNo0xu(String string) {
        if (TextUtils.isEmpty(string)) {
            return null;
        }
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            unicode.append(Integer.toHexString(c));
        }
        String str = unicode.toString();

        return str;
    }

    /**
     * 将unicode并且不含有0xu的汉字码转换成utf-8格式的汉字
     *
     * @param unicode
     * @return
     */
    public static String unicodeToString(String unicode) {
        if (TextUtils.isEmpty(unicode)) {
            return null;
        }
        String str = unicode.replace("0x", "\\");

        StringBuffer string = new StringBuffer();
        String[] hex = str.split("\\\\u");
        for (int i = 1; i < hex.length; i++) {
            int data = Integer.parseInt(hex[i], 16);
            string.append((char) data);
        }
        return string.toString();
    }

    /**
     * TODO 将unicode的汉字码转换成utf-8格式的汉字
     *
     * @param unicode
     * @return
     */
    public static String unicodeNo0xuToString(String unicode) {
        if (TextUtils.isEmpty(unicode)) {
            return null;
        }
        StringBuffer string = new StringBuffer();
        int len = unicode.length();
        int arrayLen = len / 4;
        String hex;
        int data;
        for (int i = 0; i < arrayLen; i++) {
            hex = unicode.substring(i * 4, (i + 1) * 4);
            data = Integer.parseInt(hex, 16);
            string.append((char) data);
        }
        return string.toString();
    }

    /**
     *
     * @param string 将GB2312编码转为字符串
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String GB2312Decode( String string){
        byte[] bytes = new byte[string.length() / 2];
        for(int i = 0; i < bytes.length; i ++){
            byte high = Byte.parseByte(string.substring(i * 2, i * 2 + 1), 16);
            byte low = Byte.parseByte(string.substring(i * 2 + 1, i * 2 + 2), 16);
            bytes[i] = (byte) (high << 4 | low);
        }
        try {
            return new String(bytes, "GB2312");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }
    }


}
