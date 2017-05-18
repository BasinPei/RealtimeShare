package cn.ysu.edu.realtimeshare.screenreplay.service;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.IOException;

import cn.ysu.edu.realtimeshare.screenreplay.media.MediaAudioEncoder;
import cn.ysu.edu.realtimeshare.screenreplay.media.MediaEncoder;
import cn.ysu.edu.realtimeshare.screenreplay.media.MediaMuxerWrapper;
import cn.ysu.edu.realtimeshare.screenreplay.media.MediaScreenEncoder;


/**
 * Created by BasinPei on 2017/5/27.
 */
public class ScreenRecorderService extends IntentService {
    private static final boolean DEBUG = false;
    private static final String TAG = "ScreenRecorderService";

    private static final String BASE = "cn.ysu.edu.realtimeshare.screenreplay.service.";
    public static final String ACTION_START = BASE + "ACTION_START";
    public static final String ACTION_STOP = BASE + "ACTION_STOP";
    public static final String ACTION_PAUSE = BASE + "ACTION_PAUSE";
    public static final String ACTION_RESUME = BASE + "ACTION_RESUME";
    public static final String ACTION_QUERY_STATUS = BASE + "ACTION_QUERY_STATUS";
    public static final String ACTION_QUERY_STATUS_RESULT = BASE + "ACTION_QUERY_STATUS_RESULT";
    public static final String EXTRA_RESULT_CODE = BASE + "EXTRA_RESULT_CODE";
    public static final String EXTRA_QUERY_RESULT_RECORDING = BASE + "EXTRA_QUERY_RESULT_RECORDING";
    public static final String EXTRA_QUERY_RESULT_PAUSING = BASE + "EXTRA_QUERY_RESULT_PAUSING";

    public static final String ACTION_QUERY_OUTPUT_PATH = BASE + "ACTION_QUERY_OUTPUT_PATH";
    public static final String ACTION_QUERY_OUTPUT_PATH_RESULT = BASE + "ACTION_QUERY_OUTPUT_PATH_RESULT";

    public static final String ACTION_SET_OUTPUT_PATH = BASE + "ACTION_SET_OUTPUT_PATH";
    public static final String ACTION_RESTART_WITH_NEW_FILE = BASE + "ACTION_RESTART_WITH_NEW_FILE";

    public static final String EXTRA_QUERY_OUTPUT_PATH = BASE + "EXTRA_QUERY_OUTPUT_PATH";

    public static final String EXTRA_SET_OUTPUT_PATH = BASE + "EXTRA_SET_OUTPUT_PATH";
    public static final String EXTRA_RESTART_WITH_NEW_FILE = BASE + "EXTRA_RESTART_WITH_NEW_FILE";

    public static final String ACTION_RELEASE_RESOURCE = BASE + "ACTION_RELEASE_RESOURCE";

    private static Object sSync = new Object();
    private static MediaMuxerWrapper sMuxer;

    private MediaProjectionManager mMediaProjectionManager;

    public ScreenRecorderService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.v(TAG, "onCreate:");
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        if (DEBUG) Log.v(TAG, "onHandleIntent:intent=" + intent);
        final String action = intent.getAction();
        if (ACTION_START.equals(action)) {
            startScreenRecord(intent);
            updateStatus();
        } else if (ACTION_STOP.equals(action)) {
            stopScreenRecord();
            updateStatus();
        } else if (ACTION_QUERY_STATUS.equals(action)) {
            updateStatus();
        } else if (ACTION_PAUSE.equals(action)) {
            pauseScreenRecord();
            updateStatus();
        } else if (ACTION_RESUME.equals(action)) {
            resumeScreenRecord();
            updateStatus();
        } else if (ACTION_QUERY_OUTPUT_PATH.equals(action)) {
            getOutputPath();
        } else if (ACTION_SET_OUTPUT_PATH.equals(action)) {
            String path = intent.getStringExtra(EXTRA_SET_OUTPUT_PATH);
            setOutputPath(path);
        } else if (ACTION_RESTART_WITH_NEW_FILE.equals(action)) {
            String path = intent.getStringExtra(EXTRA_RESTART_WITH_NEW_FILE);
            restartWithNewFile(path);
        } else if (ACTION_RELEASE_RESOURCE.equals(action)) {
            releaseResource();
        }
    }

    private void updateStatus() {
        final boolean isRecording, isPausing;
        synchronized (sSync) {
            isRecording = (sMuxer != null);
            isPausing = isRecording ? sMuxer.isPaused() : false;
        }
        final Intent result = new Intent();
        result.setAction(ACTION_QUERY_STATUS_RESULT);
        result.putExtra(EXTRA_QUERY_RESULT_RECORDING, isRecording);
        result.putExtra(EXTRA_QUERY_RESULT_PAUSING, isPausing);
        if (DEBUG)
            Log.v(TAG, "sendBroadcast:isRecording=" + isRecording + ",isPausing=" + isPausing);
        sendBroadcast(result);
    }

    /**
     * start screen recording as .mp4 file
     *
     * @param intent
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startScreenRecord(final Intent intent) {
        if (DEBUG) Log.v(TAG, "startScreenRecord:sMuxer=" + sMuxer);
        synchronized (sSync) {
            if (sMuxer == null) {
                final int resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, 0);
                // get MediaProjection
                final MediaProjection projection = mMediaProjectionManager.getMediaProjection(resultCode, intent);
                if (projection != null) {
                    final DisplayMetrics metrics = getResources().getDisplayMetrics();
                    final int density = metrics.densityDpi;

                    _mediaProjection = projection;

                    if (DEBUG) Log.v(TAG, "startRecording:");
                    try {
                        sMuxer = new MediaMuxerWrapper(".mp4"); // if you record audio only, ".m4a" is also OK.


                        if (_outputPath != null)
                            sMuxer.setOutputPath(_outputPath, true);

                        _outputPath = null;

                        if (true) {
                            // for screen capturing
                            new MediaScreenEncoder(sMuxer, mMediaEncoderListener,
                                    projection, metrics.widthPixels, metrics.heightPixels, density);
                        }
                        if (true) {
                            // for audio capturing
                            new MediaAudioEncoder(sMuxer, mMediaEncoderListener);
                        }
                        sMuxer.prepare();
                        sMuxer.startRecording();
                    } catch (final IOException e) {
                        Log.e(TAG, "startScreenRecord:", e);
                    }
                }
            }
        }
    }

    /**
     * stop screen recording
     */
    private void stopScreenRecord() {
        if (DEBUG) Log.v(TAG, "stopScreenRecord:sMuxer=" + sMuxer);
        synchronized (sSync) {
            if (sMuxer != null) {
                sMuxer.stopRecording();
                sMuxer = null;
                // you should not wait here
            }
        }
    }

    private void pauseScreenRecord() {
        synchronized (sSync) {
            if (sMuxer != null) {
                sMuxer.pauseRecording();
            }
        }
    }

    private void resumeScreenRecord() {
        synchronized (sSync) {
            if (sMuxer != null) {
                sMuxer.resumeRecording();
            }
        }
    }

    /**
     * callback methods from encoder
     */
    private static final MediaEncoder.MediaEncoderListener mMediaEncoderListener = new MediaEncoder.MediaEncoderListener() {
        @Override
        public void onPrepared(final MediaEncoder encoder) {
            if (DEBUG) Log.v(TAG, "onPrepared:encoder=" + encoder);
        }

        @Override
        public void onStopped(final MediaEncoder encoder) {
            if (DEBUG) Log.v(TAG, "onStopped:encoder=" + encoder);
        }
    };

    private void getOutputPath() {
        String path = sMuxer.getOutputPath();
        Intent i = new Intent(ACTION_QUERY_OUTPUT_PATH_RESULT);
        i.putExtra(EXTRA_QUERY_OUTPUT_PATH, path);
        sendBroadcast(i);
    }

    private static String _outputPath = null;

    private void setOutputPath(String path) {
        _outputPath = path;
    }


    private static MediaProjection _mediaProjection;

    private void restartWithNewFile(String path) {
        try {
            setOutputPath(path);

            sMuxer = new MediaMuxerWrapper(".mp4");

            sMuxer.setOutputPath(path, true);
            final DisplayMetrics metrics = getResources().getDisplayMetrics();
            final int density = metrics.densityDpi;

            if (true) {
                // for screen capturing
                new MediaScreenEncoder(sMuxer, mMediaEncoderListener,
                        _mediaProjection, metrics.widthPixels, metrics.heightPixels, density);
            }
            if (true) {
                // for audio capturing
                new MediaAudioEncoder(sMuxer, mMediaEncoderListener);
            }
            sMuxer.prepare();
            sMuxer.startRecording();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void releaseResource() {
        if (_mediaProjection != null) {
            _mediaProjection.stop();
            _mediaProjection = null;
        }
    }
}
