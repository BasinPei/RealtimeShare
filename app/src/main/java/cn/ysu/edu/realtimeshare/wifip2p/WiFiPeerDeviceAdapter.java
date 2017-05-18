package cn.ysu.edu.realtimeshare.wifip2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import cn.ysu.edu.realtimeshare.R;

/**
 * Created by BasinPei on 2017/4/18.
 */

public class WiFiPeerDeviceAdapter extends BaseAdapter {
    private LayoutInflater mLayoutInflater;
    protected ArrayList<WifiP2pDevice> mDeviceList = new ArrayList<>();

    public WiFiPeerDeviceAdapter(Context context){
        this.mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.device_item_layout,null);
            viewHolder.tv_deviceName = (TextView) convertView.findViewById(R.id.device_name);
            viewHolder.tv_deviceStatus = (TextView) convertView.findViewById(R.id.device_details);
            convertView.setTag(viewHolder);
        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        WifiP2pDevice wifiP2pDevice = mDeviceList.get(position);
        viewHolder.tv_deviceName.setText(wifiP2pDevice.deviceName);
        viewHolder.tv_deviceStatus.setText(getDeviceStatus(wifiP2pDevice.status));

        return convertView;
    }

    public void resetData(ArrayList<WifiP2pDevice> devicesData){
        mDeviceList.clear();
        mDeviceList.addAll(devicesData);
        notifyDataSetChanged();
    }

    class ViewHolder{
        TextView tv_deviceName;
        TextView tv_deviceStatus;
    }

    public static int getDeviceStatus(int deviceStatus) {
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return R.string.device_state_available;
            case WifiP2pDevice.INVITED:
                return R.string.device_state_invited;
            case WifiP2pDevice.CONNECTED:
                return R.string.device_state_connected;
            case WifiP2pDevice.FAILED:
                return R.string.device_state_failed;
            case WifiP2pDevice.UNAVAILABLE:
                return R.string.device_state_unavailable;
            default:
                return R.string.device_state_unknow;
        }
    }
}
