package cn.ysu.edu.realtimeshare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.file.FileItemScanAdapter;
import cn.ysu.edu.realtimeshare.file.bean.FileProperty;
import cn.ysu.edu.realtimeshare.file.operation.FileOperationOfType;
import cn.ysu.edu.realtimeshare.file.operation.FileSearchUtil;
import cn.ysu.edu.realtimeshare.file.operation.SharedFileOperation;

/**
 * Created by BasinPei on 2017/4/27.
 */

public class ScreenCaptureFileActivity extends AppCompatActivity {
    private ArrayList<FileProperty> mListData = new ArrayList<>();
    private FileItemScanAdapter mAdapter;

    ListView lv_screenCaptureFile;
    TextView tv_noneFileTip;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_capture_file);
        initView();
        initData();
    }

    private void initData() {
        String path = Environment.getExternalStorageDirectory().getPath() + "/" + this.getPackageName() + "/RecordingScreen";
        File listRoot = new File(path);
        mListData.clear();
        if(listRoot.exists()){
            File[] files = listRoot.listFiles();
            for (int i = 0;i < files.length;i++){
                File temp = files[i];
                FileProperty fileProperty = new FileProperty();
                // 设置文件名称
                fileProperty.setFileName(temp.getName().toString());
                // 设置文件路径
                fileProperty.setFilePath(temp.getPath());
                fileProperty.setFileSize(FileSearchUtil.convertFileSize(temp.length()));
                fileProperty.setIconSrcID(R.mipmap.icon_video);
                mListData.add(fileProperty);
            }
        }
        mAdapter.resetData(mListData);
        if(mListData.size() == 0){
            tv_noneFileTip.setVisibility(View.VISIBLE);
        }

    }

    private void initView() {
        tv_noneFileTip = (TextView) findViewById(R.id.none_recordscreen_file_tip);
        lv_screenCaptureFile = (ListView) findViewById(R.id.screen_capture_file_list);
        lv_screenCaptureFile.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileProperty openFile = mListData.get(position);
                Intent openFileIntent = FileOperationOfType.getVideoFileIntent(openFile.getFilePath());
                startActivity(openFileIntent);
            }
        });
        mAdapter = new FileItemScanAdapter(this);
        lv_screenCaptureFile.setAdapter(mAdapter);

    }
}
