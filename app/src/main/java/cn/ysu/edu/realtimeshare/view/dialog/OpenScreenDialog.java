package cn.ysu.edu.realtimeshare.view.dialog;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.widget.CheckBox;

import cn.ysu.edu.realtimeshare.R;
import cn.ysu.edu.realtimeshare.librtsp.PreferenceInfo;
import cn.ysu.edu.realtimeshare.librtsp.SessionBuilder;
import cn.ysu.edu.realtimeshare.librtsp.audio.AudioQuality;
import cn.ysu.edu.realtimeshare.librtsp.video.VideoQuality;


/**
 * Created by KerriGan on 2016/6/12.
 */
public class OpenScreenDialog extends BaseDialog{
    public OpenScreenDialog(Context context) {
        super(context);

        VideoQuality videoQuality = VideoQuality.DEFAULT_VIDEO_QUALITY.clone();
        videoQuality.resX= 1280;
        videoQuality.resY= 720;
        videoQuality._density= 1;

        AudioQuality audioQuality = AudioQuality.DEFAULT_AUDIO_QUALITY.clone();
        audioQuality.bitRate = 64000;
        audioQuality.samplingRate = 44100;

        SessionBuilder.getInstance()
                .setContext(_context)
                .setVideoQuality(videoQuality)
                .setAudioQuality(audioQuality)
                .setVideoEncoder(SessionBuilder.VIDEO_NONE)
                .setAudioEncoder(SessionBuilder.AUDIO_NONE);
    }

    private boolean _isOpen=false;

    private MediaProjectionManager _mediaProjectionManager;

    private CheckBox _checkBoxVideo;
    private CheckBox _checkBoxAudio;
    @Override
    protected void init() {
        super.init();

        _build.setTitle("需要获得屏幕录制权限");

        _contentView= LayoutInflater.from(_context).inflate(R.layout.dialog_open_screen,null);

        _build.setView(_contentView);

        _mediaProjectionManager= (MediaProjectionManager) _context.
                getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        _build.setPositiveButton("打开", new DialogInterface.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _isOpen=true;
                Intent i=_mediaProjectionManager.createScreenCaptureIntent();
                ((Activity)_context).startActivityForResult(i, 1);
            }
        });

        _build.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                _isOpen = false;
            }
        });

        createDialog();

        _dialog.setCanceledOnTouchOutside(true);

        _checkBoxVideo= (CheckBox) _contentView.findViewById(R.id.check_box_video);
        _checkBoxAudio= (CheckBox) _contentView.findViewById(R.id.check_box_audio);
    }

    public boolean isOpen()
    {
        return _isOpen;
    }

    private MediaProjection _mediaProjection;
    private ProgressDialog _progressDialog;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode==1&&resultCode==Activity.RESULT_OK)
        {
            if(_checkBoxVideo.isChecked())
                SessionBuilder.getInstance().setVideoEncoder(SessionBuilder.VIDEO_SCREEN);
            if(_checkBoxAudio.isChecked())
                SessionBuilder.getInstance().setAudioEncoder(SessionBuilder.AUDIO_AAC);

            _mediaProjection=_mediaProjectionManager.getMediaProjection(
                    resultCode,data);

            SessionBuilder.getInstance().setMediaProjection(_mediaProjection);


            _progressDialog=new ProgressDialog(_context);
            _progressDialog.setMessage("正在准备");
            _progressDialog.show();
            _progressDialog.setCancelable(false);

            SessionBuilder.checkMP4Config(_context,
                    SessionBuilder.getInstance().getVideoQuality().resX,
                    SessionBuilder.getInstance().getVideoQuality().resY,
                    _mediaProjection,
                    SessionBuilder.getInstance().getVideoQuality()._density,
                    new SessionBuilder.ICheck() {
                        @Override
                        public void finish(boolean success) {
                            final boolean localSuccess = success;
                            ((Activity) _context).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (_progressDialog != null && _progressDialog.isShowing())
                                        _progressDialog.dismiss();

                                    SharedPreferences pref = PreferenceManager.
                                            getDefaultSharedPreferences(_context);
                                    SharedPreferences.Editor edit = pref.edit();
                                    if (localSuccess) {
                                        edit.putBoolean(PreferenceInfo.PREF_MP4CONFIG_SUCCESS,
                                                true);
                                    } else {
                                        edit.putBoolean(PreferenceInfo.PREF_MP4CONFIG_SUCCESS,
                                                false);
                                    }
                                    edit.commit();
                                }
                            });
                        }
                    });
        }else
            _isOpen=false;
    }

    public MediaProjection getMediaProjection()
    {
        return _mediaProjection;
    }
}
