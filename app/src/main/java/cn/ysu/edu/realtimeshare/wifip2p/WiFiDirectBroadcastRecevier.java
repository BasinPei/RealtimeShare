package cn.ysu.edu.realtimeshare.wifip2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import cn.ysu.edu.realtimeshare.service.InitService;

/**
 * Created by BasinPei on 2017/4/9.
 */

public class WiFiDirectBroadcastRecevier extends BroadcastReceiver {
    private static final String TAG = "BroadcastRecevier";
    private InitService mInitService;

    public WiFiDirectBroadcastRecevier(InitService initService) {
        super();
        this.mInitService = initService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            //检测WiFi功能是否打开
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            Log.d("testtest", "onReceive: wifi test------------>");
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {

                mInitService.setIsWifiEnable(true);
            } else {
                mInitService.setIsWifiEnable(false);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //获取当前可用连接点的列表
            mInitService.requestPeers();

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            //建立或断开连接
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            mInitService.onNetWorkInfo(networkInfo);

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            //当前设备WiFi状态发生变化
            WifiP2pDevice thisDevice = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            mInitService.onWifiStateChange(thisDevice);

        }

    }
}
