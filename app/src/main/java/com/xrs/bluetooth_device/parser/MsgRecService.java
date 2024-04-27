package com.xrs.bluetooth_device.parser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.Settings;
import android.text.TextUtils;


import com.xrs.bluetooth_device.R;
import com.xrs.bluetooth_device.data.GlobalSettings;
import com.xrs.bluetooth_device.data.MsgType;
import com.xrs.bluetooth_device.data.TcpMsg;
import com.xrs.bluetooth_device.utils.DeviceUtils;
import com.xrs.bluetooth_device.utils.LogUtils;
import com.xrs.bluetooth_device.utils.SPUtils;
import com.xrs.bluetooth_device.utils.Utils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;


public class MsgRecService {

    public static String tempSearchWifiSerialNumber;

    private OnMsgReceiveCallBack onMsgReceiveCallBack;

    private static boolean isChat = false;

    private MsgRecService() {

    }

    public static MsgRecService instance() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final MsgRecService INSTANCE = new MsgRecService();
    }

    @SuppressLint("MissingPermission")
    public void handleRecvMsg(Context ctx, TcpMsg msg) {
        ParseSourceDataBytes parseBytes = null;
        try {
            parseBytes = new ParseSourceDataBytes(msg).invoke();
        } catch (Exception e) {
            LogUtils.e("handleRecvMsg " + e);
        } finally {
            if (null == parseBytes) {
                LogUtils.e("消息解析异常!!!!!! ");
                return;
            }
        }
        if (parseBytes.isNullResult()) return;
        String headerType = parseBytes.getHeaderType();
        String content = parseBytes.getContent();
        String serialNumber = parseBytes.getSerialNumber();
        String responseContent = parseBytes.getResponseContent();
        String orderContent = parseBytes.getOrderContent();
        LogUtils.e("接受信息.. " + content);
//        LogUtils.e(" headerType= " + headerType + ",content=" + content + ",recon=" + responseContent + ",order=" + orderContent + ",SerialNumber=" + serialNumber);
        switch (headerType) {
            case MsgType.IWBPTQ: //获得天气返回数据后广播
                onMsgReceiveCallBack.OnMsgReceive(headerType, content);
                break;
            case MsgType.IWBPVA://获取语聊账号密码
                LogUtils.e("语聊账号密码.. " + content);
                if (TextUtils.isEmpty(content)) {
                    return;
                }
                String[] strs = TextUtils.split(content, GlobalSettings.MSG_CONTENT_SEPERATOR);
                String easeId = strs[0];
                String easePwd = strs[1];
             /*   PreferController.instance().setEaseId(strs[0]);
                PreferController.instance().setEasePwd(strs[1]);
//                LoginController.instance().autoLogin();
                //登录云信音视频通话
                NetEaseManager.instance().parseLogin(easeId,easePwd*//*"bh_test_003,7dfc36c11ef082333f432b7cc840dbc9"*//*);*/
                break;
            case MsgType.IWBPS9://通知设备接受视频通话
                //默认先不登录，收到登录指令再登录
//                NetEaseManager.instance().parseLogin(PreferController.instance().getEaseId(),PreferController.instance().getEasePwd()/*"bh_test_003,7dfc36c11ef082333f432b7cc840dbc9"*/);
//                MsgSender.sendTxtMsg(MsgType.IWAPS9, serialNumber);
                break;
            case MsgType.IWBPCL://获取监护人列表
       /*         SPUtils.getInstance().put(ctx.getString(R.string.vChat_contacts_list_key),content);
                onMsgReceiveCallBack.OnMsgReceive(headerType, content);*/
                break;
            case MsgType.IWBP12: //sos号码
         /*       MsgSender.sendTxtMsg(MsgType.IWAP12, responseContent);
                if (TextUtils.isEmpty(orderContent))
                    return;
                PhoneUtil.asyncAddContacts(Utils.getContext(), orderContent, Phone.TYPE_OTHER);*/
                break;
            case MsgType.IWBP14: //设置联系人白名单
           /*     MsgSender.sendTxtMsg(MsgType.IWAP14, responseContent);
                if (TextUtils.isEmpty(content))
                    return;
                PhoneUtil.asyncAddContacts(Utils.getContext(), orderContent, Phone.TYPE_MOBILE);*/
                break;
            case MsgType.IWBP15://上传位置时间间隔设置
           /*     MsgSender.sendTxtMsg(MsgType.IWAP15, responseContent);
                String[] split = content.split(",");
                LocationUploadManager.instance().parseInteraval(split[split.length - 1]);*/
                break;
            case MsgType.IWBP16: //立即定位
          /*      MsgSender.sendTxtMsg(MsgType.IWAP16, serialNumber);
                AMapLocationManager.instance().start();*/
             /*   GpsManager.instance().requestLocation(new GpsRespondListener() {
                    @Override
                    public void onLocateSuccess(GpsLocationInfo locationInfo) {
                        LogUtils.file("onLocationChanged","gps 扫描成功");
                        *//*  AMapLocationManager.instance().start();//开始定位*//*
                    }

                    @Override
                    public void toLocateFailure(GpsErrorType failureCode) {
                        LogUtils.file("gps:onLocationChanged","gps 扫描失败");
                        AMapLocationManager.instance().start();//开始定位
                    }
                });*/
                break;
            case MsgType.IWBP17:
         /*       MsgSender.sendTxtMsg(MsgType.IWAP17, serialNumber);//回复
                AsyncHandler.postDelayed(new Runnable() {
                    public void run() {
                        try {
//                            Intent factoryReset = new Intent("android.intent.action.MASTER_CLEAR");
//                            Utils.getContext().sendBroadcast(factoryReset);
                            Utils.getContext().startService(new Intent("dw.action.masterclear"));
                        } catch (Exception e) {

                        }
                    }
                }, 2000);*/
                break;
            case MsgType.IWBP18://重启手表
             /*   MsgSender.sendTxtMsg(MsgType.IWAP18, serialNumber);//回复
                AsyncHandler.postDelayed(new Runnable() {
                    public void run() {
                        try {
//                            Intent reboot = new Intent(Intent.ACTION_REBOOT);
//                            reboot.putExtra("nowait", 1);
//                            reboot.putExtra("interval", 1);
//                            reboot.putExtra("window", 0);
//                            Utils.getContext().sendBroadcast(reboot);
                            Utils.getContext().startService(new Intent("dw.action.reboot"));
                        } catch (Exception e) {

                        }
                    }
                }, 2000);*/
                break;
            case MsgType.IWBP25://设置闹钟
             /*   MsgSender.sendTxtMsg(MsgType.IWAP25, responseContent);//回复

                AlarmClockUtil.getInstance().addAll(orderContent, ctx);*/
                break;
            case MsgType.IWBP27://语音消息提醒
              /*  MsgSender.sendTxtMsg(MsgType.IWBP27, responseContent);//回复
                //动画显示
                if (!this.isChat) {
//                    GifDialogUtil.getInstance().showGif(R.drawable.new_voice);
                    TextDialogUtil.getInstance().showText("收到语音消息");
                }
                onMsgReceiveCallBack.OnMsgReceive(headerType, orderContent);*/
                break;
            case MsgType.IWBP31://远程关机
             /*   MsgSender.sendTxtMsg(MsgType.IWAP31, serialNumber);//回复
                //广播
                AsyncHandler.postDelayed(new Runnable() {
                    public void run() {
                        try {
//                            Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
//                            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
//                            //其中false换成true,会弹出是否关机的确认窗口
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            Utils.getContext().startActivity(intent);
                            Intent intent = new Intent();
                            intent.putExtra("message","恭喜你，成功接收到发送的广播");
                            intent.setAction("com.thinkrace.watchservice.reboot");
                            KApplication.sContext.sendBroadcast(intent);
                        } catch (Exception e) {

                        }
                    }
                }, 2000);*/

                break;
            case MsgType.IWBP40://文本信息
             /*   MsgSender.sendTxtMsg(MsgType.IWAP40, serialNumber + GlobalSettings.MSG_CONTENT_SEPERATOR + "1");//回复
                String gb2312 = UnicodeUtils.GB2312Decode(orderContent);
                TextDialogUtil.getInstance().showText(gb2312);
                //广播，Launcher通知栏显示
                Intent msgIntent = new Intent(ReceiverConstant.ACTION_MSG);
                msgIntent.putExtra("msg", gb2312);
                Utils.getContext().sendBroadcast(msgIntent);*/
                break;
            case MsgType.IWBP43://点赞手表     为避免麻烦, 现在暂时回复1默认表示成功
             /*   MsgSender.sendTxtMsg(MsgType.IWAP43, serialNumber + GlobalSettings.MSG_CONTENT_SEPERATOR + "1");//回复
                final Random random = new Random();
                int ranNext = random.nextInt(2);
                if (ranNext == 1) {
                    GifDialogUtil.getInstance().showGif(R.drawable.award01);
                } else {
                    GifDialogUtil.getInstance().showGif(R.drawable.award02);
                }*/
                break;
            case MsgType.IWBP46://立即拍照
                /*MsgSender.sendTxtMsg(MsgType.IWAP46, serialNumber + GlobalSettings.MSG_CONTENT_SEPERATOR + "1");//回复
                //广播
                Intent cameraIntent = new Intent(Utils.getContext(), CameraService.class);
                Utils.getContext().startService(cameraIntent);*/
                break;
            case MsgType.IWBP60://亲情号(3个)
            /*    MsgSender.sendTxtMsg(MsgType.IWAP60, serialNumber + GlobalSettings.MSG_CONTENT_SEPERATOR + "1");//回复

                PhoneUtil.asyncAddContacts(Utils.getContext(), orderContent, Phone.TYPE_HOME);*/
                break;
            case MsgType.IWBP82://实时定位
            /*    MsgSender.sendTxtMsg(MsgType.IWAP82, serialNumber);//回复
                AlarmTimer.setLocationAlarmStart(ctx);//开始定位，5秒一次*/
                break;
            case MsgType.IWBP88://寻找设备
              /*  MsgSender.sendTxtMsg(MsgType.IWAP88, content);//回复
                RingUtils.playRing(ctx);*/
                break;
            case MsgType.IWBPS2://录音
               /* MsgSender.sendTxtMsg(MsgType.IWAPS2, content);//回复
                MyAudioManager.getInstance(VoiceFileUtils.getVoiceSecondDirectoryPath()).prepareAudioForBackgroundRecorder(9);*/
                break;
            case MsgType.IWBPNS://搜索wifi
                tempSearchWifiSerialNumber = serialNumber;
                //IWAPNS,080835,a@1e:c5:72:40:07:3b|b@1e:c5:72:40:07:3b|c@1e:c5:72:40:07:3b#

              /*  WifiUtil.getInstance().init();*/
                break;
            case MsgType.IWBPS6://设置wifi
             /*   MsgSender.sendTxtMsg(MsgType.IWAPS6, responseContent);
                WifiUtil.getInstance().addWifi(Utils.getContext(), orderContent);*/
                break;
            case MsgType.IWBPB0://设置心率血压报警阀值
             /*   MsgSender.sendTxtMsg(MsgType.IWAPB0, serialNumber);
                String[] bpb0Split = content.split(",");
                Settings.System.putInt(Utils.getContext().getContentResolver(),GlobalSettings.HeartRateMin,Integer.valueOf(bpb0Split[3]));
                Settings.System.putInt(Utils.getContext().getContentResolver(),GlobalSettings.HeartRateMax,Integer.valueOf(bpb0Split[4]));
                Settings.System.putInt(Utils.getContext().getContentResolver(),GlobalSettings.SystolicMin,Integer.valueOf(bpb0Split[5]));
                Settings.System.putInt(Utils.getContext().getContentResolver(),GlobalSettings.SystolicMax,Integer.valueOf(bpb0Split[6]));
                Settings.System.putInt(Utils.getContext().getContentResolver(),GlobalSettings.DiastolicMin,Integer.valueOf(bpb0Split[7]));
                Settings.System.putInt(Utils.getContext().getContentResolver(),GlobalSettings.DiastolicMax,Integer.valueOf(bpb0Split[8]));*/
                break;
            default:
//                LogUtils.e("通用广播 ： " + (msg == null ? "msg = null " : msg.getSourceDataString()));
                break;
        }
    }

    private class ParseSourceDataBytes {
        private boolean isNullResult;
        private String serialNumber; //指令流水号
        private String responseContent; //设备响应内容(包含指令流水号和之后的内容)(不包含imei)
        private String orderContent; //指令流水号之后的内容(不含流水号,imei)
        private TcpMsg msg;
        private String headerType; //IWBP00
        private String content; //包含IMEI和之后的内容
        private byte[] contentBytes;

        private ParseSourceDataBytes(TcpMsg msg) {
            this.msg = msg;
        }

        boolean isNullResult() {
            return isNullResult;
        }

        private String getSerialNumber() {
            return serialNumber;
        }

        private String getResponseContent() {
            return responseContent;
        }

        private String getOrderContent() {
            return orderContent;
        }

        private String getHeaderType() {
            return headerType;
        }

        public String getContent() {
            return content;
        }

        private byte[] getContentBytes() {
            return contentBytes;
        }

        private ParseSourceDataBytes invoke() {
            byte[] sourceDataBytes = msg.getSourceDataBytes();//原生字节数组
            int sourceDataLen = sourceDataBytes.length;
            LogUtils.d("sourceDataBytes " + Arrays.toString(sourceDataBytes) + "   length=" + sourceDataBytes.length);
            int indexComma = MsgParser.indexOfStrInBytes(sourceDataBytes, GlobalSettings.MSG_CONTENT_SEPERATOR);//第一个逗号的索引值
            LogUtils.d("indexComma " + indexComma);
            int contentBytesStart;
            int contentBytesIndexEnd;
            boolean hasContent = indexComma >= 0;
            byte[] headerBytes;
            int headerBytesStart, headerBytesEnd;
            if (!hasContent) {//没找到逗号
                contentBytes = null;
                headerBytesStart = 0;
                headerBytesEnd = sourceDataLen - 2;// 从1到length-2位置结束 去掉#
            } else { //找到第一个逗号的索引值
                contentBytesStart = indexComma + 1;
                contentBytesIndexEnd = sourceDataLen - 1; //去掉#
                contentBytes = new byte[contentBytesIndexEnd - contentBytesStart];
                LogUtils.d("contentBytesLength " + contentBytes.length + "    contentBytesStart=" + contentBytesStart + "   contentBytesIndexEnd=" + contentBytesIndexEnd);
                //实现将数组复制到其他数组中，把从索引0开始的contentBytes.length个数据复制到目标的索引为0的位置上
                System.arraycopy(sourceDataBytes, contentBytesStart, contentBytes, 0, contentBytes.length);
                headerBytesStart = 0;
                headerBytesEnd = indexComma - 1;
                LogUtils.d("contentBytes " + Arrays.toString(contentBytes));
                LogUtils.d("content " + new String(contentBytes, Charset.defaultCharset()));
            }
            headerBytes = new byte[headerBytesEnd - headerBytesStart + 1]; //IWBP00
            System.arraycopy(sourceDataBytes, headerBytesStart, headerBytes, 0, headerBytes.length);
            LogUtils.d("headerBytes " + Arrays.toString(headerBytes));
            String headerDataString = new String(headerBytes, Charset.defaultCharset());
            LogUtils.i("headerDataString " + headerDataString);
            String[] segment = MsgParser.getRecvHeaderSegments(headerDataString);
            if (segment.length == 0) {
                LogUtils.e("头部解析为空...");
                isNullResult = true;
                return this;
            }
            LogUtils.d("segment " + Arrays.toString(segment));//[IWBP16]
            LogUtils.d("segment.length " + segment.length);
            if (segment.length != 1) {
                LogUtils.e("头部解析格式错误....");
                isNullResult = true;
                return this;
            }
            headerType = segment[segment.length - 1];
            String lenStr = segment[0];
            LogUtils.i("headerType " + headerType);
            if (TextUtils.isEmpty(headerType)) {
                LogUtils.e("头部类型解析为空...");
                isNullResult = true;
                return this;
            }
            headerType = headerType.trim();
            if (null != contentBytes) {
                content = new String(contentBytes, Charset.defaultCharset());
                if (content.contains(GlobalSettings.MSG_CONTENT_SEPERATOR)) {
                    responseContent = content.replace(DeviceUtils.getIMEI(Utils.getContext()) + GlobalSettings.MSG_CONTENT_SEPERATOR, "");
                    serialNumber = content.split(GlobalSettings.MSG_CONTENT_SEPERATOR)[1]; //指令流水号
                    orderContent = responseContent.replace(serialNumber + GlobalSettings.MSG_CONTENT_SEPERATOR, "");
                } else {
                    serialNumber = "";
                    responseContent = "";
                }
            }
            isNullResult = false;
            return this;
        }
    }

    public interface OnMsgReceiveCallBack {
        public void OnMsgReceive(String type, String content);
    }

    public void setOnMsgReceiveCallBack(OnMsgReceiveCallBack callBack) {
        this.onMsgReceiveCallBack = callBack;
    }

    public void setChat(boolean chat) {
        this.isChat = chat;
    }
}
