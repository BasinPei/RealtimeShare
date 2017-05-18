package cn.ysu.edu.realtimeshare.activity;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;

import cn.ysu.edu.realtimeshare.librtsp.SessionBuilder;
import cn.ysu.edu.realtimeshare.librtsp.rtsp.RtspServer;
import cn.ysu.edu.realtimeshare.service.InitService;
import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.file.operation.SharedFileOperation;
import cn.ysu.edu.realtimeshare.fragment.LocalDeviceFragment;
import cn.ysu.edu.realtimeshare.fragment.MineSettingsFragment;
import cn.ysu.edu.realtimeshare.fragment.NearByDeviceFragment;
import cn.ysu.edu.realtimeshare.view.dialog.OpenScreenDialog;
import cn.ysu.edu.realtimeshare.view.tab.MainTabContainerAdapter;
import cn.ysu.edu.realtimeshare.view.tab.TabContainerView;

public class MainActivity extends BaseExitActivity implements NearByDeviceFragment.DeviceActionListener, WifiP2pManager.ChannelListener {
    public static final String TAG = "MainActivity";
    private InitService mInitService;
    private Intent mServiceIntent;

    private boolean isWifiP2pEnabled = false;
    private boolean isRetryChannel = false;
    private boolean isGroupOwner = false;


    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;

    private LocalDeviceFragment mLocalDeviceFragment = new LocalDeviceFragment();
    private NearByDeviceFragment mNearByDeviceFragment = new NearByDeviceFragment();
    private MineSettingsFragment mMineSettingsFragment = new MineSettingsFragment();

    TabContainerView tabContainerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        mWifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(this, getMainLooper(), null);

        mServiceIntent = new Intent(MainActivity.this, InitService.class);
        startService(mServiceIntent);

    }

    /**
     * set WiFi is Enable
     *
     * @param isWifiP2pEnabled
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
        mLocalDeviceFragment.setWifiEnable(isWifiP2pEnabled);
    }

    public boolean getIsWifiEnable() {
        return isWifiP2pEnabled;
    }

    public boolean getIsGroupOwner() {
        return isGroupOwner;
    }

    /**
     * Remove all peers and clear all fileds.
     * This is called on BroadcastRecevier receving a state change event
     */
    public void resetData() {
        mNearByDeviceFragment.clearPeers();
    }


    @Override
    protected void onResume() {
        super.onResume();
        bindService(mServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(serviceConnection);
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mInitService = ((InitService.InitServiceBinder) iBinder).getInitService();
            if (mInitService.getIsBackgroudExecute()) {
                SharedFileOperation.getSharedFileList().clear();
                SharedFileOperation.setSharedFileList(mInitService.getSharedFileList());
                mLocalDeviceFragment.setCreateGroupSwitch(true);
            }

            mInitService.setWiFiRecevieListener(new OnWiFiRecevieListener() {
                @Override
                public void onWifiStatusResult(boolean isEnable) {
                    //Wifi状态发生改变
                    setIsWifiP2pEnabled(isEnable);
                    if (!isEnable) {
                        if (isGroupOwner) {
                            if (mWifiP2pManager != null) {
                                mWifiP2pManager.removeGroup(mChannel, null);
                                mLocalDeviceFragment.setCreateGroupSwitch(false);

                                closeShareScreen();
                                mLocalDeviceFragment.setShareScreenSwitch(false);
                            }
                        }
                        resetData();
                    }
                }

                @Override
                public void onPeersSearchResult() {
                    //获取当前可用连接点的列表
                    if (mWifiP2pManager != null) {
                        mWifiP2pManager.requestPeers(mChannel, mNearByDeviceFragment);
                    }
                }

                @Override
                public void onConnectionChangeResult(NetworkInfo networkInfo) {
                    //建立或断开连接
                    //TODO check disconnect

                    if (networkInfo.isConnected()) {
                        // we are connected with the other device, request connection
                        // info to find group owner IP
                        mWifiP2pManager.requestConnectionInfo(mChannel, mNearByDeviceFragment);
                    } else {
                        // It's a disconnect
                        if(isGroupOwner){
                            //主机更新已连接设备列表
                            updateConnectedDevices();
                        }else {
                            //从机更新可连接服务设备列表
                            discoverPeers();
                            //设置已连接设备名称不可见和Button enable
                            mNearByDeviceFragment.setDisconnect();
                        }

                    }
                }

                @Override
                public void onThisDeviceChangeResult(WifiP2pDevice wifiP2pDevice) {
                    //当前设备状态发生变化
                    mLocalDeviceFragment.updateThisDevice(wifiP2pDevice);
                    mNearByDeviceFragment.setDeviceStatus(wifiP2pDevice);
                    mMineSettingsFragment.updateThisDeviceName(wifiP2pDevice);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.e(TAG, "onServiceDisconnected");
        }
    };


    private void initView() {
        tabContainerView = (TabContainerView) findViewById(R.id.tab_containerview_main);
        tabContainerView.setAdapter(new MainTabContainerAdapter(this, getSupportFragmentManager(),
                new Fragment[]{mLocalDeviceFragment, mNearByDeviceFragment, mMineSettingsFragment}));
    }

    public void discoverPeers() {
        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "WifiP2pManager discoverPeers onSuccess: ");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "WifiP2pManager discoverPeers onFailure: ");
            }
        });
    }

    @Override
    public void connect(WifiP2pConfig config) {
        mWifiP2pManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.


            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, R.string.manager_connect_failed, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void disconnect() {

    }

    /**
     * 更新已连接设备列表
     */
    public void updateConnectedDevices() {
        if (mWifiP2pManager != null) {
            mWifiP2pManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    ArrayList<WifiP2pDevice> connectedDevices = new ArrayList<>();
                    connectedDevices.addAll(group.getClientList());
                    mLocalDeviceFragment.updateConnectedDevices(connectedDevices);
                }
            });
        }

    }

    /**
     * 创建群组
     *
     * @param isChecked
     * @param switchCallBack
     */
    public void switchCreateGroup(boolean isChecked, final LocalDeviceFragment.SwitchCallBack switchCallBack) {
        //创建群组
        if (isChecked) {
            if (isWifiP2pEnabled) {
                if (mWifiP2pManager != null) {
                    mWifiP2pManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            isGroupOwner = true;
                            mInitService.initServerSocket();
                        }

                        @Override
                        public void onFailure(int reason) {
                            AlertDialog.Builder tipDialog = new AlertDialog.Builder(MainActivity.this);
                            tipDialog.setTitle(R.string.tip_dialog);
                            tipDialog.setMessage(R.string.local_create_group_fail_tip);
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
                            switchCallBack.onSwithResult(false);
                        }
                    });
                }
            } else {
                switchCallBack.onSwithResult(false);
                AlertDialog.Builder tipDialog = new AlertDialog.Builder(this);
                tipDialog.setTitle(R.string.tip_dialog);
                tipDialog.setMessage(R.string.wlan_state_tip);
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

        } else {//移除群组
            if (mWifiP2pManager != null) {
                mWifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        isGroupOwner = false;
                    }

                    @Override
                    public void onFailure(int reason) {
                        switchCallBack.onSwithResult(true);
                    }
                });
            }

            mLocalDeviceFragment.clearConnectedPeers();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        _openScreenDialog.onActivityResult(requestCode, resultCode, data);

        if (SessionBuilder.getInstance().getMediaProjection() == null
                && _openScreenDialog.isOpen()) {
            //has no permission to record screen
            Toast.makeText(this, "权限获取失败", Toast.LENGTH_SHORT).show();
            mLocalDeviceFragment.setShareScreenSwitch(false);

            if (_isServiceStart) {
                _isServiceStart = false;
                try {
                    this.unbindService(_rtspServerConnection);
                } catch (Exception e) {
                }

                this.stopService(new Intent(this, RtspServer.class));
                SharedFileOperation.setIsShareScreen(false);
            }
        } else if (!_openScreenDialog.isOpen()) {
            mLocalDeviceFragment.setShareScreenSwitch(false);
        }
    }

    /**
     * 开启屏幕共享
     */
    private OpenScreenDialog _openScreenDialog;
    private boolean _isServiceStart = false;

    public RtspServer _rtspServer;
    private ServiceConnection _rtspServerConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            _rtspServer = ((RtspServer.LocalBinder) service).getService();


            _rtspServer.addCallbackListener(new RtspServer.CallbackListener() {
                @Override
                public void onError(RtspServer server, Exception e, final int error) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "RTSP服务开启失败-->" + error, Toast.LENGTH_SHORT).show();
                        }
                    });

                }

                @Override
                public void onMessage(RtspServer server, int message) {
                    switch (message) {
                        case RtspServer.MESSAGE_STREAMING_STARTED:
                            Toast.makeText(MainActivity.this, "RTSP服务开启", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setShareScreen(boolean isShareScreenCheck, final LocalDeviceFragment.SwitchCallBack switchCallBack) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            Toast.makeText(this, R.string.screen_capture_enable_false, Toast.LENGTH_SHORT).show();
            switchCallBack.onSwithResult(false);
            return;
        }

        if (isShareScreenCheck) {//开启屏幕共享
            if (isGroupOwner) {
                //开始分享屏幕
                _openScreenDialog = new OpenScreenDialog(this);
                _openScreenDialog.show();

                _openScreenDialog.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!_openScreenDialog.isOpen()) {
                            switchCallBack.onSwithResult(false);
                            SharedFileOperation.setIsShareScreen(false);
                        } else {
                            MainActivity.this.bindService(
                                    new Intent(MainActivity.this, RtspServer.class),
                                    _rtspServerConnection,
                                    Context.BIND_AUTO_CREATE);

                            switchCallBack.onSwithResult(true);
                            _isServiceStart = true;
                            SharedFileOperation.setIsShareScreen(true);
                        }
                    }
                });


            } else {
                //没有创建群组
                Toast.makeText(this, R.string.local_share_screen_fail_tip, Toast.LENGTH_LONG).show();
                switchCallBack.onSwithResult(false);
                SharedFileOperation.setIsShareScreen(false);
            }
        } else {//取消屏幕共享
            closeShareScreen();
            switchCallBack.onSwithResult(false);
        }

    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void closeShareScreen() {
        if (_openScreenDialog.getMediaProjection() != null) {
            _openScreenDialog.getMediaProjection().stop();
        }
        if (_isServiceStart) {
            _isServiceStart = false;
            MainActivity.this.unbindService(_rtspServerConnection);
        }

        MainActivity.this.stopService(
                new Intent(MainActivity.this, RtspServer.class));
        SharedFileOperation.setIsShareScreen(false);

    }


    @Override
    public void onChannelDisconnected() {
        //try once more
        if (mWifiP2pManager != null && !isRetryChannel) {
            Toast.makeText(this, R.string.channel_again_tip, Toast.LENGTH_SHORT).show();
            resetData();
            isRetryChannel = true;
            mWifiP2pManager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this, R.string.channel_lost_tip, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * service register broadcastRecevier result callbak for Activity
     */
    public interface OnWiFiRecevieListener {
        void onWifiStatusResult(boolean isEnable);

        void onPeersSearchResult();

        void onConnectionChangeResult(NetworkInfo networkInfo);

        void onThisDeviceChangeResult(WifiP2pDevice wifiP2pDevice);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mInitService != null) {
            mInitService.setWiFiRecevieListener(null);

            if (!isGroupOwner) {
                mInitService.setIsBackgroudExecute(false);
                stopService(mServiceIntent);
                mWifiP2pManager.cancelConnect(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure(int reason) {

                    }
                });

            } else {
                mInitService.setIsBackgroudExecute(true);
                mInitService.restoreSharedFileList(SharedFileOperation.getSharedFileList());
            }
        }
        if (_isServiceStart) {
            MainActivity.this.unbindService(_rtspServerConnection);
            _rtspServerConnection = null;
        }
    }
}