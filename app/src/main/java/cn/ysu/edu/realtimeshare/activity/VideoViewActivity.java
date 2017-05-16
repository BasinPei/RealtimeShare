package cn.ysu.edu.realtimeshare.activity;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import cn.ysu.edu.realtimeshare.R;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import io.vov.vitamio.widget.VideoView;

public class VideoViewActivity extends AppCompatActivity {
    private VideoView _videoView;
    private String _path;
    private AlertDialog _errorDialog;
    private AlertDialog.Builder _dialogBuilder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        if(this.getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            return;
        }

        if(!LibsChecker.checkVitamioLibs(this))
            return;

        setContentView(R.layout.activity_video_view);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        init();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    protected void init()
    {
        _videoView= (VideoView) findViewById(R.id.surface_view);
        _dialogBuilder=new AlertDialog.Builder(this);
        _dialogBuilder.setCancelable(true);


        _path=this.getIntent().getExtras()!=null?this.getIntent().getExtras().getString("path"):null;

        if(_path==null || _path.length()<=0)
        {
            _dialogBuilder.setMessage("地址解析有误");
            _dialogBuilder.setPositiveButton("确定", null);
            _dialogBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    VideoViewActivity.this.finish();
                }
            });
            _errorDialog=_dialogBuilder.create();
            _errorDialog.setCanceledOnTouchOutside(true);
            _errorDialog.show();
        }
        else
        {
            _videoView.setBufferSize(1024*1024*3);
            _videoView.setVideoPath(_path);
            _videoView.setMediaController(new MediaController(this));
            _videoView.requestFocus();
            _videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                }
            });

        }
    }
}
