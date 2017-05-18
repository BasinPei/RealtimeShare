package cn.ysu.edu.realtimeshare.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import cn.ysu.edu.realtimeshare.R;

/**
 * Created by BasinPei on 2017/4/15.
 */

public class FileAddActivity extends AppCompatActivity {
    LinearLayout loadStorageView;
    Button viewSharedList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_file);

        initView();
    }

    private void initView() {
        viewSharedList = (Button) findViewById(R.id.view_shared_files_list);
        viewSharedList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewSharedListIntent = new Intent(FileAddActivity.this,SharedFileListActivity.class);
                startActivity(viewSharedListIntent);
            }
        });

        loadStorageView = (LinearLayout) findViewById(R.id.load_storage);
        loadStorageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loadStorageIntent = new Intent(FileAddActivity.this,LocalStorageActivity.class);
                startActivity(loadStorageIntent);
            }
        });
    }

    public void onClick(View view){
        Intent intent = new Intent(FileAddActivity.this,FileListByTypeActivity.class);
        int fileType = -1;
        switch (view.getId()){
            case R.id.btn_movie:
                fileType = FileListByTypeActivity.TYPE_VIDEO;
                break;
            case R.id.btn_music:
                fileType = FileListByTypeActivity.TYPE_MUSIC;
                break;
            case R.id.btn_photo:
                fileType = FileListByTypeActivity.TYPE_IMAGE;
                break;
            case R.id.btn_doc:
                fileType = FileListByTypeActivity.TYPE_DOC;
                break;
            case R.id.btn_apk:
                fileType = FileListByTypeActivity.TYPE_APK;
                break;
            case R.id.btn_rar:
                fileType = FileListByTypeActivity.TYPE_RAR;
                break;
        }
        intent.putExtra(FileListByTypeActivity.EXTRA_FILE_TYPE,fileType);
        startActivity(intent);
    }
}
