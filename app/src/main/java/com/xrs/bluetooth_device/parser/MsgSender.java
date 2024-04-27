package com.xrs.bluetooth_device.parser;

import android.text.TextUtils;
import android.util.Log;


import com.xrs.bluetooth_device.data.GlobalSettings;
import com.xrs.bluetooth_device.data.MsgType;
import com.xrs.bluetooth_device.data.TcpMsg;
import com.xrs.bluetooth_device.utils.LogUtils;
import com.xrs.bluetooth_device.utils.OrderUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author mare
 * @Description:TODO 发送消息工具类(默认是同步)
 * @csdnblog http://blog.csdn.net/mare_blue
 * @date 2017/12/12
 * @time 18:06
 */
public class MsgSender {

    /**
     * 只发送消息类型(用于回复)
     *
     * @param msgType 消息类型
     */
    public static void sendTxtMsg(String msgType) {
        sendTxtMsg(msgType, null, null, null);
    }

    /**
     * TODO 根据类型回调 发送字符消息
     */
    public static void sendTxtMsg(String msgType, TcpMsg.SendCallBack callBack) {
        sendTxtMsg(msgType, null, null, callBack);
    }

    public static void sendTxtMsg(String msgType, String content) {
        sendTxtMsg(msgType, content, null, null);
    }

    public static void sendTxtMsg(String msgType, String content, TcpMsg.SendCallBack callBack) {
        sendTxtMsg(msgType, content, null, callBack);
    }

    public static void sendTxtMsg(String msgType, String content, String contentPart) {
        sendTxtMsg(msgType, content, contentPart, null);
    }

    public static void sendTxtMsg(String msgType, String content, String contentPart, TcpMsg.SendCallBack callBack) {
        if (checkImeiNull()) {
            LogUtils.d("sendTxtMsg imei=null");
            return;
        }
        TcpMsg msg = defaultMsgUniquee(msgType);
        msg.setContentStr(content);
        msg.setMsgPart(contentPart);
        msg.setSendCallBack(callBack);
        msg.setTextMsg(true);
        sendTcpMsg(msg);
    }

    private static void sendTcpMsg(TcpMsg msg) {
        Log.e("MsgSender","contentStr"+msg.contentStr);
        OrderUtil.getInstance().sendMsg(msg.msgType
                + GlobalSettings.MSG_CONTENT_SEPERATOR
                + msg.contentStr
                + GlobalSettings.MSG_SUFFIX_ESCAPE);
    }

    /**
     * TODO 异步发送文本消息(用于信息获取)
     *
     * @param msgType 消息类型
     */
    public static void sendAsyncTxtMsg(String msgType) {
        sendAsyncTxtMsg(msgType, null, null, null);
    }

    /**
     * TODO 异步发送文本消息
     *
     * @param msgType  消息类型
     * @param callBack 发送回调
     */
    public static void sendAsyncTxtMsg(String msgType, TcpMsg.SendCallBack callBack) {
        sendAsyncTxtMsg(msgType, null, null, callBack);
    }

    public static void sendAsyncTxtMsg(String msgType, String content) {
        sendAsyncTxtMsg(msgType, content, null, null);
    }

    public static void sendAsyncTxtMsg(String msgType, String content, TcpMsg.SendCallBack callBack) {
        sendAsyncTxtMsg(msgType, content, null, callBack);
    }

    /**
     * TODO 异步发送文本消息
     *
     * @param msgType     消息类型
     * @param content     消息内容
     * @param contentPart 消息内容前缀码(包含分隔符)
     */
    public static void sendAsyncTxtMsg(String msgType, String content, String contentPart) {
        sendAsyncTxtMsg(msgType, content, contentPart, null);
    }

    /**
     * TODO 异步发送文本消息
     */
    public static void sendAsyncTxtMsg(final String msgType, final String content,
                                       final String contentPart,
                                       final TcpMsg.SendCallBack callBack) {
        if (checkImeiNull()) {
            LogUtils.d("sendTxtMsg imei=null");
            return;
        }
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                TcpMsg msg = defaultMsgUniquee(msgType);
                msg.setContentStr(content);
                msg.setMsgPart(contentPart);
                msg.setSendCallBack(callBack);
                msg.setTextMsg(true);
                sendTcpMsg(msg);
            }
        });
    }

    public static void sendAsyncBytesMsg(String msgType, TcpMsg.SendCallBack callBack) {
        sendAsyncBytesMsg(msgType, null, null, callBack);
    }

    public static void sendAsyncBytesMsg(String msgType, byte[] content) {
        sendAsyncBytesMsg(msgType, content, null, null);
    }

    public static void sendAsyncBytesMsg(String msgType, byte[] content, TcpMsg.SendCallBack callBack) {
        sendAsyncBytesMsg(msgType, content, null, callBack);
    }

    public static void sendAsyncBytesMsg(String msgType, byte[] content, String contentPart) {
        sendAsyncBytesMsg(msgType, content, contentPart, null);
    }

    public static void sendAsyncBytesMsg(final String msgType, final byte[] content,
                                         final String contentPart, final TcpMsg.SendCallBack callBack) {
        if (checkImeiNull()) {
            LogUtils.d("sendTxtMsg imei=null");
            return;
        }

        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                final TcpMsg msg = defaultMsgUniquee(msgType);
                msg.setContentBytes(content);
                msg.setMsgPart(contentPart);
                msg.setSendCallBack(callBack);
                msg.setTextMsg(false);
                sendTcpMsg(msg);
            }
        });
    }

    public static void sendBytesMsg(String msgType, TcpMsg.SendCallBack callBack) {
        sendBytesMsg(msgType, null, null, callBack);
    }

    public static void sendBytesMsg(String msgType, byte[] content) {
        sendBytesMsg(msgType, content, null, null);
    }

    public static void sendBytesMsg(String msgType, byte[] content, TcpMsg.SendCallBack callBack) {
        sendBytesMsg(msgType, content, null, callBack);
    }

    public static void sendBytesMsg(String msgType, byte[] content, String contentPart) {
        sendBytesMsg(msgType, content, contentPart, null);
    }

    public static void sendBytesMsg(final String msgType, final byte[] content,
                                    final String contentPart, final TcpMsg.SendCallBack callBack) {
        if (checkImeiNull()) {
            LogUtils.d("sendTxtMsg imei=null");
            return;
        }
        final TcpMsg msg = defaultMsgUniquee(msgType);
        msg.setContentBytes(content);
        msg.setMsgPart(contentPart);
        msg.setSendCallBack(callBack);
        msg.setTextMsg(false);
        sendTcpMsg(msg);
    }

    private static boolean checkImeiNull() {
        return TextUtils.isEmpty(GlobalSettings.instance().getImei());
    }

    /**
     * TODO 队列里仅发送一次的消息
     *
     * @param msgType 消息类型
     * @return 是否允许重复发送
     */
    private static boolean isTypeUniquee(String msgType) {
        switch (msgType) {
            //TODO 心跳
            case MsgType.IWAPLN:
                //            case MsgType.KA:
            case MsgType.IWAP03:
                //TODO 获取账号信息
            case MsgType.IWAPVA:
            case MsgType.IWBPVU:
                //TODO 上传信息
            case MsgType.IWBP12:
                return true;
            default:
                return false;
        }
    }

    private static TcpMsg defaultMsg() {
        return new TcpMsg();
    }

    private static TcpMsg defaultMsgUniquee(String msgType) {
        TcpMsg msg = defaultMsg();
        msg.setMsgType(msgType);
        msg.setUniquee(isTypeUniquee(msgType));
        return msg;
    }

}
