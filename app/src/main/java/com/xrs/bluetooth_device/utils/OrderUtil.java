package com.xrs.bluetooth_device.utils;
/*
 *  @项目名：  RootStartAuto
 *  @包名：    com.thinkrace.orderlibrary
 *  @文件名:   OrderUtil.this
 *  @创建者:   win10
 *  @创建时间:  2017/7/20 16:30
 *  @描述：    TODO
 */


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;


import com.libsocket.constant.SPConstant;
import com.libsocket.constant.TcpConstants;
import com.libsocket.sdk.ConnectionInfo;
import com.libsocket.sdk.OkSocket;
import com.libsocket.sdk.OkSocketOptions;
import com.libsocket.sdk.SocketActionAdapter;
import com.libsocket.sdk.bean.IPulseSendable;
import com.libsocket.sdk.bean.ISendable;
import com.libsocket.sdk.bean.OriginalData;
import com.libsocket.sdk.connection.IConnectionManager;
import com.libsocket.sdk.protocol.IWNormalHeaderProtocol;
import com.libsocket.utils.SL;
import com.xrs.bluetooth_device.data.GlobalSettings;
import com.xrs.bluetooth_device.data.HandShake;
import com.xrs.bluetooth_device.data.MsgDataBean;
import com.xrs.bluetooth_device.data.MsgType;
import com.xrs.bluetooth_device.data.PulseBean;
import com.xrs.bluetooth_device.data.RedirectException;
import com.xrs.bluetooth_device.data.TcpMsg;
import com.xrs.bluetooth_device.function.AlarmTimer;
import com.xrs.bluetooth_device.parser.MsgRecService;
import com.xrs.bluetooth_device.receiver.CommonAlarmReceiver;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class OrderUtil {

    private static OrderUtil instance;
    private String domainName = TcpConstants.DOMAIN;
    private String ip = TcpConstants.IP;
    private int port = TcpConstants.PORT;
    private Context context;
    private SharedPreferences sp;
    private IConnectionManager mManager;
    private String TAG = "socket";
    private String MQTT_TAG = "MQTT_TEST";
    
  
 
 

    private SocketActionAdapter adapter = new SocketActionAdapter() {
        //连接成功
        @Override
        public void onSocketConnectionSuccess(final Context context, final ConnectionInfo info, String action) {
            SL.e("已经连上服务器O(∩_∩)O~\"" + "       对方IP地址: " + info.getIp() + "  " + info.getPort());
            new Thread(new Runnable() {
                @Override
                public void run() {
                    SystemClock.sleep(2000);
                    if (mManager == null){
                        Log.e("thread","崩溃拉");
                    }
                    mManager.send(new HandShake()); //握手-登录包
                    try {
                        mManager.getPulseManager().setPulseSendable(new PulseBean()); //心跳包(亮屏才有效？)
                    }catch (Exception e){
                        Log.e("OrderUtils : ",e.toString());
                    }
                }
            }).start();

        }

        @Override
        public void onSocketDisconnection(Context context, ConnectionInfo info, String action, Exception e) {
            if (e != null) {
                if (e instanceof RedirectException) {
                    SL.e("正在重定向连接...");
                    mManager.switchConnectionInfo(((RedirectException) e).redirectInfo);
                    mManager.connect();
                } else {
                    SL.e("异常断开:" + e.getMessage());
                    startSocket();
                }
            } else {
                SL.e("正常断开");
            }
        }

        //连接失败
        @Override
        public void onSocketConnectionFailed(Context context, ConnectionInfo info, String action, Exception e) {
            SL.e(info.getIp() + "  " + info.getPort() + "连接失败");
        }

        //接收成功
        @Override
        public void onSocketReadResponse(Context context, ConnectionInfo info, String action, OriginalData data) {
            super.onSocketReadResponse(context, info, action, data);
            String str = new String(data.getBodyBytes(), Charset.forName("utf-8"));
            TcpMsg tcpMsg = new TcpMsg();
            tcpMsg.setSourceDataBytes(data.getBodyBytes());
            tcpMsg.setSourceDataString(str);
            tcpMsg.contentStr = str;
            if (tcpMsg.contentStr.trim().length() > 0) {
                SL.e("接收到数据=" + tcpMsg.contentStr);
                if (tcpMsg.contentStr.contains(MsgType.IWBP03)) {
                    mManager.getPulseManager().feed();
                    SL.e("收到心跳,喂狗成功"+str);
                }else if(tcpMsg.contentStr.contains(MsgType.IWBPLN)){
                    //暂停获取云信账号和密码
//                    kqAccount(GlobalSettings.instance().getImei());
                    //定位一下获取最新位置信息
                   /* AMapLocationManager.instance().start();
                    AlarmTimer.startConfirmedFrequencyUpload(KApplication.sContext);//开启固定频率上传(定位信息)*/
                }else if(tcpMsg.contentStr.contains(MsgType.IWBPVA)){
                    //暂停获取云信音视频列表
//                    getChatList(GlobalSettings.instance().getImei());
                }
                MsgRecService.instance().handleRecvMsg(Utils.getContext(), tcpMsg);
            } else {
                Log.i(TAG, "没有数据返回不更新");
            }
        }

        //发送成功
        @Override
        public void onSocketWriteResponse(Context context, ConnectionInfo info, String action, ISendable data) {
            super.onSocketWriteResponse(context, info, action, data);
            String str = new String(data.parse(), Charset.forName("utf-8"));
            /*str += BluetoothAdapter.getDefaultAdapter().getAddress();*/
            if (str.contains(MsgType.IWAPLN)) {
                mManager.getPulseManager().pulse();

                SL.e("发送握手数据(Handshake Sending):" + str);
            }else {
                SL.e("发送数据成功:" + str);
            }
        }

        @Override
        public void onPulseSend(Context context, ConnectionInfo info, IPulseSendable data) {
            super.onPulseSend(context, info, data);
            String str = new String(data.parse(), Charset.forName("utf-8"));
            SL.e("心跳发送" + str);
        }
    };

    public static String getMacAddress() {
        try {
            // 把当前机器上访问网络的接口存入 List集合中
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (!"wlan0".equalsIgnoreCase(nif.getName())) {
                    continue;
                }
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null || macBytes.length == 0) {
                    continue;
                }
                StringBuilder result = new StringBuilder();
                for (byte b : macBytes) {
                    //每隔两个字符加一个:
                    result.append(String.format("%02X:", b));
                }
                if (result.length() > 0) {
                    //删除最后一个:
                    result.deleteCharAt(result.length() - 1);
                }
                return result.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private OrderUtil() {
        context = OkSocket.getContext();
        sp = context.getSharedPreferences(SPConstant.CURRENT_USR_NAME, Context.MODE_PRIVATE);
    }

    public synchronized static OrderUtil getInstance() {
        if (instance == null) {
            instance = new OrderUtil();
        }
        return instance;
    }

    /**
     * 视频、语聊获取账号密码
     * 2：网易
     * @param imei
     */
    public void kqAccount(String imei) {
        String s = "";
        s = MsgType.IWAPVA + GlobalSettings.MSG_CONTENT_SEPERATOR + imei + GlobalSettings.MSG_CONTENT_SEPERATOR + "2" + GlobalSettings.MSG_SUFFIX_ESCAPE;
        sendMsg(s);
    }

    /**
     * 获取语音、视频聊天联系人列表
     * 2：网易
     * @param imei
     */
    public void getChatList(String imei){
        String s = "";
        s = MsgType.IWAPCL + GlobalSettings.MSG_CONTENT_SEPERATOR + imei + GlobalSettings.MSG_CONTENT_SEPERATOR + "2" + GlobalSettings.MSG_SUFFIX_ESCAPE;
        sendMsg(s);
    }

    /**
     * 同步好友列表
     * 2:环信
     */
    public void syncFriends(String imei){
        String s = "";
        s = MsgType.IWAPT3 + GlobalSettings.MSG_CONTENT_SEPERATOR + imei + GlobalSettings.MSG_CONTENT_SEPERATOR + "2" + GlobalSettings.MSG_SUFFIX_ESCAPE;
        sendMsg(s);
    }

    /**
     * 上传心率和血压
     *
     * @param heartRate
     * @param hypotension
     * @param hypertension
     */
    public void uploadHeartRateAndBloodPressure(String heartRate, String hypotension, String hypertension) {
        String s = "";
        s = MsgType.IWAPHT + GlobalSettings.MSG_CONTENT_SEPERATOR + heartRate + GlobalSettings.MSG_CONTENT_SEPERATOR + hypotension + GlobalSettings.MSG_CONTENT_SEPERATOR + hypertension + GlobalSettings.MSG_SUFFIX_ESCAPE;
        sendMsg(s);
    }

    /**
     * 请求天气
     *
     * @param baseStationInfo
     * @param language
     */
    public void requestWeather(String baseStationInfo, String language) {
        String s = "";
        s = MsgType.IWAPTQ + GlobalSettings.MSG_CONTENT_SEPERATOR + baseStationInfo + GlobalSettings.MSG_CONTENT_SEPERATOR + language + GlobalSettings.MSG_SUFFIX_ESCAPE;
        sendMsg(s);
    }

    /**
     * SOS报警
     * locale 语言
     */
    public void sos(){
        String s = "";
        Calendar c = Calendar.getInstance();
        s = MsgType.IWAP10
                + String.valueOf(c.get(Calendar.YEAR)).substring(2,4)
                + (c.get(Calendar.MONTH) + 1)
                + c.get(Calendar.DATE)
                + "V"
                + "0000.0000N00000.0000E000.0"
                + c.get(Calendar.HOUR_OF_DAY)
                + c.get(Calendar.MINUTE)
                + c.get(Calendar.SECOND)
                + "000.00"
                + "113"
                + "000"
                + "000"
                + "00000" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "000" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "0" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "0000" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "0000" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "01" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "zh_cn" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "00,HOME|74-DE-2B-44-88-8C|97"
                + GlobalSettings.MSG_SUFFIX_ESCAPE;
        //s = "IWAP10181105A2232.9806N11404.9355E000.1061830323.8706000908000502,460,0,9520,3671,00,zh-cn,00,HOME|74-DE-2B-44-88-8C|97&HOME1|74-DE-2B-44-88-8C|97&HOME2|74-DE-2B-44-88-8C|97&HOME3|74-DE-2B-44-88-8C|97#";
        sendMsg(s);
    }

    public void alarm(String type){
        String s = "";
        Calendar c = Calendar.getInstance();
        s = MsgType.IWAP10
                + String.valueOf(c.get(Calendar.YEAR)).substring(2,4)
                + (c.get(Calendar.MONTH) + 1)
                + c.get(Calendar.DATE)
                + "V"
                + "0000.0000N00000.0000E000.0"
                + c.get(Calendar.HOUR_OF_DAY)
                + c.get(Calendar.MINUTE)
                + c.get(Calendar.SECOND)
                + "000.00"
                + "113"
                + "000"
                + "000"
                + "00000" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "000" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "0" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "0000" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "0000" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + type + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "zh_cn" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "00,HOME|74-DE-2B-44-88-8C|97"
                + GlobalSettings.MSG_SUFFIX_ESCAPE;
        //s = "IWAP10181105A2232.9806N11404.9355E000.1061830323.8706000908000502,460,0,9520,3671,00,zh-cn,00,HOME|74-DE-2B-44-88-8C|97&HOME1|74-DE-2B-44-88-8C|97&HOME2|74-DE-2B-44-88-8C|97&HOME3|74-DE-2B-44-88-8C|97#";
        sendMsg(s);
    }



    /**
     * 心率血压报警
     */
    public void heartBloodPressure(String type){
        String s = "";
        s = MsgType.IWAP10
                + "000000"
                + "V"
                + "0000.0000N00000.0000E000.0"
                + "000000"
                + "000.00"
                + "113"
                + "000"
                + "000"
                + "00000" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "000" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "0" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "0000" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "0000" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + type + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "zh_cn" + GlobalSettings.MSG_CONTENT_SEPERATOR
                + "00"
                + GlobalSettings.MSG_SUFFIX_ESCAPE;
        //s = "IWAP10181105A2232.9806N11404.9355E000.1061830323.8706000908000502,460,0,9520,3671,00,zh-cn,00,HOME|74-DE-2B-44-88-8C|97&HOME1|74-DE-2B-44-88-8C|97&HOME2|74-DE-2B-44-88-8C|97&HOME3|74-DE-2B-44-88-8C|97#";
        sendMsg(s);
    }

    /*--------------------------------ip-----------------------------------*/

    public void startSocket() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SL.e("startSocket");
        String potocol = sp.getString(SPConstant.CURRENT_WICHE_PORT, "TCP");
        if(potocol.equals("TCP")){
            domainName=TcpConstants.DOMAIN;
            port = TcpConstants.PORT;
            getIP(domainName);
            SL.e("startSocket");
        }
//        else if (potocol.equals("MQTT")){
//            connectMqtt();
//        }


    }

    public void stopSocket() {
        if (mManager != null) {
            mManager.disconnect();
            mManager.unRegisterReceiver(adapter);
        }
        SL.e("stopSocket");
    }

    /*---------------------------------socket----------------------------------*/

    /**
     * 解析域名
     *
     * @param domain 待解析域名
     */
    private void getIP(String domain) {
        new MyTask().execute(domain);

    }

    public IConnectionManager getIConnectionManager() {
        if (mManager == null) {
            return null;
        }
        return mManager;
    }

    /**
     * 发送
     *
     * @param msg 待发送的文本信息
     */
     public void sendMsg(String msg) {
         String potocol = sp.getString(SPConstant.CURRENT_WICHE_PORT, "TCP");
         if(potocol.equals("TCP")){
             MsgDataBean msgDataBean = new MsgDataBean(msg);
             if (mManager != null && mManager.isConnect()) {
//            SL.e(msg + "      正在发送中...");
                 mManager.send(msgDataBean);
             } else {
                 startSocket();
                 SL.e("未创建连接");
             }
         }
//         }else if(potocol.equals("MQTT")){
//         if(mqttAndroidClient.isConnected()){
//             pubMqttMessage(msg);
//             Log.e(MQTT_TAG,"我是MQTT，我在发送数据");
//         }

//         }
    }

    /**
     * 发送
     *
     * @param msg 待发送的文本信息
     */
    public void sendMsgRe(String msg) {
        String potocol = sp.getString(SPConstant.CURRENT_WICHE_PORT, "TCP");
        if(potocol.equals("TCP")){
            MsgDataBean msgDataBean = new MsgDataBean(msg);
            if (mManager != null && mManager.isConnect()) {
            SL.e(msg + "      正在发送中...");
                mManager.send(msgDataBean);
                if (CommonAlarmReceiver.offlineMessageList.size() > 0){
                    Thread sendThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // 等待5秒
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            for (String mac : CommonAlarmReceiver.offlineMessageList) {
                                MsgDataBean msgDataBean = new MsgDataBean(mac);
                                if (mManager != null && mManager.isConnect()) {
//            SL.e(msg + "      正在发送中...");
                                    Log.e("历史数据:",mac);
                                    mManager.send(msgDataBean);
                                }
                                try {
                                    // 等待5秒
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                            CommonAlarmReceiver.offlineMessageList.clear();
                        }
                    });
                    sendThread.start();
                }
            } else {
                startSocket();
                /*if (!CommonAlarmReceiver.isNetWork){*/
                    CommonAlarmReceiver.offlineMessageList.add(msg);
                    SL.e("未创建连接添加离线数据:"+msg);
                /*}*/
            }
        }
//         }else if(potocol.equals("MQTT")){
//         if(mqttAndroidClient.isConnected()){
//             pubMqttMessage(msg);
//             Log.e(MQTT_TAG,"我是MQTT，我在发送数据");
//         }

//         }
    }

    private void createSocket(String ip, int port) {
         Log.e("thread","创建连接");
        ConnectionInfo info = new ConnectionInfo(ip, port);
        OkSocketOptions okOptions = new OkSocketOptions.Builder()
                .setHeaderProtocol(new IWNormalHeaderProtocol())
                .setWritePackageBytes(1024)
                .setReadPackageBytes(1200)
                .setPulseFrequency(AlarmTimer.HEARTBEAT_TIME)
                .build();
        mManager = OkSocket.open(info).option(okOptions);
        mManager.registerReceiver(adapter);
        mManager.connect();
    }

    @SuppressLint("StaticFieldLeak")
    private class MyTask extends AsyncTask<String, Integer, String> {
        String IPAddress = "";
        InetAddress ReturnStr1 = null;

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            String domain = params[0];
            try {
                ReturnStr1 = InetAddress.getByName(domain);
                IPAddress = ReturnStr1.getHostAddress();
            } catch (UnknownHostException e) {
                return "";
            }
            return IPAddress;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.isEmpty()) {
                SL.e("域名解析失败..." + s);
                SL.e("开始设置默认IP地址..." + ip);
            } else {
                SL.e("域名解析成功..." + s);
                ip = s;
            }
            createSocket(ip, port);
            super.onPostExecute(s);
        }
    }
}
