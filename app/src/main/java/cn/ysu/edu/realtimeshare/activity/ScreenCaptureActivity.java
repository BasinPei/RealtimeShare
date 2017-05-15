package cn.ysu.edu.realtimeshare.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.screenreplay.media.MediaMuxerWrapper;
import cn.ysu.edu.realtimeshare.screenreplay.service.ScreenRecorderService;

public class ScreenCaptureActivity extends AppCompatActivity {
    private static final byte REQUEST_SCREEN_CAPTURE_CODE = 1;
    private MediaProjectionManager _mediaProjectionManager;
    private CaptureStatus _state;
    private ScreenCaptureReceiver _captureReceiver;

    private ImageButton _startBtn;
    private ViewGroup _btnGroup;
    private ImageButton _pauseBtn;
    private ImageButton _stopBtn;
    private Button _browseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_capture);
        init();
    }

    private void viewScreenCapture(){
        Intent viewScreenCaptureIntent = new Intent(ScreenCaptureActivity.this,ScreenCaptureFileActivity.class);
        startActivity(viewScreenCaptureIntent);
    }

    protected void init() {
        _browseBtn = (Button) findViewById(R.id.view_screen_capture);
        _browseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewScreenCapture();
            }
        });

        if (_captureReceiver == null) {
            _captureReceiver = new ScreenCaptureReceiver(this);
        }

        _mediaProjectionManager = (MediaProjectionManager) this.getSystemService(MEDIA_PROJECTION_SERVICE);

        String path = Environment.getExternalStorageDirectory().getPath() + "/" + this.getPackageName() + "/RecordingScreen";
        File saveScreenCaptureDirectory = new File(path);
        if(!saveScreenCaptureDirectory.exists()){
            saveScreenCaptureDirectory.mkdirs();
        }

        Intent i = new Intent(this, ScreenRecorderService.class);
        i.setAction(ScreenRecorderService.ACTION_SET_OUTPUT_PATH);
        i.putExtra(ScreenRecorderService.EXTRA_SET_OUTPUT_PATH, path + "/" + MediaMuxerWrapper.getDateTimeString() + ".mp4") ;
        this.startService(i);

        _startBtn = (ImageButton) findViewById(R.id.btn_start);

        _btnGroup = (ViewGroup) findViewById(R.id.btn_group);

        _pauseBtn = (ImageButton) findViewById(R.id.btn_pause);

        _stopBtn = (ImageButton) findViewById(R.id.btn_stop);

        _state = CaptureStatus.STATUS_IDLE;

        changeState(_state);

        _startBtn.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                if (ScreenCaptureActivity.this._state == CaptureStatus.STATUS_IDLE
                        || ScreenCaptureActivity.this._state == CaptureStatus.STATUS_STOPPED) {
                    Intent i = _mediaProjectionManager.createScreenCaptureIntent();
                    ScreenCaptureActivity.this.startActivityForResult(i, REQUEST_SCREEN_CAPTURE_CODE);
                } else {
                    stopScreenRecorder();
                }
            }
        });

        _pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (_state == CaptureStatus.STATUS_RECORDING) {
                    pauseScreenRecorder();
                } else if (_state == CaptureStatus.STATUS_PAUSING) {
                    resumeScreenRecorder();
                }
            }
        });

        _stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopScreenRecorder();
                Intent intent = new Intent(ScreenCaptureActivity.this, ScreenRecorderService.class);
                intent.setAction(ScreenRecorderService.ACTION_RELEASE_RESOURCE);
                ScreenCaptureActivity.this.startService(intent);
                viewScreenCapture();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ScreenRecorderService.ACTION_QUERY_STATUS_RESULT);
        intentFilter.addAction(ScreenRecorderService.ACTION_QUERY_OUTPUT_PATH_RESULT);
        registerReceiver(_captureReceiver, intentFilter);
        queryRecordingStatus();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(_captureReceiver);
        super.onPause();
    }

    private void queryRecordingStatus() {
        Intent intent = new Intent(this, ScreenRecorderService.class);
        intent.setAction(ScreenRecorderService.ACTION_QUERY_STATUS);
        startService(intent);
    }


    private void startScreenRecorder(int resultCode, Intent data) {
        Intent intent = new Intent(this, ScreenRecorderService.class);
        intent.setAction(ScreenRecorderService.ACTION_START);
        intent.putExtra(ScreenRecorderService.EXTRA_RESULT_CODE, resultCode);
        intent.putExtras(data);
        startService(intent);
    }

    private void stopScreenRecorder() {
        Intent intent = new Intent(ScreenCaptureActivity.this, ScreenRecorderService.class);
        intent.setAction(ScreenRecorderService.ACTION_STOP);
        startService(intent);
    }

    private void pauseScreenRecorder() {
        Intent intent = new Intent(ScreenCaptureActivity.this, ScreenRecorderService.class);
        intent.setAction(ScreenRecorderService.ACTION_PAUSE);
        startService(intent);
    }

    private void resumeScreenRecorder() {
        Intent intent = new Intent(ScreenCaptureActivity.this, ScreenRecorderService.class);

        intent.setAction(ScreenRecorderService.ACTION_RESUME);
        startService(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_SCREEN_CAPTURE_CODE == requestCode) {
            if (resultCode != Activity.RESULT_OK) {
                //no permission
                Toast.makeText(this, "没有拿到录屏权限", Toast.LENGTH_SHORT).show();
                this.finish();
                return;
            }
            startScreenRecorder(resultCode, data);
        }
    }

    private static final class ScreenCaptureReceiver extends BroadcastReceiver {
        private final WeakReference<ScreenCaptureActivity> _weakParent;

        public ScreenCaptureReceiver(ScreenCaptureActivity parent) {
            _weakParent = new WeakReference<ScreenCaptureActivity>(parent);
        }

        boolean _isOk = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (ScreenRecorderService.ACTION_QUERY_STATUS_RESULT.equals(action)) {
                final boolean isRecording = intent.getBooleanExtra
                        (ScreenRecorderService.EXTRA_QUERY_RESULT_RECORDING, false);
                final boolean isPausing = intent.getBooleanExtra
                        (ScreenRecorderService.EXTRA_QUERY_RESULT_PAUSING, false);
                final ScreenCaptureActivity parent = _weakParent.get();
                if (parent != null) {
//                        parent.updateRecording(isRecording, isPausing);
                    if (isRecording && !isPausing) {

                        _weakParent.get().changeState(CaptureStatus.STATUS_RECORDING);

                        parent.changeState(CaptureStatus.STATUS_RECORDING);
                        if (_isOk)
                            return;
                        _isOk = true;
                    } else if (!isRecording && !isPausing) {
                        if (parent._state == CaptureStatus.STATUS_IDLE)
                            return;
                        _weakParent.get().changeState(CaptureStatus.STATUS_STOPPED);

                    } else if (isPausing) {
                        _weakParent.get().changeState(CaptureStatus.STATUS_PAUSING);
                    }
                }
            }
        }
    }

    public void changeState(CaptureStatus state) {
        switch (state) {
            case STATUS_IDLE:
                _state = CaptureStatus.STATUS_IDLE;
                _startBtn.setVisibility(View.VISIBLE);
                _btnGroup.setVisibility(View.INVISIBLE);
                break;
            case STATUS_RECORDING:
                _state = CaptureStatus.STATUS_RECORDING;
                _startBtn.setVisibility(View.INVISIBLE);
                _btnGroup.setVisibility(View.VISIBLE);
                _pauseBtn.setBackgroundResource(R.drawable.btn_pause);
                break;
            case STATUS_PAUSING:
                _state = CaptureStatus.STATUS_PAUSING;
                _startBtn.setVisibility(View.INVISIBLE);
                _btnGroup.setVisibility(View.VISIBLE);
                _pauseBtn.setBackgroundResource(R.drawable.btn_start);
                break;
            case STATUS_STOPPED:
                _state = CaptureStatus.STATUS_STOPPED;
                _startBtn.setVisibility(View.VISIBLE);
                _btnGroup.setVisibility(View.INVISIBLE);
                break;
        }
    }

    private enum CaptureStatus {
        STATUS_IDLE,
        STATUS_RECORDING,
        STATUS_PAUSING,
        STATUS_STOPPED
    }
}
