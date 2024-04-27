package com.xrs.bluetooth_device.function;

import android.os.Parcel;
import android.os.Parcelable;

public class AlarmInfo implements Parcelable {

    private String days;
    private int index;
    private String startTime;
    private String endTime;

    public AlarmInfo(){

    }

    public String getDays() {
        return days;
    }
    public void setDays(String days) {
        this.days = days;
    }
    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }
    public String getStartTime() {
        return startTime;
    }
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }



    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // TODO Auto-generated method stub

    }


}
