package cn.ysu.edu.realtimeshare.service;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.activity.BaseExitActivity;
import cn.ysu.edu.realtimeshare.activity.LaunchActivity;
import cn.ysu.edu.realtimeshare.activity.MainActivity;
import cn.ysu.edu.realtimeshare.activity.WelcomeActivity;
import cn.ysu.edu.realtimeshare.file.bean.FileProperty;
import cn.ysu.edu.realtimeshare.file.operation.SharedFileOperation;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPServerList;
import cn.ysu.edu.realtimeshare.httpserver.util.EasyServer;
import cn.ysu.edu.realtimeshare.librtsp.rtsp.RtspServer;
import cn.ysu.edu.realtimeshare.view.dialog.OpenScreenDialog;
import cn.ysu.edu.realtimeshare.wifip2p.WiFiDirectBroadcastRecevier;

/**
 * Created by BasinPei on 2017/4/20.
 */

public class InitService extends Service {
    public static final String TAG = "InitService";
    public static final String SHARED_FILE_PATH = "shared_file_path";
    public static final String REQUEST_FLAG = "request_flag";
    public static final String SHARE_SCREEN_FALG = "is_share_screen";
    public static final int GROUP_OWNER_PORT = 8988;
    public static final int REQUEST_SHARED_FILE = 1;
    public static final int REQUEST_NO_MEDIA_FILE = 2;
    public static final int REQUEST_MEDIA_FILE = 3;
    public static final int REQUEST_SHARE_SCREEN = 4;
    private static final String BROADCAST_FILTER = "notification_onclick";
    private static final String BROADCAST_FLAG_KEY = "notification_click";
    private static final int BROADCAST_LAUNCH_VALUE = 0X1001;
    private static final int BROADCAST_CONCEL_VALUE = 0X1002;

    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mHolderContext;

    private EasyServer mEasyServer = null;
    private ServerSocket mServerSocket = null;
    private OpenScreenDialog mOpenScreenDialog = null;

    private MainActivity.OnWiFiRecevieListener mOnWiFiRecevieListener;
    private ArrayList<FileProperty> mSharedListData = new ArrayList<>();
    private Thread mServerSocketThread;

    private BroadcastReceiver mWiFiDirectBroadcastRecevier = null;
    private final IntentFilter mIntentFilter = new IntentFilter();
    private boolean isBackgroudExecute = false;
    private boolean isShareScreen = false;
    private boolean isGroupOwner = false;
    private boolean isWifiP2pEnable = false;

    private boolean isRemainResult = true;
    private boolean remainWifiIsEnable = false;
    private WifiP2pDevice remainWifiP2pDevice = null;

    private Notification mNotification = null;
//    private NotificationManager mNotificationManager;
//    private int notificationId;
    private NotificationClickReceiver mNotificationClickReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        //add necessary intent values to be matched
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mWiFiDirectBroadcastRecevier = new WiFiDirectBroadcastRecevier(this);
        registerReceiver(mWiFiDirectBroadcastRecevier, mIntentFilter);

        initNotification();
    }

    private void initNotification() {
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this);
        notifyBuilder.setContentTitle(getResources().getString(R.string.app_name));
        notifyBuilder.setContentText(getResources().getString(R.string.running));
        notifyBuilder.setSmallIcon(R.mipmap.ic_wlan);
        // 将AutoCancel设为true后，当你点击通知栏的notification后，它会自动被取消消失
        notifyBuilder.setAutoCancel(false);
        // 将Ongoing设为true 那么notification将不能滑动删除
        notifyBuilder.setOngoing(true);
        // 从Android4.1开始，可以通过以下方法，设置notification的优先级，优先级越高的，通知排的越靠前，优先级低的，不会在手机最顶部的状态栏显示图标
        notifyBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.remote_notify);
        notifyBuilder.setContent(remoteView);
//        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotification = notifyBuilder.build();

        Intent notificationIntent = new Intent(BROADCAST_FILTER);
        notificationIntent.putExtra(BROADCAST_FLAG_KEY, BROADCAST_LAUNCH_VALUE);
        PendingIntent launchPending = PendingIntent.getBroadcast(this, 1, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.launch_application, launchPending);

        notificationIntent.putExtra(BROADCAST_FLAG_KEY, BROADCAST_CONCEL_VALUE);
        PendingIntent closePending = PendingIntent.getBroadcast(this, 2 , notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.close_server, closePending);

        IntentFilter filter = new IntentFilter(BROADCAST_FILTER);
        mNotificationClickReceiver = new NotificationClickReceiver();
        registerReceiver(mNotificationClickReceiver, filter);
    }

    public void setStartForeground() {
        startForeground((int) System.currentTimeMillis(), mNotification);
    }

    /*public void notifyNotification(){
        notificationId = (int)System.currentTimeMillis();
        mNotificationManager.notify(notificationId,mNotification);
    }*/

    /*public void concelNotification(){
        mNotificationManager.cancel(notificationId);
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void initServerSocket() {
        //监听http请求
        mEasyServer = new EasyServer();
        mEasyServer.start();
        //监听Socket连接
        mServerSocketThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (mServerSocket == null) {
                    try {
                        mServerSocket = new ServerSocket(GROUP_OWNER_PORT);
                        while (true) {
                            // 一旦有堵塞, 则表示服务器与客户端获得了连接
                            Socket client = mServerSocket.accept();
                            // 处理这次连接
                            new HandleClientRequestThread(client);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "initServerSocket: " + e.getMessage());
                    } finally {
                        if (mServerSocket != null) {
                            try {
                                mServerSocket.close();
                            } catch (Exception e) {
                                mServerSocket = null;
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        mServerSocketThread.start();

    }

    /**
     * get Service Instance
     */
    public class InitServiceBinder extends Binder {
        public InitService getInitService() {
            return InitService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new InitServiceBinder();
    }

    public void setWiFiRecevieListener(MainActivity.OnWiFiRecevieListener onWiFiRecevieListener) {
        this.mOnWiFiRecevieListener = onWiFiRecevieListener;
        if (onWiFiRecevieListener != null) {
            if (isRemainResult) {
                mOnWiFiRecevieListener.onWifiStatusResult(remainWifiIsEnable);
                mOnWiFiRecevieListener.onThisDeviceChangeResult(remainWifiP2pDevice);
                isRemainResult = false;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: stopService");
        unregisterReceiver(mWiFiDirectBroadcastRecevier);
        unregisterReceiver(mNotificationClickReceiver);

        if (isGroupOwner) {
            if (mServerSocketThread != null) {
                try {
                    mServerSocket.close();
                    mServerSocket = null;
                } catch (Exception e) {
                } finally {
                    mServerSocketThread = null;
                }
            }

            HTTPServerList httpServerList = mEasyServer.getHttpServerList();
            httpServerList.stop();
            httpServerList.close();
            httpServerList.clear();
            mEasyServer.interrupt();
        }

    }

    public void setIsWifiEnable(boolean isWifiEnable) {
        isWifiP2pEnable = isWifiEnable;
        if (isRemainResult) {
            remainWifiIsEnable = isWifiEnable;
        }

        if (mOnWiFiRecevieListener != null) {
            mOnWiFiRecevieListener.onWifiStatusResult(isWifiEnable);
        }

        if(isBackgroudExecute){
            if (!isWifiEnable) {
                closeServer();
            }
        }
    }

    public void requestPeers() {
        if (mOnWiFiRecevieListener != null) {
            mOnWiFiRecevieListener.onPeersSearchResult();
        }
    }

    public void onNetWorkInfo(NetworkInfo networkInfo) {
        if (mOnWiFiRecevieListener != null) {
            mOnWiFiRecevieListener.onConnectionChangeResult(networkInfo);
        }
    }

    public void onWifiStateChange(WifiP2pDevice thisDevice) {
        if (isRemainResult) {
            remainWifiP2pDevice = thisDevice;
        }
        if (mOnWiFiRecevieListener != null) {
            mOnWiFiRecevieListener.onThisDeviceChangeResult(thisDevice);
        }
    }

    public boolean getIsBackgroudExecute() {
        return isBackgroudExecute;
    }

    public void setIsBackgroudExecute(boolean isBackgroudExecute) {
        this.isBackgroudExecute = isBackgroudExecute;
    }

    public boolean isShareScreen() {
        return isShareScreen;
    }

    public OpenScreenDialog getOpenScreenDialog() {
        return mOpenScreenDialog;
    }

    public void setOpenScreenDialog(OpenScreenDialog openScreenDialog) {
        this.mOpenScreenDialog = openScreenDialog;
    }

    public void setShareScreen(boolean shareScreen) {
        isShareScreen = shareScreen;
    }

    public boolean isWifiP2pEnable() {
        return isWifiP2pEnable;
    }

    public void setGroupOwner(boolean groupOwner) {
        isGroupOwner = groupOwner;
    }

    public void setmWifiP2pManager(WifiP2pManager mWifiP2pManager) {
        this.mWifiP2pManager = mWifiP2pManager;
    }

    public void setmChannel(WifiP2pManager.Channel mChannel) {
        this.mChannel = mChannel;
    }

    public void setmHolderContext(MainActivity mHolderContext) {
        this.mHolderContext = mHolderContext;
    }

    public ArrayList<FileProperty> getSharedFileList() {
        return mSharedListData;
    }

    public void restoreSharedFileList(ArrayList<FileProperty> fileList) {
        mSharedListData.clear();
        mSharedListData.addAll(fileList);
    }

    public class HandleClientRequestThread implements Runnable {
        private Socket clientSocket;

        public HandleClientRequestThread(Socket socket) {
            this.clientSocket = socket;
            new Thread(this).start();
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = clientSocket.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] by = new byte[1024];
                int n;
                while ((n = inputStream.read(by)) != -1) {
                    baos.write(by, 0, n);
                }
                baos.close();

                // 处理客户端数据
                JSONObject jsonObject = new JSONObject(baos.toString());

                int optionNum = jsonObject.getInt(REQUEST_FLAG);//标记动作
                switch (optionNum) {
                    case REQUEST_SHARED_FILE:
                        JSONArray jsonArray = new JSONArray();
                        ArrayList<FileProperty> sharedFileList = SharedFileOperation.getSharedFileList();
                        for (int i = 0; i < sharedFileList.size(); i++) {
                            FileProperty temp = sharedFileList.get(i);
                            JSONObject jsonObjectSend = new JSONObject();
                            jsonObjectSend.put(FileProperty.NAME_KEY, temp.getFileName());
                            jsonObjectSend.put(FileProperty.PATH_KEY, temp.getFilePath());
                            jsonObjectSend.put(FileProperty.SIZE_KEY, temp.getFileSize());
                            jsonObjectSend.put(FileProperty.ICON_KEY, temp.getIconSrcID());
                            jsonArray.put(jsonObjectSend);
                        }
                        OutputStream fileListOutputStream = clientSocket.getOutputStream();
                        fileListOutputStream.write(jsonArray.toString().getBytes());
                        fileListOutputStream.flush();
                        fileListOutputStream.close();
//                        clientSocket.shutdownOutput();

                        break;
                    case REQUEST_NO_MEDIA_FILE:
                        String targetFilePath = jsonObject.getString(SHARED_FILE_PATH);//标记动作
                        OutputStream fileOutputStream = clientSocket.getOutputStream();
                        InputStream fileInputStream = null;
                        try {
                            fileInputStream = new FileInputStream(targetFilePath);
                        } catch (FileNotFoundException e) {
                            Log.e(TAG, "onHandleIntent: FileNotFoundException");
                            fileOutputStream.close();
                            clientSocket.shutdownOutput();
                        }
                        copyFile(fileInputStream, fileOutputStream);
                        fileOutputStream.flush();
//                        clientSocket.shutdownOutput();
                        break;
                    case REQUEST_MEDIA_FILE:

                        break;
                    case REQUEST_SHARE_SCREEN:
                        JSONObject resultJsonObject = new JSONObject();
                        boolean isShareScreen = SharedFileOperation.getIsShareScreen();
                        resultJsonObject.put(SHARE_SCREEN_FALG, isShareScreen);
                        OutputStream resultOutputStream = clientSocket.getOutputStream();
                        resultOutputStream.write(resultJsonObject.toString().getBytes());
                        resultOutputStream.flush();
                        resultOutputStream.close();
                        break;
                }
                inputStream.close();

            } catch (Exception e) {
                Log.e("testtest", "run: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public static boolean copyFile(InputStream inputStream, OutputStream outputStream) {
        byte buf[] = new byte[1024];

        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                outputStream.write(buf, 0, len);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "copyFile: " + e.getMessage());
            return false;
        }
        return true;
    }

    public class NotificationClickReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent) {

            if(intent.getAction().compareTo(BROADCAST_FILTER)==0)
            {
                switch (intent.getIntExtra(BROADCAST_FLAG_KEY,-1))
                {
                    case BROADCAST_LAUNCH_VALUE:
                        if(isBackgroudExecute){
                            Intent i=new Intent(InitService.this,WelcomeActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            InitService.this.startActivity(i);
                        }
                        break;
                    case BROADCAST_CONCEL_VALUE:
                        if (mWifiP2pManager != null) {
                            mWifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
                                @Override
                                public void onSuccess(){
                                    closeServer();
                                }

                                @Override
                                public void onFailure(int reason) {
                                }
                            });
                        }
                        break;
                }
            }
        }
    }

    private void closeServer(){
        isGroupOwner = false;
        closeShareScreen();
        if(!isBackgroudExecute){
            mHolderContext.setIsGroupOwner(false);
            mHolderContext.getLocalDeviceFragment().setCreateGroupSwitch(false);
        }
        stopService(new Intent(InitService.this,InitService.class));
        InitService.this.stopForeground(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void closeShareScreen() {
        if(mOpenScreenDialog != null){
            mOpenScreenDialog.getMediaProjection().stop();
        }

        if (isShareScreen) {
            isShareScreen = false;
            stopService(new Intent(this, RtspServer.class));
            SharedFileOperation.setIsShareScreen(false);
            if(!isBackgroudExecute){
                mHolderContext.getLocalDeviceFragment().setShareScreenSwitch(false);
            }
        }
    }
}
