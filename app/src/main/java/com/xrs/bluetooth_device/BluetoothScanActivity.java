package com.xrs.bluetooth_device;

/**
 * @ClassName BluetoothScanActivity
 * @Author kotlin
 * @Email 949390151@qq.com
 * @Date 2025/10/30 17:36
 * ^_^^_^^_^^_^^_^^_^^_^
 */
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.xrs.bluetooth_device.utils.BlueToothUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BluetoothScanActivity extends Activity {
    private boolean mScrollEnable = true;
    private Runnable mScrollRunnable;
    private BluetoothAdapter mBtAdapter;
    private static final int TOTAL_ROUND = 3;          // 想扫 3 轮 ≈ 36 s
    private int mRound = 0;
    private ListView mListView;
    private DeviceAdapter mAdapter;
    private TextView mTvTip;
    public static BlueToothUtils blueToothUtil = new BlueToothUtils();
    private final Map<String, Device> mDeviceMap = new LinkedHashMap<>(); // 顺序也要保留
    private static class Device {
        String name, mac;
        short rssi;
        Device(String name, String mac, short rssi) {
            this.name = name; this.mac = mac; this.rssi = rssi;
        }
    }

    private final List<Device> mDevices = new ArrayList<>();

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice dev = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String mac = dev.getAddress();
                String name = dev.getName();
                if (name == null) name = "Unknown";
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                // 同 MAC 只更新 RSSI，不新增条目
                mDeviceMap.put(mac, new Device(name, mac, rssi));

                // 把 map 转 list 给 adapter
                mDevices.clear();
                mDevices.addAll(mDeviceMap.values());
                mAdapter.notifyDataSetChanged();

                mTvTip.setText("共扫描到 " + mDeviceMap.size() + " 个设备");
            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                // 想扫 3 轮就简单计数，到点自动停
                mRound++;
                if (mRound < 3) {                    // 例如 3 轮 ≈ 36 s
                    mListView.postDelayed(new Runnable() {
                        @Override public void run() {
                            mBtAdapter.startDiscovery(); // 歇 500 ms 再发
                        }
                    }, 500);
                } else {
                    mTvTip.setText("扫描完成，共 " + mDeviceMap.size() + " 个设备");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blue_activity);

        mListView = findViewById(R.id.lv_devices);
        mTvTip    = findViewById(R.id.tv_tip);
        mAdapter  = new DeviceAdapter();
        mListView.setAdapter(mAdapter);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null) {
            Toast.makeText(this, "本机无蓝牙", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mBtAdapter.isEnabled()) {
            blueToothUtil.startBlueEnable(BluetoothAdapter.getDefaultAdapter(), this);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);   // +++
        registerReceiver(mReceiver, filter);
        mBtAdapter.startDiscovery();
        // 缓慢自动滚动
        mScrollRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mScrollEnable) return;
                if (mListView.getLastVisiblePosition() >= mAdapter.getCount() - 1) {
                    mListView.smoothScrollToPosition(0);          // 回顶
                } else {
                    mListView.smoothScrollBy(1, 50);
                }
                mListView.postDelayed(this, 50);
            }
        };
        mListView.post(mScrollRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScrollEnable = false;          // 停掉循环
        mListView.removeCallbacks(mScrollRunnable);
        if (mBtAdapter != null) mBtAdapter.cancelDiscovery();
        unregisterReceiver(mReceiver);
    }

    /* ====================== Adapter ====================== */
    private class DeviceAdapter extends BaseAdapter {
        @Override public int getCount() { return mDevices.size(); }
        @Override public Device getItem(int position) { return mDevices.get(position); }
        @Override public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder h;
            if (convertView == null) {
                convertView = LayoutInflater.from(BluetoothScanActivity.this)
                        .inflate(R.layout.item_device, parent, false);
                h = new ViewHolder();
                h.tvName = convertView.findViewById(R.id.tv_name);
                h.tvMac  = convertView.findViewById(R.id.tv_mac);
                h.tvRssi = convertView.findViewById(R.id.tv_rssi);
                convertView.setTag(h);
            } else {
                h = (ViewHolder) convertView.getTag();
            }
            Device d = getItem(position);
            h.tvName.setText(d.name);
            h.tvMac.setText ("MAC: " + d.mac);
            h.tvRssi.setText("RSSI: " + d.rssi + " dBm");
            return convertView;
        }
        private class ViewHolder {
            TextView tvName, tvMac, tvRssi;
        }
    }
}
