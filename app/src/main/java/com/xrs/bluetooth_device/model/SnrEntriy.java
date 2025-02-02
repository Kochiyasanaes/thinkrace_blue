package com.xrs.bluetooth_device.model;

public class SnrEntriy
{
  private int snrBg;
  private int snrNumber;
  private float snrValue;

  public SnrEntriy()
  {
  }

  public SnrEntriy(float paramFloat, int paramInt1, int paramInt2)
  {
    this.snrNumber = paramInt1;
    this.snrValue = paramFloat;
    this.snrBg = paramInt2;
  }

  public int getSnrBg()
  {
    return this.snrBg;
  }

  public int getSnrNumber()
  {
    return this.snrNumber;
  }

  public float getSnrValue()
  {
    return this.snrValue;
  }

  public void setSnrBg(int paramInt)
  {
    this.snrBg = paramInt;
  }

  public void setSnrNumber(int paramInt)
  {
    this.snrNumber = paramInt;
  }

  public void setSnrValue(int paramInt)
  {
    this.snrValue = paramInt;
  }
}

/* Location:           D:\software\jd-gui-0.3.3.windows\8.jar
 * Qualified Name:     mobile.miki.SnrEntriy
 * JD-Core Version:    0.6.0
 */