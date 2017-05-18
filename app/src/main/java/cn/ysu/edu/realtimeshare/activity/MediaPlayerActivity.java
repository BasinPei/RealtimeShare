package cn.ysu.edu.realtimeshare.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.httpserver.http.Parameter;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.widget.MediaController;
import jcifs.smb.SmbException;


public class MediaPlayerActivity extends ActionBarActivity {
    private SurfaceView _surfaceView;
    private SurfaceHolder _surfaceHolder;
    private MediaPlayer _mediaPlayer;
    private String _videoPath;
    private MediaController _mediaController;
    private VolumeReceiver _volumeReceiver;
    private PlayerControl _playerControl = new PlayerControl();

    /**
     * add in 2017/5/4 fixed crash bug, by BasinPei
     */
    private boolean _isBufferFinish = false;
    private ProgressDialog _progressDlg = null;


    public static final int LOCAL_VIDEO = 0x001;
    public static final int ONLINE_VIDEO = 0x002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.getRequestedOrientation() !=
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            return;
        }

        if (getSupportActionBar() != null)
            getSupportActionBar().hide();

        if (!LibsChecker.checkVitamioLibs(this))
            return;
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                , WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //隐藏状态栏
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window = MediaPlayerActivity.this.getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        setContentView(R.layout.activity_media_player);

        Intent paramIntent = getIntent();
        if(paramIntent != null){
            _videoPath = paramIntent.getStringExtra("path");
        }

        if (_videoPath == null){
            Toast.makeText(this, "视频路径无效", Toast.LENGTH_LONG).show();
            finish();
        }


        _surfaceView = (SurfaceView) findViewById(R.id.surface_view);
        _surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!_isBufferFinish)
                    return;

                if (_mediaController.isShowing())
                    _mediaController.hide();
                else
                    _mediaController.show(3000);
            }
        });

        _surfaceHolder = _surfaceView.getHolder();

        _surfaceHolder.setFormat(PixelFormat.RGBA_8888);
        _surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                playVideo();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        _mediaController = new MediaController(this);
        _mediaController.setEnabled(true);

        _mediaController.setAnchorView(_surfaceView);
        _mediaController.setMediaPlayer(_playerControl);

        //register broadcast
        _volumeReceiver = new VolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        this.registerReceiver(_volumeReceiver, filter);
    }

    public void playVideo() {
        doCleanUp();

        _mediaPlayer = new MediaPlayer(this);
        _mediaPlayer.setDisplay(_surfaceHolder);
        try {

            _mediaPlayer.setDataSource(this, Uri.parse(_videoPath));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (checkOnlineVideo(_videoPath)) {
                        _mediaPlayer.prepareAsync();
                    } else {
                        MediaPlayerActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MediaPlayerActivity.this,
                                        "视频不存在哦哦哦！！", Toast.LENGTH_LONG).show();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Thread.sleep(3000);
                                            MediaPlayerActivity.this.finish();
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                            }
                        });
                    }
                }
            }).start();

            _progressDlg = new ProgressDialog(MediaPlayerActivity.this);
            _progressDlg.setMessage(getResources().getString(R.string.search_loading));
            _progressDlg.setCanceledOnTouchOutside(false);
            _progressDlg.show();
            _mediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {

                    /**
                     *  fixed in 2017/5/4 crash bug,by BasinPei
                     */
                    if (percent > 0.1) {
                        _progressDlg.dismiss();
                        if (!_isBufferFinish)
                            _isBufferFinish = true;
                    }
                }
            });
            _mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    MediaPlayerActivity.this.finish();
                }
            });
            _mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    _isVideoReadyToBePlayed = true;
                    if (_isVideoReadyToBePlayed && _isVideoSizeKnown)
                        startVideoPlayBack();
                }
            });
            _mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                @Override
                public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                    _isVideoSizeKnown = true;
                    _videoWidth = width;
                    _videoHeight = height;
                    if (_isVideoSizeKnown && _isVideoReadyToBePlayed)
                        startVideoPlayBack();
                }
            });
            _mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    _isSeeking = false;
                }
            });

//            _mediaPlayer.getMetadata();
            setVolumeControlStream(AudioManager.STREAM_MUSIC);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void doCleanUp() {
        _videoWidth = 0;
        _videoHeight = 0;
        _isVideoReadyToBePlayed = false;
        _isVideoSizeKnown = false;
        _isSeeking = false;
        _currentPosition = -1;
    }

    private int _videoWidth;
    private int _videoHeight;
    private boolean _isVideoReadyToBePlayed = false;
    private boolean _isVideoSizeKnown = false;
    private long _beginSysTime;

    protected void startVideoPlayBack() {
        _surfaceHolder.setFixedSize(_videoWidth, _videoHeight);
        _mediaPlayer.start();
        _mediaController.setFileName(_videoPath);

        _beginSysTime = System.currentTimeMillis();
        _handlerInterval.sendEmptyMessageDelayed(1, 1000);
    }

    protected void releaseBuffer() {
        if (_mediaPlayer != null) {
            _mediaPlayer.release();
            _mediaPlayer = null;
        }
    }

    @Override
    protected void onDestroy() {
        if (_mediaController != null)
            _mediaController.hide();
        super.onDestroy();
        releaseBuffer();
        doCleanUp();

        if (_volumeReceiver != null)
            unregisterReceiver(_volumeReceiver);

        _isExit = true;
    }


    //Broad Cast

    protected class VolumeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
//                AudioManager manager= (AudioManager)
//                        context.getSystemService(Context.AUDIO_SERVICE);
//                int volume=manager.getStreamVolume(AudioManager.STREAM_MUSIC);
//                manager.setStreamVolume(AudioManager.STREAM_MUSIC,volume,AudioManager.FLAG_PLAY_SOUND);
//                _mediaPlayer.setVolume(volume, volume);
            }
        }
    }

    private int _firstTouchX = -1;
    private boolean _isSeeking = false;
    private long _currentPosition = -1;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        if (ev.getAction() == MotionEvent.ACTION_DOWN && _firstTouchX == -1) {
            _firstTouchX = (int) ev.getX();
        } else if (ev.getAction() == MotionEvent.ACTION_UP) {
            boolean temp = _isSeeking;
            _firstTouchX = -1;
//            _isSeeking=false;
            _currentPosition = -1;
            if (temp && _mediaController.isShowing()) {
                return true;
            }
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE && ev.getHistorySize() > 1) {
            float offSet = ev.getX() - _firstTouchX;
            synchronized (this) {
                if (!_isSeeking && _isBufferFinish) {
                    System.out.println("OFFSET:" + offSet);
                    changeVideoPos(offSet);
                }
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    private boolean _isExit = false;
    private Handler _handlerInterval = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0 && _mediaPlayer != null) {
                _beginSysTime = (System.currentTimeMillis() -
                        _mediaPlayer.getCurrentPosition());
                return;
            }

            if (msg.what != 1 || _mediaPlayer == null)
                return;
//            System.out.println("play time:" + (System.currentTimeMillis() - _beginSysTime));
//            _mediaController.setFileName(String.valueOf(System.currentTimeMillis() - _beginSysTime));
            long dis = Math.abs(_mediaPlayer.getCurrentPosition()
                    - System.currentTimeMillis() - _beginSysTime);
            if (dis >= 1200 && _firstTouchX == -1) {
                _handlerInterval.sendEmptyMessageDelayed(0, 5000);
            }

            if (!_isExit)
                _handlerInterval.sendEmptyMessageDelayed(1, 1000);
        }
    };

    protected synchronized void changeVideoPos(float offSet) {
//        _isSeeking=true;
//        if(_beginPos<_currentPosition-500)
//            _currentPosition= (long) (_currentPosition+offSet/50*1000);
//        else
//            _currentPosition=(long) (_beginPos+offSet/50*1000);
//        _mediaPlayer.seekTo
//                (_currentPosition);
        if (offSet >= 50) {
            _isSeeking = true;
            _playerControl.seekTo((System.currentTimeMillis() - _beginSysTime + 10000));
            _beginSysTime -= 10000;
        } else if (offSet <= -50) {
            _isSeeking = true;
            _playerControl.seekTo((System.currentTimeMillis() - _beginSysTime - 10000));
            _beginSysTime += 10000;
        }
//        _handlerInterval.sendEmptyMessageDelayed(1, 100);
    }

    /**
     * must be used in child thread
     */
    public boolean checkOnlineVideo(String path) {
        URL u;
        String path2 = new String(path);
        HttpURLConnection connection = null;
        try {
            u = new URL(path2);
            connection = (HttpURLConnection) u.openConnection();
            connection.setConnectTimeout(6 * 1000);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.connect();
            InputStream i = connection.getInputStream();
            i.close();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(connection != null){
                connection.disconnect();
            }
        }
        return true;
    }

    public class PlayerControl implements MediaController.MediaPlayerControl {
        @Override
        public void start() {
            if (!_mediaPlayer.isPlaying())
                _mediaPlayer.start();
        }

        @Override
        public void pause() {
            if (_mediaPlayer.isPlaying())
                _mediaPlayer.pause();
        }

        @Override
        public long getDuration() {
            if (_mediaPlayer == null)
                return 0;
            return _mediaPlayer.getDuration();
        }

        @Override
        public long getCurrentPosition() {
            if (_mediaPlayer == null)
                return 0;

            if (_currentPosition != -1)
                return _currentPosition;
            return _mediaPlayer.getCurrentPosition();

        }

        @Override
        public void seekTo(long pos) {
            if (_mediaPlayer == null)
                return;
//                AudioManager manager = (AudioManager)
//                        MediaPlayerActivity.this.getSystemService(Context.AUDIO_SERVICE);
//                int currentVolume = manager.getStreamVolume(AudioManager.
//                        STREAM_MUSIC);
//                System.out.println("Voice:" + currentVolume);
//                if (currentVolume - 0 <= 0.0000001)
//                    _mediaPlayer.setVolume(0, 0);

            _mediaPlayer.seekTo(pos);
        }

        @Override
        public boolean isPlaying() {
            if (_mediaPlayer == null)
                return false;
            return _mediaPlayer.isPlaying();
        }

        @Override
        public int getBufferPercentage() {
            if (_mediaPlayer == null)
                return 0;
            return _mediaPlayer.getBufferProgress();
        }
    }

}
