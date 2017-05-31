package cn.ysu.edu.realtimeshare.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import cn.ysu.edu.realtimeshare.activity.AccessResourceActivity;
import cn.ysu.edu.realtimeshare.activity.MainActivity;
import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.wifip2p.WiFiPeerDeviceAdapter;

/**
 * Created by BasinPei on 2017/4/14.
 */

public class NearByDeviceFragment extends Fragment implements WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {
    private View mContentView;
    private WiFiPeerDeviceAdapter mWiFiPeerDeviceAdapter;
    private ArrayList<WifiP2pDevice> mAvaliableDevices = new ArrayList<>();
    private WifiP2pDevice mCurrentConnectingDevice;
    private WifiP2pDevice mThisDevice = null;
    private WifiP2pInfo mCurrentConnectingInfo;

    Button btn_accessResource;
    ListView lv_nearByDeviecs;
    TextView tv_nearByNoneDevices;
    TextView tv_nearByDeviceStatus;

    LinearLayout connectedDeviceNameContainer;
    ProgressDialog progressDialog;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_nearby_device, null);
        initView();
        mWiFiPeerDeviceAdapter = new WiFiPeerDeviceAdapter(getActivity());
        lv_nearByDeviecs.setAdapter(mWiFiPeerDeviceAdapter);
        return mContentView;
    }

    private void initView() {
        tv_nearByNoneDevices = (TextView) mContentView.findViewById(R.id.nearby_none_device_tip);

        connectedDeviceNameContainer = (LinearLayout) mContentView.findViewById(R.id.nearby_device_name_container);

        btn_accessResource = (Button) mContentView.findViewById(R.id.nearby_access_resource);
        btn_accessResource.setEnabled(false);
        btn_accessResource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent accessResourceIntent = new Intent(getActivity(), AccessResourceActivity.class);
                accessResourceIntent.putExtra(AccessResourceActivity.EXTRA_WIFI_INFO_FALG,mCurrentConnectingInfo);
                startActivity(accessResourceIntent);
            }
        });

        tv_nearByDeviceStatus = (TextView) mContentView.findViewById(R.id.nearby_device_status);
        setDeviceStatus(mThisDevice);
        lv_nearByDeviecs = (ListView) mContentView.findViewById(R.id.nearby_avaliable_devices);
        lv_nearByDeviecs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCurrentConnectingDevice = mAvaliableDevices.get(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getResources().getString(R.string.connect_with_device)+mCurrentConnectingDevice.deviceName+"?");
                builder.setTitle(R.string.tip_dialog);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(((MainActivity)getActivity()).getIsGroupOwner()){
                            //提示当前设备是服务设备，是否关闭当前群组
                            //TODO 2017/05/27


                        }else{
                            //与选择的设备连接
                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = mCurrentConnectingDevice.deviceAddress;
                            config.wps.setup = WpsInfo.PBC;
                            if(progressDialog != null && progressDialog.isShowing()){
                                progressDialog.dismiss();
                            }
                            progressDialog = ProgressDialog.show(getActivity(),null,getResources().getString(R.string.connecting_dialog_tip),true,true);

                            ((NearByDeviceFragment.DeviceActionListener)getActivity()).connect(config);
                        }
                    }
                });
                builder.setNegativeButton(R.string.concel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.nearby_device_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_direct_discover:
                if(((MainActivity) getActivity()).getIsWifiEnable()){
                    if(progressDialog != null && progressDialog.isShowing()){
                        progressDialog.dismiss();
                    }
                    progressDialog = ProgressDialog.show(getActivity(),null,getResources().getString(R.string.nearby_searching_peers),true,true);
                    ((MainActivity) getActivity()).discoverPeers();
                }else{
                    AlertDialog.Builder tipDialog = new AlertDialog.Builder(getActivity());
                    tipDialog.setTitle(R.string.tip_dialog);
                    tipDialog.setMessage(R.string.tip_wifi_not_enable);
                    tipDialog.setPositiveButton(R.string.go_set, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        }
                    });
                    tipDialog.setNegativeButton(R.string.concel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    tipDialog.create().show();
                }


                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        if(progressDialog != null && progressDialog.isShowing()){
            progressDialog.dismiss();
        }

        mAvaliableDevices.clear();
        for(WifiP2pDevice wifiP2pDevice:peers.getDeviceList()){
            if(wifiP2pDevice.isGroupOwner()){
                mAvaliableDevices.add(wifiP2pDevice);
            }
        }

        if(mWiFiPeerDeviceAdapter != null){
            mWiFiPeerDeviceAdapter.resetData(mAvaliableDevices);
        }

        //根据可连接服务设备个数设置提示
        if(tv_nearByNoneDevices != null){
            if(mAvaliableDevices.size() > 0){
                tv_nearByNoneDevices.setVisibility(View.GONE);
            }else if(mAvaliableDevices.size() == 0){
                tv_nearByNoneDevices.setVisibility(View.VISIBLE);
            }
        }

        Activity holder = getActivity();
        if(holder != null){
            ((MainActivity)holder).discoverPeers();
        }
    }

    public void setDeviceStatus(WifiP2pDevice wifiP2pDevice){
        if(mThisDevice == null){
            mThisDevice = wifiP2pDevice;
        }

        if(tv_nearByDeviceStatus != null && wifiP2pDevice != null){
            tv_nearByDeviceStatus.setText(WiFiPeerDeviceAdapter.getDeviceStatus(wifiP2pDevice.status));
        }
    }

    public void clearPeers() {
        mAvaliableDevices.clear();
        if(mWiFiPeerDeviceAdapter != null){
            mWiFiPeerDeviceAdapter.resetData(mAvaliableDevices);
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        mCurrentConnectingInfo = info;

        if(info.groupFormed && info.isGroupOwner){
            //group owner action
            if(getActivity() != null){
                ((MainActivity)getActivity()).updateConnectedDevices();
            }

        }else if(info.groupFormed){
            //client action
            Toast.makeText(getActivity(),R.string.manager_connect_success,Toast.LENGTH_SHORT).show();

            //update the connected device name

            connectedDeviceNameContainer.setVisibility(View.VISIBLE);
            btn_accessResource.setEnabled(true);
            ((TextView)(connectedDeviceNameContainer.findViewById(R.id.nearby_device_connected_name))).setText(mCurrentConnectingDevice.deviceName);
            if(progressDialog != null && progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }

    }

    public void setDisconnect() {
        connectedDeviceNameContainer.setVisibility(View.GONE);
        btn_accessResource.setEnabled(false);
    }

    /**
     * An interface-callback for the activity to listen to fragment interaction
     * events.
     */
    public interface DeviceActionListener
    {
        void connect(WifiP2pConfig config);

        //TODO 2017/05/27 call this when disconnect with GO device
        void disconnect();
    }
}
