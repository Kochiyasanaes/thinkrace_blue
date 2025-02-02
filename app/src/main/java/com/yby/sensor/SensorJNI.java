package com.yby.sensor;


import android.util.Log;

/**
 * @author wzb<wangzhibin_x@qq.com>
 * @date Jul 17, 2020 3:16:03 PM
 */
public class SensorJNI {
	static {
		System.loadLibrary("yby_sensor");
	}


	/**
	 * 获取温度函数
	 *
	 * @return int 温度值的100倍. 例:3944 39.44摄氏度
	 */
	public native static int getTemp();


	/**
	 * 获取计步数函数
	 *
	 * @return int 本次开机后的总计步数.(重新开机后会从0计数)
	 */
	public native static int getStep();

	/**
	 * 温度校准函数,让设备进入校准模式37度恒温单点校准.大约需要1分钟
	 *
	 * @return int 0,成功;1失败
	 */
	public native static int caliTemp();

	/**
	 * 心率开始测试
	 *
	 * @return
	 */
	public native static int hrStart();

	/**
	 * 心率停止测试
	 *
	 * @return
	 */
	public native static int hrStop();
/**
 * 获取心率值,由驱动主动上报uevent事件
 * 	UEventObserver m_ueObs=new UEventObserver(){
 *	public void onUEvent(UEvent event) {
 *		String hr=event.get("HR");
 *		Log.d("wzb","UEventObserver hr="+hr);
 *
 *        }
 *    };
 *
 *注册监听
 * m_ueObs.startObserving("DEVPATH=/devices/virtual/ic_detect_drv/hr");
 *
 * 注销监听
 * m_ueObs.stopObserving();
 */

	/**
	 * 血氧开始测试
	 *
	 * @return
	 */
	public native static int spoStart();

	/**
	 * 血氧停止测试
	 *
	 * @return
	 */
	public native static int spoStop();

	public native static int gsensorEnable();

	public native static int gsensorDisable();

	public native static int hrFactoryTest();
	public native static int getLcdID();
	public native static int getTPID();
}