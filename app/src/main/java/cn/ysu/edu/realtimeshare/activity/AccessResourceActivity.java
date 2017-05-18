package cn.ysu.edu.realtimeshare.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.file.FileItemScanAdapter;
import cn.ysu.edu.realtimeshare.file.bean.FileProperty;
import cn.ysu.edu.realtimeshare.file.operation.FileOperationOfType;
import cn.ysu.edu.realtimeshare.file.operation.SharedFileOperation;
import cn.ysu.edu.realtimeshare.librtsp.rtsp.RtspServer;
import cn.ysu.edu.realtimeshare.service.InitService;

/**
 * Created by BasinPei on 2017/4/18.
 */

public class AccessResourceActivity extends AppCompatActivity {
    public static final String EXTRA_WIFI_INFO_FALG = "extra_wifi_info";
    private static final String TAG = "AccessResourceActivity";

    private WifiP2pInfo mConnectedWifiInfo;
    private FileItemScanAdapter mFileItemScanAdapter;
    private ArrayList<FileProperty> mSharedFileList = new ArrayList<>();
    private ProgressDialog progressDialog;

    LinearLayout getSharedScreenContainer;
    ListView lv_sharedFileList;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView noneSharedFileTip;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_resource);
        mConnectedWifiInfo = getIntent().getParcelableExtra(EXTRA_WIFI_INFO_FALG);
        initView();

    }

    private void initView() {
        getSharedScreenContainer = (LinearLayout) findViewById(R.id.access_shared_screen);
        getSharedScreenContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GetSharedScreentTask().execute();
            }
        });
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_shared_list);
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorAccent);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                new GetSharedFileListTask().execute();
            }
        });

        noneSharedFileTip = (TextView) findViewById(R.id.none_shared_file_tip);

        lv_sharedFileList = (ListView) findViewById(R.id.shared_file_list);
        mFileItemScanAdapter = new FileItemScanAdapter(this);
        lv_sharedFileList.setAdapter(mFileItemScanAdapter);
        lv_sharedFileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final FileProperty fileProperty = mSharedFileList.get(position);
                int fileType = FileOperationOfType.getFileType(fileProperty.getFileName());
                if (fileType == FileOperationOfType.TYPE_VIDEO) {

                    //请求媒体文件
                    String filePath = fileProperty.getFilePath();
                    String suffix = cn.ysu.edu.realtimeshare.httpserver.servlet.File.getSuffixByPath(filePath);
                    String path = "http://" + mConnectedWifiInfo.groupOwnerAddress.getHostAddress() + ":"
                            + SharedFileOperation.HTTP_FILE_PORT + "/File/"
                            + fileProperty.hashCode() + suffix;

                   /* try {
                        path = URLEncoder.encode(path, "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }*/

                    /*Intent mediaPlayIntent = new Intent(AccessResourceActivity.this, MediaPlayerActivity.class);
                    mediaPlayIntent.putExtra("path", path);
                    startActivity(mediaPlayIntent);*/

                    Intent i = new Intent(Intent.ACTION_VIEW);
                    Uri uri = Uri.parse(path);
                    i.setData(uri);
                    startActivity(i);


                } else {
                    //请求非媒体文件
                    new GetSharedFileTask().execute(fileProperty);
                }
            }
        });
        lv_sharedFileList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0)
                    swipeRefreshLayout.setEnabled(true);
                else
                    swipeRefreshLayout.setEnabled(false);
            }
        });

    }


    /**
     * 获得分享文件列表
     */
    class GetSharedFileListTask extends AsyncTask<Void, Void, ArrayList<FileProperty>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected ArrayList<FileProperty> doInBackground(Void... params) {
            Socket socket = null;
            try {
                socket = new Socket(mConnectedWifiInfo.groupOwnerAddress.getHostAddress(), InitService.GROUP_OWNER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(InitService.REQUEST_FLAG, InitService.REQUEST_SHARED_FILE);
//                socket.connect(new InetSocketAddress(mConnectedWifiInfo.groupOwnerAddress.getHostAddress(),InitService.GROUP_OWNER_PORT),SOCKET_TIMEOUT);

                OutputStream os = socket.getOutputStream();
                Log.d("testtest", "doInBackground: --------->" + jsonObject.toString());
                os.write(jsonObject.toString().getBytes());
                os.flush();
                socket.shutdownOutput();

                //从服务端读取数据
                InputStream ins = socket.getInputStream();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] by = new byte[1024];
                int n;
                while ((n = ins.read(by)) != -1) {
                    baos.write(by, 0, n);
                }
                baos.close();

                Log.d("testtest", "doInBackground:---------> " + baos.toString());
                JSONArray jsonArray = new JSONArray(baos.toString());

                mSharedFileList.clear();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject temp = jsonArray.getJSONObject(i);
                    FileProperty sharedFile = new FileProperty();
                    sharedFile.setFileName(temp.getString(FileProperty.NAME_KEY));
                    sharedFile.setFilePath(temp.getString(FileProperty.PATH_KEY));
                    sharedFile.setFileSize(temp.getString(FileProperty.SIZE_KEY));
                    sharedFile.setIconSrcID(temp.getInt(FileProperty.ICON_KEY));
                    mSharedFileList.add(sharedFile);
                }
                os.close();
                ins.close();
            } catch (JSONException e) {
                Log.e("testtest", "doInBackground: " + e.getMessage());
            } catch (UnknownHostException e) {
                Log.e("testtest", "doInBackground: " + e.getMessage());

            } catch (IOException e) {
                Log.e("testtest", "doInBackground: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return mSharedFileList;
        }

        @Override
        protected void onPostExecute(ArrayList<FileProperty> fileProperties) {
            swipeRefreshLayout.setRefreshing(false);
            if(fileProperties.size() > 0){
                noneSharedFileTip.setVisibility(View.GONE);
                mFileItemScanAdapter.resetData(fileProperties);
            }else if(fileProperties.size() == 0){
                noneSharedFileTip.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 获得分享的文件
     */
    class GetSharedFileTask extends AsyncTask<FileProperty, Void, File> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(AccessResourceActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.search_loading));
            progressDialog.show();
        }

        @Override
        protected File doInBackground(FileProperty... params) {
            Socket socket = null;
            try {
                socket = new Socket(mConnectedWifiInfo.groupOwnerAddress.getHostAddress(), InitService.GROUP_OWNER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = new JSONObject();
            File targetfile = null;
            try {
                jsonObject.put(InitService.REQUEST_FLAG, InitService.REQUEST_NO_MEDIA_FILE);
                jsonObject.put(InitService.SHARED_FILE_PATH, params[0].getFilePath());
//                socket.connect(new InetSocketAddress(mConnectedWifiInfo.groupOwnerAddress.getHostAddress(), InitService.GROUP_OWNER_PORT), SOCKET_TIMEOUT);

                Log.d(TAG, "doInBackground: " + socket.isOutputShutdown());
                OutputStream os = socket.getOutputStream();
                os.write(jsonObject.toString().getBytes());
                os.flush();
                socket.shutdownOutput();

                //创建文件夹和文件
                targetfile = new File(Environment.getExternalStorageDirectory() + "/" + AccessResourceActivity.this.getPackageName() + "/" + params[0].getFileName());
                File parentDirectory = new File(targetfile.getParent());
                if (!parentDirectory.exists()) {
                    parentDirectory.mkdirs();
                }
                targetfile.createNewFile();
                //从服务端读取数据
                InputStream ins = socket.getInputStream();
                boolean result = InitService.copyFile(ins, new FileOutputStream(targetfile));
                Log.d(TAG, "doInBackground: " + result);

                ins.close();
                os.close();
            } catch (JSONException e) {
                Log.e(TAG, "doInBackground: " + e.getMessage());
            } catch (UnknownHostException e) {
                Log.e(TAG, "doInBackground: " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "doInBackground: " + e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
            return targetfile;
        }

        @Override
        protected void onPostExecute(File targetFile) {
            super.onPostExecute(targetFile);
            progressDialog.dismiss();
            if (targetFile.exists()) {
                if (targetFile.length() == 0) {
                    targetFile.delete();
                    Toast.makeText(AccessResourceActivity.this, R.string.access_file_fail, Toast.LENGTH_LONG).show();
                } else {
                    Intent openFileIntent = FileOperationOfType.getOpenFileIntent(targetFile.getPath());
                    startActivity(openFileIntent);
                }
            }
        }
    }

    /**
     * 获得共享屏幕
     */
    class GetSharedScreentTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(AccessResourceActivity.this);
            progressDialog.setMessage(getResources().getString(R.string.search_loading));
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean isShareScreen = false;
            Socket socket = null;
            try {
                socket = new Socket(mConnectedWifiInfo.groupOwnerAddress.getHostAddress(), InitService.GROUP_OWNER_PORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(InitService.REQUEST_FLAG, InitService.REQUEST_SHARE_SCREEN);
//                socket.connect(new InetSocketAddress(mConnectedWifiInfo.groupOwnerAddress.getHostAddress(),InitService.GROUP_OWNER_PORT),SOCKET_TIMEOUT);

                OutputStream os = socket.getOutputStream();
                Log.d("testtest", "doInBackground: --------->" + jsonObject.toString());
                os.write(jsonObject.toString().getBytes());
                os.flush();
                socket.shutdownOutput();

                //从服务端读取数据
                InputStream ins = socket.getInputStream();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] by = new byte[1024];
                int n;
                while ((n = ins.read(by)) != -1) {
                    baos.write(by, 0, n);
                }
                baos.close();

                Log.d("testtest", "doInBackground:---------> " + baos.toString());
                JSONObject resultJsonObject = new JSONObject(baos.toString());
                isShareScreen = resultJsonObject.getBoolean(InitService.SHARE_SCREEN_FALG);

                os.close();
                ins.close();
            } catch (JSONException e) {
                Log.e("testtest", "doInBackground: " + e.getMessage());
            } catch (UnknownHostException e) {
                Log.e("testtest", "doInBackground: " + e.getMessage());

            } catch (IOException e) {
                Log.e("testtest", "doInBackground: " + e.getMessage());
                e.printStackTrace();
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return isShareScreen;
        }

        @Override
        protected void onPostExecute(Boolean isShareScreen) {
            progressDialog.dismiss();
            if (!isShareScreen) {//主机没有分享屏幕
                Toast.makeText(AccessResourceActivity.this, "false", Toast.LENGTH_SHORT).show();
            } else {//主机分享屏幕，获得rtsp路径
                String urlPath = "rtsp://" + mConnectedWifiInfo.groupOwnerAddress.getHostAddress() + ":" + RtspServer.DEFAULT_RTSP_PORT;

                Intent i = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(urlPath);
                i.setData(uri);
                startActivity(i);

                /*Intent screenVideoIntent = VideoViewActivity.getIntent(AccessResourceActivity.this,urlPath);
                AccessResourceActivity.this.startActivity(screenVideoIntent);*/
            }
        }
    }
}
