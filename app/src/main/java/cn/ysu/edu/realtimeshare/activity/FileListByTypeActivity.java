package cn.ysu.edu.realtimeshare.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.file.FileItemSelectAdapter;
import cn.ysu.edu.realtimeshare.file.bean.FileProperty;
import cn.ysu.edu.realtimeshare.file.operation.FileOperationOfType;
import cn.ysu.edu.realtimeshare.file.operation.FileSearchUtil;
import cn.ysu.edu.realtimeshare.file.operation.SharedFileOperation;

/**
 * Created by Administrator on 2017/4/15.
 */

public class FileListByTypeActivity extends AppCompatActivity {
    public static final String EXTRA_FILE_TYPE = "extra_file_type";
    private static final int UPDATE_LIST_FLAG = 0;

    public static final int TYPE_VIDEO = 0X0001;
    public static final int TYPE_MUSIC = 0X0002;
    public static final int TYPE_IMAGE = 0X0003;
    public static final int TYPE_DOC = 0X0004;
    public static final int TYPE_APK = 0X0005;
    public static final int TYPE_RAR = 0X0006;

    private FileItemSelectAdapter mFileItemSelectAdapter;
    private int mFileType;
    private ArrayList<FileProperty> mFileList = new ArrayList<>();
    private ArrayList<File> mSearchListData = new ArrayList<>();
    private Handler mUpdateListHandler;

    ListView fileList;
    TextView noneFileTip;
    ProgressDialog progressDialog;
    SearchView searchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_list_type);
        mUpdateListHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_LIST_FLAG:
                        mFileItemSelectAdapter.resetData(mFileList);
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        if(mFileList.size() == 0){
                            noneFileTip.setVisibility(View.VISIBLE);
                        }

                        break;
                    default:
                        break;
                }
            }
        };
        progressDialog = ProgressDialog.show(this, null, getResources().getString(R.string.search_loading), true, false);

        mFileType = getIntent().getIntExtra(EXTRA_FILE_TYPE, -1);
        initView();
        loadData();
    }

    private void initView() {
        int titleResId;
        switch (mFileType) {
            case TYPE_VIDEO:
                titleResId = R.string.file_category_video;
                break;
            case TYPE_MUSIC:
                titleResId = R.string.file_category_music;
                break;
            case TYPE_IMAGE:
                titleResId = R.string.file_category_image;
                break;
            case TYPE_DOC:
                titleResId = R.string.file_category_document;
                break;
            case TYPE_APK:
                titleResId = R.string.file_category_application;
                break;
            case TYPE_RAR:
                titleResId = R.string.file_category_compress;
                break;
            default:
                titleResId = R.string.file_category_others;
                break;
        }
        getSupportActionBar().setTitle(titleResId);

        fileList = (ListView) findViewById(R.id.file_type_list);
        fileList.setTextFilterEnabled(true);
        mFileItemSelectAdapter = new FileItemSelectAdapter(this);
        fileList.setAdapter(mFileItemSelectAdapter);
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileProperty fileProperty = mFileList.get(position);
                Intent openFileIntent = FileOperationOfType.getOpenFileIntent(fileProperty.getFilePath());
                startActivity(openFileIntent);
            }
        });

        noneFileTip = (TextView) findViewById(R.id.file_type_none_tip);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_file_menu,menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search_file));
        searchView.setOnQueryTextListener(new QueryListener());
        return true;
    }

    //搜索文本监听器
    private class QueryListener implements SearchView.OnQueryTextListener
    {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            if(TextUtils.isEmpty(newText)){
                mFileItemSelectAdapter.resetData(mFileList);

            }else{
                fileList.setFilterText(newText);
            }
            return true;
        }
    }

    private void loadData() {
        final String rootPath = Environment.getExternalStorageDirectory().getPath();
        new Thread(new Runnable() {
            @Override
            public void run() {
                switch (mFileType) {
                    case TYPE_VIDEO:
                        mSearchListData.addAll(FileSearchUtil.getAllMediaFile(rootPath,FileListByTypeActivity.this, new FileSearchUtil.ISearch() {
                            @Override
                            public void search(File file) {
                                //callback for file
                            }
                        }));
                        break;
                    case TYPE_MUSIC:
                        mSearchListData.addAll(FileSearchUtil.getAllMusicFile(rootPath,FileListByTypeActivity.this, new FileSearchUtil.ISearch() {
                            @Override
                            public void search(File file) {

                            }
                        }));
                        break;
                    case TYPE_IMAGE:
                        mSearchListData.addAll(FileSearchUtil.getAllImageFile(rootPath,FileListByTypeActivity.this, new FileSearchUtil.ISearch() {
                            @Override
                            public void search(File file) {

                            }
                        }));
                        break;
                    case TYPE_DOC:
                        mSearchListData.addAll(FileSearchUtil.getAllDocFile(rootPath,FileListByTypeActivity.this, new FileSearchUtil.ISearch() {
                            @Override
                            public void search(File file) {

                            }
                        }));
                        break;
                    case TYPE_APK:
                        mSearchListData.addAll(FileSearchUtil.getAllApkFile(rootPath,FileListByTypeActivity.this, new FileSearchUtil.ISearch() {
                            @Override
                            public void search(File file) {

                            }
                        }));
                        break;
                    case TYPE_RAR:
                        mSearchListData.addAll(FileSearchUtil.getAllRarFile(rootPath,FileListByTypeActivity.this, new FileSearchUtil.ISearch() {
                            @Override
                            public void search(File file) {

                            }
                        }));
                        break;
                }

                for (File file : mSearchListData) {
                    FileProperty fileProperty = new FileProperty();
                    if (file.length() == 0 || file.isDirectory()) {
                        continue;
                    }

                    // 设置文件名称
                    fileProperty.setFileName(file.getName().toString());
                    // 设置文件路径
                    fileProperty.setFilePath(file.getPath());
                    fileProperty.setFileSize(FileSearchUtil.convertFileSize(file.length()));
                    // 判断是否为文件夹并设置图标
                    fileProperty.setDirectory(false);
                    fileProperty.setIconSrcID(FileOperationOfType.getFileTypeIcon(file));
                    fileProperty.setSelected(SharedFileOperation.checkFileSelected(fileProperty));
                    mFileList.add(fileProperty);
                }
                mUpdateListHandler.sendEmptyMessage(UPDATE_LIST_FLAG);

            }
        }).start();

    }
}
