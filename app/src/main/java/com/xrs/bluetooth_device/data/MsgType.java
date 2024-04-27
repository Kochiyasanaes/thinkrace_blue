package com.xrs.bluetooth_device.data;


public class MsgType {
    /*链路保持(带电量 步数 翻滚信息)*/
    public static final String IWAPLN = "IWAPLN";
    public static final String IWBPLN = "IWBPLN";
    /*链路保持(心跳包)*/
    public static final String IWAP03 = "IWAP03";
    public static final String IWBP03 = "IWBP03";
    /*位置数据上报(智能机)*/
    public static final String IWAPT1 = "IWAPT1";
    public static final String IWBPT1 = "IWBPT1";
    /*数据上传间隔设置*/
    public static final String IWBP15 = "IWBP15";
    public static final String IWAP15 = "IWAP15";
    /*定位指令*/
    public static final String IWBP16 = "IWBP16";
    public static final String IWAP16 = "IWAP16";
    /*获取天气-智能机*/
    public static final String IWAPTQ = "IWAPTQ";
    public static final String IWBPTQ = "IWBPTQ";
    /*设置表盘*/
    public static final String IWAPS1 = "IWAPS1";
    public static final String IWBPS1 = "IWBPS1";
    /*设置3个SOS号码*/
    public static final String IWBP12 = "IWBP12";
    public static final String IWAP12 = "IWAP12";
    /*视频、语聊获取账号登陆指令*/
    public static final String IWAPVA = "IWAPVA";
    public static final String IWBPVA = "IWBPVA";
    /*设置联系人白名单(10个)*/
    public static final String IWBP14 = "IWBP14";
    public static final String IWAP14 = "IWAP14";
    /*低电提醒*/
    public static final String IWBP04 = "IWBP04";
    public static final String IWAP04 = "IWAP04";
    /*语音上行*/
    public static final String IWAP07 = "IWAP07";
    public static final String IWBP07 = "IWBP07";
    /*语音下行- URL*/
    public static final String IWAPVU = "IWAPVU";
    public static final String IWBPVU = "IWBPVU";
    /*语音消息提醒*/
    public static final String IWAP27 = "IWAP27";
    public static final String IWBP27 = "IWBP27";
    /*图片上行*/
    public static final String IWAP42 = "IWAP42";
    public static final String IWBP42 = "IWBP42";
    /*亲情号*/
    public static final String IWAP60 = "IWAP60";
    public static final String IWBP60 = "IWBP60";
    /*远程关机*/
    public static final String IWAP31 = "IWAP31";
    public static final String IWBP31 = "IWBP31";
    /*重启手表*/
    public static final String IWAP18 = "IWAP18";
    public static final String IWBP18 = "IWBP18";
    /*找手表*/
    public static final String IWAP88 = "IWAP88";
    public static final String IWBP88 = "IWBP88";
    /*设置上课隐身时间段*/
    public static final String IWAP26 = "IWAP26";
    public static final String IWBP26 = "IWBP26";
    /*闹钟*/
    public static final String IWAP25 = "IWAP25";
    public static final String IWBP25 = "IWBP25";
    /*定时开关机*/
    public static final String IWAP79 = "IWAP79";
    public static final String IWBP79 = "IWBP79";
    /*查询话费*/
    public static final String IWAP78 = "IWAP78";
    public static final String IWBP78 = "IWBP78";
    /*远程监拍*/
    public static final String IWAP46 = "IWAP46";
    public static final String IWBP46 = "IWBP46";
    /*点赞手表*/
    public static final String IWAP43 = "IWAP43";
    public static final String IWBP43 = "IWBP43";
    /*搜索wifi*/
    public static final String IWAPNS = "IWAPNS";
    public static final String IWBPNS = "IWBPNS";
    /*搜索蓝牙*/
    public static final String IWAPT9 = "IWAPT9";
    public static final String IWBPT9 = "IWBPT9";
    /*设置WIFI*/
    public static final String IWBPS6 = "IWBPS6";
    public static final String IWAPS6 = "IWAPS6";
    /*获取语音聊天列表*/
    public static final String IWAPCL = "IWAPCL";
    public static final String IWBPCL = "IWBPCL";
    /*同步好友列表*/
    public static final String IWAPT3 = "IWAPT3";
    public static final String IWBPT3 = "IWBPT3";
    /*碰碰交友*/
    public static final String IWAPT4 = "IWAPT4";
    public static final String IWBPT4 = "IWBPT4";
    /*实时定位*/
    public static final String IWAP82 = "IWAP82";
    public static final String IWBP82 = "IWBP82";
    /*录音*/
    public static final String IWAPS2 = "IWAPS2";
    public static final String IWBPS2 = "IWBPS2";
    /*文本下发*/
    public static final String IWAP40 = "IWAP40";
    public static final String IWBP40 = "IWBP40";
    /*心率血压*/
    public static final String IWAPHT = "IWAPHT";
    public static final String IWBPHT = "IWBPHT";
    /*恢复出厂设置*/
    public static final String IWAP17 = "IWAP17";
    public static final String IWBP17 = "IWBP17";
    /*SOS报警*/
    public static final String IWAP10 = "IWAP10";
    public static final String IWBP10 = "IWBP10";
    /*心率血压报警阀值*/
    public static final String IWAPB0 = "IWAPB0";
    public static final String IWBPB0 = "IWBPB0";
    /*通知设备接受视频通话*/
    public static final String IWAPS9 = "IWAPS9";
    public static final String IWBPS9 = "IWBPS9";

    public static final String IWAPBL = "IWAPBL";

    //wifi状态
    public static final String IWAPWL = "IWAPWL";
    //跌倒
    public static final String IWAPFD = "IWAPFD";
    public static final String IWAPWR = "IWAPWR";

    //跌倒
    public static final String IWAPUB = "IWAPUB";
    //静止
    public static final String IWAPST = "IWAPST";

    private MsgType() {

    }

    private static class SingletonHolder {
        private static final MsgType INSTANCE = new MsgType();
    }

    public static MsgType instance() {
        return SingletonHolder.INSTANCE;
    }

}
