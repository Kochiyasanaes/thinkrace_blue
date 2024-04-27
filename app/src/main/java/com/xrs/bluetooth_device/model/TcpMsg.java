package com.xrs.bluetooth_device.model;

import android.text.TextUtils;

import com.xrs.bluetooth_device.utils.LogUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ProjectName: jiaokaodemo
 * @Package: com.xrs.bluetooth_device.model
 * @ClassName: TcpMsg
 * @Description: 消息处理类
 * @Author: kotlin
 * @CreateDate: 2022/3/10 14:22
 */
public class TcpMsg {
    protected static final AtomicInteger IDAtomic = new AtomicInteger();
    protected byte[] sourceDataBytes;//数据源
    protected String sourceDataString;//数据源
    protected int id;
    protected long time;//发送、接受消息的时间戳
    protected byte[][] endDecodeData;
    private boolean isTextMsg = true;//默认是文本信息

    public SendCallBack sendCallBack;
    private boolean isUniquee = true;//指令不允许重复发送(默认可以重复发送多个)
    public String msgType;//消息类型(避免重复)
    public String contentStr;
    public byte[] contentBytes;
    public String msgPart;//组装好的部分消息(包含分隔符',')

    public interface SendCallBack {
        public void onSuccessSend(TcpMsg msg);

        public void onErrorSend(TcpMsg msg);

    }

    //[厂商*设备ID*内容长度*内容]


    public TcpMsg() {
    }

    public TcpMsg(int id) {
        this.id = id;
    }

    public void setTime() {
        time = System.currentTimeMillis();//接收发送短信的时间
    }

    private void init() {
        id = IDAtomic.getAndIncrement();
    }

    public long getTime() {
        return time;
    }

    public byte[][] getEndDecodeData() {
        return endDecodeData;
    }

    public void setEndDecodeData(byte[][] endDecodeData) {
        this.endDecodeData = endDecodeData;
    }

    @Override
    public int hashCode() {
        return id;
    }


    public byte[] getSourceDataBytes() {
        return sourceDataBytes;
    }

    public void setSourceDataBytes(byte[] sourceDataBytes) {
        this.sourceDataBytes = sourceDataBytes;
    }

    public String getSourceDataString() {
        return sourceDataString;
    }

    public void setSourceDataString(String sourceDataString) {
        this.sourceDataString = format(sourceDataString);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static AtomicInteger getIDAtomic() {
        return IDAtomic;
    }

    public String format(String source) {
        return source;
    }

    public SendCallBack getSendCallBack() {
        return sendCallBack;
    }

    public void setSendCallBack(SendCallBack sendCallBack) {
        this.sendCallBack = sendCallBack;
    }

    public boolean isTextMsg() {
        return isTextMsg;
    }

    public void setTextMsg(boolean textMsg) {
        isTextMsg = textMsg;
    }

    public boolean isUniquee() {
        return isUniquee;
    }

    public void setUniquee(boolean uniquee) {
        isUniquee = uniquee;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getContentStr() {
        return contentStr;
    }

    public void setContentStr(String contentStr) {
        this.contentStr = contentStr;
    }

    public byte[] getContentBytes() {
        return contentBytes;
    }

    public void setContentBytes(byte[] contentBytes) {
        this.contentBytes = contentBytes;
    }

    public String getMsgPart() {
        return msgPart;
    }

    public void setMsgPart(String msgPart) {
        this.msgPart = msgPart;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TcpMsg tcpMsg = (TcpMsg) o;
//        return id == tcpMsg.id;
        boolean isUniquee = tcpMsg.isUniquee();
        if (isUniquee) {
            LogUtils.d("checkUniqueeMsg " + msgType + " -- " + tcpMsg.msgType);
            return TextUtils.equals(msgType, tcpMsg.msgType);//比较消息类型即可
        } else {
            return id == tcpMsg.id;//比较id
        }
    }
}
