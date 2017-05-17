package cn.ysu.edu.realtimeshare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
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

public class LocalStorageActivity extends AppCompatActivity {
    private static final int UPDATE_LIST_FLAG = 0;
    private ArrayList<FileProperty> mFileData = new ArrayList<>();// 文件列表信息
    private File mCurrentDirectory;// 当前文件夹
    private FileItemSelectAdapter mFileItemSelectAdapter;// 文件信息适配器
    private String mRootPath;// 根目录路径
    private Handler mUpdateListHandler;

    TextView tv_currentLocation;
    ListView lv_fileList;
    TextView tv_emptyDirectoryTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_storage);

        initView();

        mFileItemSelectAdapter = new FileItemSelectAdapter(this);
        lv_fileList.setAdapter(mFileItemSelectAdapter);
        mRootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        mCurrentDirectory = new File(mRootPath);

        setCurrentLocation();

        getData(mRootPath);
        mUpdateListHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_LIST_FLAG:
                        mFileItemSelectAdapter.resetData(mFileData);
                        if(mFileData.size() == 0){
                            tv_emptyDirectoryTip.setVisibility(View.VISIBLE);
                        }else if(mFileData.size() > 0){
                            tv_emptyDirectoryTip.setVisibility(View.GONE);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void initView() {
        tv_currentLocation = (TextView) findViewById(R.id.file_current_location);
        lv_fileList = (ListView) findViewById(R.id.file_storage_list);
        tv_emptyDirectoryTip = (TextView) findViewById(R.id.storage_empty_directory);

        lv_fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileProperty fileProperty = mFileData.get(position);
                String filePath = fileProperty.getFilePath();
                if (fileProperty.isDirectory()) {
                    // 如果当前选择是文件夹
                    mCurrentDirectory = new File(filePath);
                    setCurrentLocation();
                    getData(fileProperty.getFilePath());
                } else {// 如果是当前选择是文件
                    Intent openFileIntent = FileOperationOfType.getOpenFileIntent(filePath);
                    startActivity(openFileIntent);
                }
            }
        });

    }

    private void getData(final String path) {
        // 查询列表为耗时操作，开辟一个子线程进行此操作
        new Thread() {
            @Override
            public void run() {
                findAllFilesByPath(path);
                mUpdateListHandler.sendEmptyMessage(UPDATE_LIST_FLAG);
            }
        }.start();
    }

    public void findAllFilesByPath(String path) {
        mFileData.clear();
        if (path == null || path.equals("")) {
            return;
        }
        File parentFile = new File(path);
        File[] files = parentFile.listFiles();

        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                FileProperty fileProperty = new FileProperty();
                boolean isDirectory = files[i].isDirectory();
                // 设置文件名称
                fileProperty.setFileName(files[i].getName().toString());
                // 设置文件路径
                fileProperty.setFilePath(files[i].getAbsolutePath());
                // 设置文件大小
                fileProperty.setFileSize(FileSearchUtil.convertFileSize(files[i].length()));
                // 判断是否为文件夹并设置图标
                fileProperty.setDirectory(isDirectory);
                if (fileProperty.isDirectory()) {
                    fileProperty.setIconSrcID(R.mipmap.icon_folder);
                    fileProperty.setSelected(SharedFileOperation.checkFileSelected(fileProperty));
                    mFileData.add(0,fileProperty);
                } else {
                    fileProperty.setIconSrcID(FileOperationOfType.getFileTypeIcon(files[i]));
                    fileProperty.setSelected(SharedFileOperation.checkFileSelected(fileProperty));
                    mFileData.add(mFileData.size(),fileProperty);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        isRootDirectory();
    }

    public void isRootDirectory() {
        if (mRootPath.equals(mCurrentDirectory.getAbsolutePath())) {
            finish();
        } else {
            String parentPath = mCurrentDirectory.getParent();
            mCurrentDirectory = new File(parentPath);
            setCurrentLocation();
            getData(parentPath);
        }
    }

    private void setCurrentLocation() {
        tv_currentLocation.setText(mCurrentDirectory.getPath());
    }

}
