package cn.ysu.edu.realtimeshare.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

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

import cn.ysu.edu.realtimeshare.activity.MainActivity;
import cn.ysu.edu.realtimeshare.file.bean.FileProperty;
import cn.ysu.edu.realtimeshare.file.operation.SharedFileOperation;
import cn.ysu.edu.realtimeshare.httpserver.http.HTTPServerList;
import cn.ysu.edu.realtimeshare.httpserver.util.EasyServer;
import cn.ysu.edu.realtimeshare.wifip2p.WiFiDirectBroadcastRecevier;

/**
 * Created by BasinPei on 2017/4/20.
 */

public class InitService extends Service {
    private static final String TAG = "InitService";
    public static final int GROUP_OWNER_PORT = 8988;
    public static final int REQUEST_SHARED_FILE = 1;
    public static final int REQUEST_NO_MEDIA_FILE = 2;
    public static final int REQUEST_MEDIA_FILE = 3;
    public static final int REQUEST_SHARE_SCREEN = 4;
    public static final String SHARED_FILE_PATH = "shared_file_path";
    public static final String REQUEST_FLAG = "request_flag";
    public static final String SHARE_SCREEN_FALG = "is_share_screen";

    private EasyServer mEasyServer=null;
    private ServerSocket mServerSocket = null;



    private MainActivity.OnWiFiRecevieListener mOnWiFiRecevieListener;
    private ArrayList<FileProperty> mSharedListData = new ArrayList<>();
    private Thread mServerSocketThread;

    private BroadcastReceiver mWiFiDirectBroadcastRecevier = null;
    private final IntentFilter mIntentFilter = new IntentFilter();
    private boolean isBackgroudExecute = false;
    private boolean isShareScreenScreen = false;
    private boolean isGroupOwner = false;


    private boolean isRemainResult = true;
    private boolean remainWifiIsEnable = false;
    private WifiP2pDevice remainWifiP2pDevice = null;

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
    }

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
                if(mServerSocket == null){
                    try {
                        mServerSocket = new ServerSocket(GROUP_OWNER_PORT);
                        while (true) {
                            // 一旦有堵塞, 则表示服务器与客户端获得了连接
                            Socket client = mServerSocket.accept();
                            // 处理这次连接
                            new HandleClientRequestThread(client);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "initServerSocket: "+e.getMessage());
                    }finally{
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
    public class InitServiceBinder extends Binder{
        public InitService getInitService(){
            return InitService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new InitServiceBinder();
    }

    public void setWiFiRecevieListener(MainActivity.OnWiFiRecevieListener onWiFiRecevieListener){
        this.mOnWiFiRecevieListener = onWiFiRecevieListener;
        if(onWiFiRecevieListener != null){
            if(isRemainResult){
                mOnWiFiRecevieListener.onWifiStatusResult(remainWifiIsEnable);
                mOnWiFiRecevieListener.onThisDeviceChangeResult(remainWifiP2pDevice);
                isRemainResult = false;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWiFiDirectBroadcastRecevier);

        if(isGroupOwner){
            if(mServerSocketThread != null){
                try {
                    mServerSocket.close();
                    mServerSocketThread.join();
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

    public void setIsWifiEnable(boolean isWifiEnable){
        if(isRemainResult){
            remainWifiIsEnable = isWifiEnable;
        }
        if(mOnWiFiRecevieListener != null){
            mOnWiFiRecevieListener.onWifiStatusResult(isWifiEnable);
        }
    }

    public void requestPeers(){
        if(mOnWiFiRecevieListener != null){
            mOnWiFiRecevieListener.onPeersSearchResult();
        }
    }

    public void onNetWorkInfo(NetworkInfo networkInfo){
        if(mOnWiFiRecevieListener != null){
            mOnWiFiRecevieListener.onConnectionChangeResult(networkInfo);
        }
    }

    public void onWifiStateChange(WifiP2pDevice thisDevice){
        if(isRemainResult){
            remainWifiP2pDevice = thisDevice;
        }
        if(mOnWiFiRecevieListener != null){
            mOnWiFiRecevieListener.onThisDeviceChangeResult(thisDevice);
        }
    }

    public boolean getIsBackgroudExecute(){
        return isBackgroudExecute;
    }

    public void setIsBackgroudExecute(boolean isBackgroudExecute){
        this.isBackgroudExecute = isBackgroudExecute;
    }

    public boolean isShareScreenScreen() {
        return isShareScreenScreen;
    }

    public void setShareScreenScreen(boolean shareScreenScreen) {
        isShareScreenScreen = shareScreenScreen;
    }

    public void setGroupOwner(boolean groupOwner) {
        isGroupOwner = groupOwner;
    }

    public ArrayList<FileProperty> getSharedFileList(){
        return mSharedListData;
    }

    public void restoreSharedFileList(ArrayList<FileProperty> fileList){
        mSharedListData.clear();
        mSharedListData.addAll(fileList);
    }

    public class HandleClientRequestThread implements Runnable {
        private Socket clientSocket;
        public HandleClientRequestThread(Socket socket){
            this.clientSocket = socket;
            new Thread(this).start();
        }

        @Override
        public void run() {
            try {
                InputStream inputStream = clientSocket.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] by = new byte[1024];
                int n ;
                while((n=inputStream.read(by))!=-1){
                    baos.write(by,0,n);
                }
                baos.close();

                // 处理客户端数据
                Log.d("testtest", "run: --------->"+baos.toString());
                JSONObject jsonObject=new JSONObject(baos.toString());

                int optionNum = jsonObject.getInt(REQUEST_FLAG);//标记动作
                switch (optionNum){
                    case REQUEST_SHARED_FILE:
                        JSONArray jsonArray = new JSONArray();
                        ArrayList<FileProperty> sharedFileList = SharedFileOperation.getSharedFileList();
                        for(int i = 0;i < sharedFileList.size();i++){
                            FileProperty temp = sharedFileList.get(i);
                            JSONObject jsonObjectSend = new JSONObject();
                            jsonObjectSend.put(FileProperty.NAME_KEY,temp.getFileName());
                            jsonObjectSend.put(FileProperty.PATH_KEY,temp.getFilePath());
                            jsonObjectSend.put(FileProperty.SIZE_KEY,temp.getFileSize());
                            jsonObjectSend.put(FileProperty.ICON_KEY,temp.getIconSrcID());
                            jsonArray.put(jsonObjectSend);
                        }
                        OutputStream fileListOutputStream = clientSocket.getOutputStream();
                        Log.d("testtest", "run: --------->"+jsonArray.toString());
                        fileListOutputStream.write(jsonArray.toString().getBytes());
                        fileListOutputStream.flush();
                        fileListOutputStream.close();
//                        clientSocket.shutdownOutput();

                        break;
                    case REQUEST_NO_MEDIA_FILE:
                        String targetFilePath = jsonObject.getString(SHARED_FILE_PATH);//标记动作
                        OutputStream fileOutputStream = clientSocket.getOutputStream();
                        InputStream fileInputStream = null;
                        try{
                            fileInputStream = new FileInputStream(targetFilePath);
                        }catch (FileNotFoundException e){
                            Log.e(TAG, "onHandleIntent: FileNotFoundException");
                            fileOutputStream.close();
                            clientSocket.shutdownOutput();
                        }
                        copyFile(fileInputStream,fileOutputStream);
                        fileOutputStream.flush();
//                        clientSocket.shutdownOutput();
                        break;
                    case REQUEST_MEDIA_FILE:
                        //启动Http响应
                        /*Log.d(TAG, "run:启动Http响应 ");
                        mEasyServer = new EasyServer();
                        mEasyServer.start();*/
                        break;
                    case REQUEST_SHARE_SCREEN:
                        JSONObject resultJsonObject = new JSONObject();
                        boolean isShareScreen = SharedFileOperation.getIsShareScreen();
                        resultJsonObject.put(SHARE_SCREEN_FALG,isShareScreen);
                        OutputStream resultOutputStream = clientSocket.getOutputStream();
                        resultOutputStream.write(resultJsonObject.toString().getBytes());
                        resultOutputStream.flush();
                        resultOutputStream.close();
                        break;
                }
                inputStream.close();

            } catch (Exception e) {
                Log.e("testtest", "run: "+e.getMessage());
                e.printStackTrace();
            }
        }
    }


    public static boolean copyFile(InputStream inputStream,OutputStream outputStream){
        byte buf[] = new byte[1024];

        int len;
        try{
            while((len = inputStream.read(buf)) != -1){
                outputStream.write(buf,0,len);
            }
            outputStream.close();
            inputStream.close();
        }catch (IOException e){
            Log.e(TAG, "copyFile: "+e.getMessage());
            return false;
        }
        return true;
    }

}
