package com.xrs.bluetooth_device.data;

import android.content.Context;
import android.text.TextUtils;


import com.libsocket.constant.SPConstant;
import com.libsocket.constant.TcpConstants;
import com.xrs.bluetooth_device.utils.DeviceUtils;
import com.xrs.bluetooth_device.utils.LogUtils;
import com.xrs.bluetooth_device.utils.SPUtils;


public class GlobalSettings {

	/**
	 * 厂商
	 */
	protected final static String product = TcpConstants.PRODUCT;
	/**
	 * 设备ID
	 */
	public final static String MSG_CONTENT_SEPERATOR = ",";
	public final static String MSG_SUFFIX_ESCAPE = "#";

	public final static String HeartRateMax = "HeartRateMax";
	public final static String HeartRateMin = "HeartRateMin";
	public final static String SystolicMax = "SystolicMax";
	public final static String SystolicMin = "SystolicMin";
	public final static String DiastolicMax = "DiastolicMax";
	public final static String DiastolicMin = "DiastolicMin";

	public static int PORT = TcpConstants.PORT;
	private static final String DEFAULT_IP = TcpConstants.IP;
	private final static String DOMAIN_NAME = TcpConstants.DOMAIN;
	private final static String TEST_DOMAIN_NAME = TcpConstants.TEST_DOMAIN;
	private static String IP = "";//初始化为空
	private String imei = "";
	private String imsi = "";

	private GlobalSettings() {
/*		LogUtils.d("getProduct " + product);
		LogUtils.d("isSmartProtocol " + TcpConstants.isSmartProtocol);
		LogUtils.d("DOMAIN_NAME " + TcpConstants.DOMAIN);
		LogUtils.d("TEST_DOMAIN " + TcpConstants.TEST_DOMAIN);*/
	}

	private static class SingletonHolder {
		private static final GlobalSettings INSTANCE = new GlobalSettings();
	}

	public static GlobalSettings instance() {
		return SingletonHolder.INSTANCE;
	}

	public String getProduct() {
		return product;
	}

	public static String getDefaultIp() {
		return DEFAULT_IP;
	}

	public static String getDomainName() {
		return DOMAIN_NAME;
	}

	public static String getTestDomainName() {
		return TEST_DOMAIN_NAME;
	}

	public String getIP() {
		if (!TextUtils.isEmpty(GlobalSettings.IP)) return GlobalSettings.IP;
		return SPUtils.getInstance().getString(SPConstant.CURRENT_IP);
	}

	public void setIP(String ip) {
		if (TextUtils.isEmpty(ip)) {
			return;
		}
		GlobalSettings.IP = ip;//先保存到内存中
	}

	public void setPort(int port) {
		if (port <= 0) {
			return;
		}
		GlobalSettings.PORT = port;
		SPUtils.getInstance().put(SPConstant.CURRENT_PORT,port);
	}

	public int getPort() {
		if (GlobalSettings.PORT > 0) return GlobalSettings.PORT;
		return SPUtils.getInstance().getInt(SPConstant.CURRENT_PORT);
	}

	public void saveImei(Context ctx) {
		String id = DeviceUtils.getIccid(ctx);
		LogUtils.i("id = " + id);
		String imei = DeviceUtils.getIMEI(ctx);
		LogUtils.i("imei = " + imei);
		GlobalSettings.instance().setImei(imei);
        /*QrUpdateEvent event = new QrUpdateEvent(QrUpdateEvent.Type.UPDATE_QR, imei); //二维码
        EventBus.getDefault().post(event);*/
	}

	public void saveImsi(Context ctx) {
		String imsi = DeviceUtils.getSubscriberId(ctx);
		LogUtils.i("imsi = " + imsi);
		GlobalSettings.instance().setImsi(imei);
	}

	public String getImei() {
		return imei;//就保存在内存中
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getImsi() {
		return imsi;//就保存在内存中
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}
}
