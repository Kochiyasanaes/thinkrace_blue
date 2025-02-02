package com.xrs.bluetooth_device.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;


public class SnrHistogramView extends View
{
  private float mBL = 1.0F;
  private int mHeight = 0;
  private Paint mPaint = new Paint();
  private String mSnrNmber = "60";
  private String mSnrValue = "80";
  private Paint mTextPaint = new Paint();
  private int mWidth = 0;

  public SnrHistogramView(Context paramContext)
  {
    this(paramContext, null);
  }

  public SnrHistogramView(Context paramContext, AttributeSet paramAttributeSet)
  {
    this(paramContext, paramAttributeSet, 0);
  }

  public SnrHistogramView(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    super(paramContext, paramAttributeSet, paramInt);
    this.mPaint.setAntiAlias(true);
    this.mPaint.setDither(true);
    this.mPaint.setColor(-16711936);
    this.mPaint.setStyle(Style.FILL);
    this.mTextPaint.setAntiAlias(true);
    this.mTextPaint.setDither(true);
    this.mTextPaint.setColor(-1);
    this.mTextPaint.setStyle(Style.FILL);
  /*  if (FTUtils.isGT09())
      this.mTextPaint.setTextSize(spToPx(12.0F));
    else if (FTUtils.isL05())
      this.mTextPaint.setTextSize(spToPx(10.0F));
    else*/
      this.mTextPaint.setTextSize(spToPx(24.0F));
  }

  private float getHistogramHeight(float paramFloat1, float paramFloat2, float paramFloat3)
  {
    return (this.mHeight - paramFloat1 - paramFloat2) * paramFloat3;
  }

  private float[] measureTextWidth(String paramString, Paint paramPaint)
  {
    FontMetrics localFontMetrics = paramPaint.getFontMetrics();
    float f1 = localFontMetrics.bottom;
    float f2 = localFontMetrics.top;
    return new float[] { paramPaint.measureText(paramString), f1 - f2 };
  }

  private float spToPx(float paramFloat)
  {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, paramFloat, getResources().getDisplayMetrics());
  }

  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // 测量文本宽度和高度
    float[] snrNumberWidthHeight = measureTextWidth(this.mSnrNmber, this.mTextPaint);
    float[] snrValueWidthHeight = measureTextWidth(this.mSnrValue, this.mTextPaint);

    // 计算直方图高度
    float histogramHeight = getHistogramHeight(snrValueWidthHeight[1], snrNumberWidthHeight[1], this.mBL);

    // 调整文本大小以适应View的宽度
    adjustTextSizeToFitWidth(this.mSnrNmber, snrNumberWidthHeight[0]);

    // 绘制文本
    canvas.drawText(this.mSnrNmber, (this.mWidth - snrNumberWidthHeight[0]) / 2.0F, this.mHeight, this.mTextPaint);
    canvas.drawText(this.mSnrValue, (this.mWidth - snrValueWidthHeight[0]) / 2.0F, this.mHeight - snrNumberWidthHeight[1] - histogramHeight, this.mTextPaint);

    // 绘制直方图背景
    canvas.drawRect(0.0F, this.mHeight - (histogramHeight + snrNumberWidthHeight[1] - 2.0F), this.mWidth, this.mHeight - snrNumberWidthHeight[1], this.mPaint);
  }

  /**
   * 调整文本大小以适应View的宽度。
   */
  private void adjustTextSizeToFitWidth(String text, float currentWidth) {
    while (this.mWidth < currentWidth) {
      this.mTextPaint.setTextSize(this.mTextPaint.getTextSize() - 2.0F);
      currentWidth = measureTextWidth(text, this.mTextPaint)[0];
    }
  }

/*  protected void onDraw(Canvas paramCanvas)
  {
    super.onDraw(paramCanvas);
    float[] localObject = measureTextWidth(this.mSnrNmber, this.mTextPaint);
    float[] arrayOfFloat = measureTextWidth(this.mSnrValue, this.mTextPaint);
    float f = getHistogramHeight(arrayOfFloat[1], localObject[1], this.mBL);
    while (true)
    {
      int i = this.mWidth;
      if (i >= localObject[0])
        break;
      localObject = this.mTextPaint;
      ((Paint)localObject).setTextSize(((Paint)localObject).getTextSize() - 2.0F);
      localObject = measureTextWidth(this.mSnrNmber, this.mTextPaint);
    }
    paramCanvas.drawText(this.mSnrNmber, (i - localObject[0]) / 2.0F, this.mHeight, this.mTextPaint);
    paramCanvas.drawText(this.mSnrValue, (this.mWidth - arrayOfFloat[0]) / 2.0F, this.mHeight - localObject[1] - f, this.mTextPaint);
    int i = this.mHeight;
    paramCanvas.drawRect(0.0F, i - (f + localObject[1] - 2.0F), this.mWidth, i - localObject[1], this.mPaint);
  }*/



  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);

    // 根据MeasureSpec的模式设置mWidth和mHeight
    if (widthMode != MeasureSpec.UNSPECIFIED && heightMode != MeasureSpec.UNSPECIFIED) {
      this.mWidth = widthSize;
      this.mHeight = heightSize;
    } else if (widthMode == MeasureSpec.EXACTLY) {
      this.mWidth = widthSize;
    } else if (heightMode == MeasureSpec.EXACTLY) {
      this.mHeight = heightSize;
    }
  }

/*  protected void onMeasure(int paramInt1, int paramInt2)
  {
    super.onMeasure(paramInt1, paramInt2);
    int i = MeasureSpec.getSize(paramInt1);
    int j = MeasureSpec.getMode(paramInt1);
    paramInt1 = MeasureSpec.getSize(paramInt2);
    int k = MeasureSpec.getMode(paramInt2);
    if ((j != -2147483648) && (j != 0) && (paramInt2 != -2147483648) && (paramInt2 != 0))
    {
      if (j == 1073741824)
        this.mWidth = i;
      if (k == 1073741824)
        this.mHeight = paramInt1;
    }
  }*/

  public void updateValue(float paramFloat, int paramInt1, int paramInt2)
  {
    this.mPaint.setColor(paramInt2);
    this.mSnrNmber = String.valueOf(paramInt1);
    this.mSnrValue = String.valueOf((int)paramFloat);
    this.mBL = (paramFloat * 0.025F);
    invalidate();
  }
}
