package com.xrs.bluetooth_device.model;

public abstract class Content {
  public static final int CATEGORY_EMOJI = 5;
  
  public static final int CATEGORY_LOCATION = 6;
  
  public static final int CATEGORY_PICTURE = 2;
  
  public static final int CATEGORY_TEXT = 3;
  
  public static final int CATEGORY_VIDEO = 4;
  
  public static final int CATEGORY_VOICE = 1;
  
  public static final int CONTACT_CATEGORY_FAMILY = 1;
  
  public static final int CONTACT_CATEGORY_GROUP = 3;
  
  public static final int CONTACT_CATEGORY_PARTNER = 2;
  
  public static final int IN = 1;
  
  public static final int OUT = 2;
  
  public static final int REMOTE_CAMERA_OSS = 7;
  
  public static final int STATUS_ERROR = 5;
  
  public static final int STATUS_LOADING = 1;
  
  public static final int STATUS_READ = 3;
  
  public static final int STATUS_SEND2OSS = 4;
  
  public static final int STATUS_UNREAD = 2;
  
  public int category;
  
  public int contactCategory;
  
  public String contactNumber;
  
  public String content;
  
  public String icon;
  
  public int inOrOut;
  
  public long localTime;
  
  public String preview;
  
  public String serverTime;
  
  public int status;

  public int filetype;
  public int datatype;
  
  protected Content(int _category, String _content, String _preview, int _status, int _inOrOut, int _contactCategory, String _contactNumber, String _serverTime, long _localtime) {
    this.category = _category;
    this.content = _content;
    this.preview = _preview;
    this.status = _status;
    this.inOrOut = _inOrOut;
    this.contactCategory = _contactCategory;
    this.contactNumber = _contactNumber;
    this.serverTime = _serverTime;
    this.localTime = _localtime;
    this.filetype=1;
    this.datatype=1;
  }
  protected Content(int _category, String _content, String _preview, int _status, int _inOrOut,
                    int _contactCategory, String _contactNumber, String _serverTime, long _localtime,
                    int _filetype, int _datatype) {
    this.category = _category;
    this.content = _content;
    this.preview = _preview;
    this.status = _status;
    this.inOrOut = _inOrOut;
    this.contactCategory = _contactCategory;
    this.contactNumber = _contactNumber;
    this.serverTime = _serverTime;
    this.localTime = _localtime;
    this.filetype=_filetype;
    this.datatype=_datatype;
  }
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("Content{category=");
    stringBuilder.append(this.category);
    stringBuilder.append(", content='");
    stringBuilder.append(this.content);
    stringBuilder.append('\'');
    stringBuilder.append(", preview='");
    stringBuilder.append(this.preview);
    stringBuilder.append('\'');
    stringBuilder.append(", status=");
    stringBuilder.append(this.status);
    stringBuilder.append(", inOrOut=");
    stringBuilder.append(this.inOrOut);
    stringBuilder.append(", contactCategory=");
    stringBuilder.append(this.contactCategory);
    stringBuilder.append(", contactNumber='");
    stringBuilder.append(this.contactNumber);
    stringBuilder.append('\'');
    stringBuilder.append(", serverTime='");
    stringBuilder.append(this.serverTime);
    stringBuilder.append('\'');
    stringBuilder.append(", localTime=");
    stringBuilder.append(this.localTime);
    stringBuilder.append(", icon='");
    stringBuilder.append(this.icon);
    stringBuilder.append('\'');
    stringBuilder.append('}');
    return stringBuilder.toString();
  }
}

