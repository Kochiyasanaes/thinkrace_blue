package com.xrs.bluetooth_device.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.xrs.bluetooth_device.R;
import com.xrs.bluetooth_device.model.SnrEntriy;
import com.xrs.bluetooth_device.model.SnrHistogramView;

import java.util.ArrayList;


public class LocationSnrAdapter extends RecyclerView.Adapter<LocationSnrAdapter.SnrHolder>
{
  private ArrayList<SnrEntriy> mSnrList = new ArrayList();

  public LocationSnrAdapter(ArrayList<SnrEntriy> paramArrayList)
  {
    this.mSnrList = paramArrayList;
  }

  public int getItemCount()
  {
    return this.mSnrList.size();
  }

  public void onBindViewHolder(SnrHolder paramSnrHolder, int paramInt)
  {
    paramSnrHolder.updateSnrValue((SnrEntriy)this.mSnrList.get(paramInt));
  }

  public SnrHolder onCreateViewHolder(ViewGroup paramViewGroup, int paramInt)
  {
    View itemView = LayoutInflater.from(paramViewGroup.getContext()).inflate(R.layout.item_snr_layout, paramViewGroup, false);
    // 返回新的ViewHolder实例
    return new SnrHolder(itemView);
  }

  public void updateLocationValue(ArrayList<SnrEntriy> paramArrayList)
  {
    this.mSnrList.clear();
    this.mSnrList.addAll(paramArrayList);
    notifyDataSetChanged();
  }

  static class SnrHolder extends RecyclerView.ViewHolder
  {
    private SnrHistogramView snrView;

    public SnrHolder(View paramView)
    {
      super(paramView);
/*      if (FTUtils.isL05())
        this.snrView = ((SnrHistogramView)paramView.findViewById(2131230899));
      else*/
        this.snrView = ((SnrHistogramView)paramView.findViewById(R.id.item_snr_view));
    }

    public void updateSnrValue(SnrEntriy paramSnrEntriy)
    {
      this.snrView.updateValue(paramSnrEntriy.getSnrValue(), paramSnrEntriy.getSnrNumber(), paramSnrEntriy.getSnrBg());
    }
  }
}
