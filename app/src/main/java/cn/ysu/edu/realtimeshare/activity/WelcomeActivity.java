package cn.ysu.edu.realtimeshare.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import java.util.Timer;
import java.util.TimerTask;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.librtsp.PreferenceInfo;

/**
 * Created by BasinPei on 2017/5/7.
 */

public class WelcomeActivity extends AppCompatActivity {
    private TimerTask task;
    private Timer timer = new Timer();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        task = new TimerTask() {
            @Override
            public void run() {
                checkFirstLaunch();
            }
        };
        timer.schedule(task, 1000);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //退出
        if ((Intent.FLAG_ACTIVITY_CLEAR_TOP & intent.getFlags()) != 0) {
            finish();
        }
    }

    private void checkFirstLaunch() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        boolean isFirst = sharedPreferences.getBoolean(PreferenceInfo.PREF_IS_FIRST_LAUNCH, true);

        if (isFirst) {
            startActivity(new Intent(this, LaunchActivity.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }


}
