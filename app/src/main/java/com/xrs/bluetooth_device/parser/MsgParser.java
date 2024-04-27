package com.xrs.bluetooth_device.parser;

import android.text.TextUtils;


import com.xrs.bluetooth_device.utils.LogUtils;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * TODO 解析收到的消息工具类
 */
public class MsgParser {

	/**
	 * 将header解析成字符串数组格式
	 *
	 * @param header IWBP00   也有可能是IWBPLN2018042002020
	 * @return String[] [IWBP00]数组
	 */
	public static String[] getRecvHeaderSegments(String header) {
		LogUtils.v("getRecvHeaderSegments " + header);
		if (TextUtils.isEmpty(header))
			return null;
		String[] segemnt = {header};
		if (header.length() > 6) {
			segemnt = new String[]{TextUtils.substring(header, 0, 6)};
		}
		printArray(segemnt);
		return segemnt;
	}

	private static void printArray(String[] segemnt) {
		for (String data : segemnt) {
			LogUtils.v("segment " + data);
		}
	}

	/**
	 * 从字符串消息中解析出消息类型
	 *
	 * @param header product*id*len*content
	 * @return msgType 消息类型
	 */
	public static String parseHeaderTypeByHeader(String header) {
		String[] segment = getRecvHeaderSegments(header);
		if (segment == null || segment.length == 0)
			return null;
		LogUtils.e("segment " + Arrays.toString(segment));
		String msgType = segment[segment.length - 1];
		LogUtils.e(msgType);
		return msgType;
	}

	/**
	 * TODO 从原生数组中解析出要查找的字符串第一次出现的下标索引
	 *
	 * @param sourceData [FISE*201700444400005*0002*LK]            IWBPT1#
	 * @return 返回第一次出现目标字节数组的索引
	 */
	public static int indexOfStrInBytes(byte[] sourceData, String str) {
		byte[] target = str.getBytes(Charset.defaultCharset());
		LogUtils.d("sourceData " + Arrays.toString(sourceData));
		LogUtils.d("target " + Arrays.toString(target));
		int positon = -1;
		boolean isFirstOccure = false;
		int searchEnd = -1;
		int sourceLen = sourceData.length;
		int targetLen = target.length;
		for (int i = 0; i < sourceLen; i++) {
			if (!isFirstOccure) {
				if (target[0] == sourceData[i]) {
					positon = i;
					LogUtils.d("indexOfStrInBytes start " + positon);
					if (targetLen == 1) {
						return positon;
					}
					searchEnd = positon + targetLen;
					if (searchEnd > sourceLen)
						return -1;//要查询的长度大于原数组长度
					isFirstOccure = true;
					continue;
				}
			}
			if (positon != -1) { //找到起始位置
				if (i < searchEnd) {
					if (sourceData[i] != target[i - positon]) {
						return -1;//没找到
					}
					if (i - positon == targetLen - 1) {//最后一位也匹配
						LogUtils.d("找到了.... ");
						return positon;//找到了
					}
				}
			}
		}
		LogUtils.d("indexOfStrInBytes end ");//找遍了也没找到
		return positon;
	}
}
