package com.xrs.bluetooth_device.data;



import com.libsocket.sdk.bean.IPulseSendable;

import java.nio.charset.Charset;

public class PulseBean implements IPulseSendable {
    private String str = "";

    public PulseBean() {
    }

    @Override
    public byte[] parse() {
        getInfo();
        return str.getBytes(Charset.defaultCharset());
    }
    private void getInfo() {
        //String gsm = GSMCellLocationUtils.getMobileDbm(Utils.getContext());
  /*      String battery = BatteryUtils.getBatteryLevel(Utils.getContext());

        int step = StepUtils.getStepCount(Utils.getContext());
        if (Integer.parseInt(battery) == 20 || Integer.parseInt(battery) == 10 || Integer.parseInt(battery) == 5){
            OrderUtil.getInstance().alarm(AlarmType.LowBattery);
        }*/

//        int step = Settings.System.getInt(Utils.getContext().getContentResolver(),"sr_step",0);
//        if(step < 0 ){
//            step = 0;
//        }
     /*   String content = "113" + "000" + battery + "00000" + GlobalSettings.MSG_CONTENT_SEPERATOR + step + GlobalSettings.MSG_CONTENT_SEPERATOR + "30";
        str = MsgType.IWAP03
                + GlobalSettings.MSG_CONTENT_SEPERATOR
                + content
                + GlobalSettings.MSG_SUFFIX_ESCAPE;*/
    }   
}