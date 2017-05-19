package cn.ysu.edu.realtimeshare.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.ysu.edu.realtimeshare.activity.FileAddActivity;
import cn.ysu.edu.realtimeshare.activity.LaunchActivity;
import cn.ysu.edu.realtimeshare.activity.MainActivity;
import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.librtsp.PreferenceInfo;
import cn.ysu.edu.realtimeshare.wifip2p.WiFiPeerDeviceAdapter;

/**
 * Created by BasinPei on 2017/4/14.
 */

public class LocalDeviceFragment extends Fragment{
    private View mContentView;
    private LinearLayout mAddFileView;
    private WiFiPeerDeviceAdapter mWiFiPeerDeviceAdapter;
    private WifiP2pDevice mThisDevice = null;

    TextView tv_deviceName;
    TextView tv_deviceStatus;
    LinearLayout setWifiEnable;
    TextView tv_wifiStatus;
    SwitchCompat mCreateGroupSwitch;
    SwitchCompat mShareScreenSwitch;
    CompoundButton.OnCheckedChangeListener mCreateGroupSwitchListener;
    CompoundButton.OnCheckedChangeListener mShareScreenSwitchListener;

    ListView connectedDeviceList;
    TextView noneConnectedDeviceTip;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_local_device,null);
        initView();
        return mContentView;
    }

    private void initView() {
        tv_deviceName = (TextView) mContentView.findViewById(R.id.local_device_name);
        tv_deviceStatus = (TextView) mContentView.findViewById(R.id.local_device_status);
        noneConnectedDeviceTip = (TextView) mContentView.findViewById(R.id.local_none_connected_tip);
        updateThisDevice(mThisDevice);

        setWifiEnable = (LinearLayout) mContentView.findViewById(R.id.local_set_wifi);
        setWifiEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        tv_wifiStatus = (TextView) mContentView.findViewById(R.id.local_wifi_state);

        mShareScreenSwitch = (SwitchCompat) mContentView.findViewById(R.id.local_share_screen);
        mShareScreenSwitchListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((MainActivity)getActivity()).setShareScreen(isChecked,new SwitchCallBack(){
                    @Override
                    public void onSwithResult(boolean switchResult) {
                        setShareScreenSwitch(switchResult);
                    }
                });
            }
        };
        mShareScreenSwitch.setOnCheckedChangeListener(mShareScreenSwitchListener);
        setShareScreenSwitch(((MainActivity)getActivity()).isShareScreen());

        mCreateGroupSwitch = (SwitchCompat) mContentView.findViewById(R.id.switch_create_group);
        mCreateGroupSwitchListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((MainActivity)getActivity()).switchCreateGroup(isChecked,new SwitchCallBack(){
                    @Override
                    public void onSwithResult(boolean switchResult) {
                        setCreateGroupSwitch(switchResult);
                    }
                });
            }
        };
        mCreateGroupSwitch.setOnCheckedChangeListener(mCreateGroupSwitchListener);
        boolean isBackExcute = ((MainActivity) getActivity()).isBackExcute();
        if(isBackExcute){
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String deviceName = sharedPreferences.getString(PreferenceInfo.PREF_DEVICE_NAME,"RealtimeShare");
            tv_deviceName.setText(deviceName);
            tv_deviceStatus.setText(R.string.device_state_connected);
        }
        setCreateGroupSwitch(isBackExcute);

        connectedDeviceList  = (ListView) mContentView.findViewById(R.id.local_connected_devices_list);
        mWiFiPeerDeviceAdapter = new WiFiPeerDeviceAdapter(getActivity());
        connectedDeviceList.setAdapter(mWiFiPeerDeviceAdapter);

        mAddFileView = (LinearLayout) mContentView.findViewById(R.id.local_add_share_file);
        mAddFileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addFileIntent = new Intent(getActivity(), FileAddActivity.class);
                startActivity(addFileIntent);
            }
        });
    }

    public void updateThisDevice(WifiP2pDevice wifiP2pDevice) {
        if(mThisDevice == null){
            mThisDevice = wifiP2pDevice;
        }

        if(tv_deviceName != null && wifiP2pDevice != null){
            tv_deviceName .setText(wifiP2pDevice.deviceName);
            tv_deviceStatus.setText(WiFiPeerDeviceAdapter.getDeviceStatus(wifiP2pDevice.status));
            SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(getActivity());
            pref.edit().putString(PreferenceInfo.PREF_DEVICE_NAME,wifiP2pDevice.deviceName).commit();
        }

    }

    public void setWifiEnable(boolean isWifiP2pEnabled) {
        if(tv_wifiStatus != null){
            if(isWifiP2pEnabled){
                tv_wifiStatus.setText(R.string.local_wlan_open);
            }else{
                tv_wifiStatus.setText(R.string.local_wlan_close);
            }
        }

    }

    public void setCreateGroupSwitch(boolean isGroupOwner){
        if(mCreateGroupSwitch != null){
            mCreateGroupSwitch.setOnCheckedChangeListener(null);
            mCreateGroupSwitch.setChecked(isGroupOwner);
            mCreateGroupSwitch.setOnCheckedChangeListener(mCreateGroupSwitchListener);
        }

    }

    public void setShareScreenSwitch(boolean isGroupOwner){
        if(mShareScreenSwitch != null){
            mShareScreenSwitch.setOnCheckedChangeListener(null);
            mShareScreenSwitch.setChecked(isGroupOwner);
            mShareScreenSwitch.setOnCheckedChangeListener(mShareScreenSwitchListener);
        }
    }

    /**
     * 更新已连接设备
     * @param connectedDevices
     */
    public void updateConnectedDevices(ArrayList<WifiP2pDevice> connectedDevices){
        if(connectedDeviceList != null){
            if(connectedDevices.size() > 0){
                noneConnectedDeviceTip.setVisibility(View.GONE);
                mWiFiPeerDeviceAdapter.resetData(connectedDevices);
            }else if(connectedDevices.size() == 0){
                noneConnectedDeviceTip.setVisibility(View.VISIBLE);
            }
        }
    }

    public void clearConnectedPeers() {
        mWiFiPeerDeviceAdapter.resetData(new ArrayList<WifiP2pDevice>());
        noneConnectedDeviceTip.setVisibility(View.VISIBLE);
    }

    public interface SwitchCallBack{
        void onSwithResult(boolean switchResult);
    }
}
