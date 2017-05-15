package cn.ysu.edu.realtimeshare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedList;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.file.FileItemScanAdapter;
import cn.ysu.edu.realtimeshare.file.bean.FileProperty;
import cn.ysu.edu.realtimeshare.file.operation.FileOperationOfType;
import cn.ysu.edu.realtimeshare.file.operation.SharedFileOperation;

/**
 * Created by Administrator on 2017/4/17.
 */

public class SharedFileListActivity extends AppCompatActivity {
    private ArrayList<FileProperty> mSharedFileListData = new ArrayList<>();
    private FileItemScanAdapter mFileItemScanAdapter;
    private ListView mSharedList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_shared_list);
        mSharedFileListData = SharedFileOperation.getSharedFileList();
        initView();
        mFileItemScanAdapter = new FileItemScanAdapter(this);
        mSharedList.setAdapter(mFileItemScanAdapter);
        mFileItemScanAdapter.resetData(mSharedFileListData);
    }

    private void initView() {
        mSharedList = (ListView) findViewById(R.id.list_shared_files);
        mSharedList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FileProperty fileProperty = mSharedFileListData.get(position);
                Intent openFileIntent = FileOperationOfType.getOpenFileIntent(fileProperty.getFilePath());
                startActivity(openFileIntent);
            }
        });
    }

}
