package com.libsocket.sdk.bean;

import java.io.Serializable;


public interface ISendable extends Serializable {
    /**
     * 数据转化
     *
     * @return 将要发送的数据的字节数组
     */
    byte[] parse();
}
