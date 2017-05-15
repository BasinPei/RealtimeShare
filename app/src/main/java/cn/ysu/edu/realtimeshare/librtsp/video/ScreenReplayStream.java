package cn.ysu.edu.realtimeshare.librtsp.video;

import android.media.MediaCodec;
import android.media.audiofx.EnvironmentalReverb;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import cn.ysu.edu.realtimeshare.librtsp.exceptions.CameraInUseException;
import cn.ysu.edu.realtimeshare.librtsp.exceptions.InvalidSurfaceException;
import cn.ysu.edu.realtimeshare.librtsp.gl.SurfaceView;
import cn.ysu.edu.realtimeshare.librtsp.mp4.MP4Config;
import cn.ysu.edu.realtimeshare.librtsp.rtp.H264Packetizer;
import cn.ysu.edu.realtimeshare.librtsp.rtp.MediaCodecInputStream;
import cn.ysu.edu.realtimeshare.screenreplay.media.MediaEncoder;
import cn.ysu.edu.realtimeshare.screenreplay.media.MediaMuxerWrapper;
import cn.ysu.edu.realtimeshare.screenreplay.media.MediaScreenEncoder;

/**
 * Created by KerriGan on 2016/5/31.
 */
public class ScreenReplayStream extends VideoStream implements MediaEncoder.MediaEncoderListener{

    private static String TAG="ScreenReplayStream";

    private MediaScreenEncoderAdapter _screenEncoderAdapter;
    private MediaMuxerWrapper _wrapper;
    private MediaProjection _mediaProjection;

    private MP4Config mConfig;

    private int _density;
    public ScreenReplayStream(MediaProjection mediaProjection,int width,int height,int density)
    {
        _mediaProjection=mediaProjection;
        mMode = MODE_MEDIACODEC_API;
        try {
            mPacketizer=new H264Packetizer();
            _wrapper=new MediaMuxerWrapper(".mp4");
            _screenEncoderAdapter =new MediaScreenEncoderAdapter(_wrapper,this,_mediaProjection
                    ,width,height,density);
            _density=density;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void start() throws IllegalStateException, IOException {
        if(!mStreaming)
        {
            if(!_wrapper.isStarted())
            {
                _wrapper.prepare();
                _wrapper.startRecording();
            }
            configure();
            mMode=MODE_MEDIACODEC_API;

            byte[] pps = Base64.decode(mConfig.getB64PPS(), Base64.NO_WRAP);
            byte[] sps = Base64.decode(mConfig.getB64SPS(), Base64.NO_WRAP);
            /**We should mark this,or @link getSessionDescription() ,others video stream should be error.*/
            ((H264Packetizer)mPacketizer).setStreamParameters(pps, sps);
            super.start();
        }
    }

    @Override
    public synchronized void stop() {
        if(_wrapper.isStarted())
        {
            _wrapper.stopRecording();
            _wrapper=null;
        }

        //ignore super.stop(); can use reflection?
        //super.stop();
        if (mStreaming) {
            try {
                mPacketizer.stop();
//                mMediaCodec.stop();
//                mMediaCodec.release();
//                mMediaCodec = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            mStreaming = false;
        }
        File f=new File(_screenEncoderAdapter.getOutputPath());
        if(f.exists())
            f.delete();
    }

    @Deprecated
    @Override
    protected void encodeWithMediaRecorder() throws IOException {
        //do nothing
        //goto encodeWithMediaCodec
        encodeWithMediaCodec();
    }

    @Override
    protected void encodeWithMediaCodec() throws IOException {
        //get MediaCodec from MediaScreenEncoder
        mMediaCodec=_screenEncoderAdapter.getMediaCodec();

        //set packetizer
        mPacketizer.setInputStream(new MediaCodecInputStream(mMediaCodec));

        mPacketizer.start();

        mStreaming=true;
    }

    @Override
    public synchronized void configure() throws IllegalStateException, IOException {
        super.configure();
        mMode=MODE_MEDIACODEC_API;
        String path = Environment.getExternalStorageDirectory().getPath() + "/cn.ysu.edu.realtimeshare/ShareScreen/MP4Config.mp4";
        mConfig=new MP4Config(path);
//        mConfig=checkMP4Config();
    }

    @Override
    public String getSessionDescription() {
        if (mConfig == null) throw new IllegalStateException("You need to call configure() first !");
        return "m=video "+String.valueOf(getDestinationPorts()[0])+" RTP/AVP 96\r\n" +
                "a=rtpmap:96 H264/90000\r\n" +
                "a=fmtp:96 packetization-mode=1;" /*+"\r\n";*/
                + "profile-level-id="+mConfig.getProfileLevel()
                +";sprop-parameter-sets="+mConfig.getB64SPS()+"," //We should mark this,or ((H264Packetizer)mPacketizer).setStreamParameters(pps, sps),otherwise video stream may error.
                +mConfig.getB64PPS()+";\r\n";
    }

    @Override
    public void onPrepared(MediaEncoder encoder) {
        Log.i(TAG,"MediaEncoder prepared.");
    }

    @Override
    public void onStopped(MediaEncoder encoder) {
        Log.i(TAG,"MediaEncoder stopped.");
    }

    private static class MediaScreenEncoderAdapter extends MediaScreenEncoder
    {

        public MediaScreenEncoderAdapter(MediaMuxerWrapper muxer, MediaEncoderListener listener, MediaProjection projection, int width, int height, int density) {
            super(muxer, listener, projection, width, height, density);
        }

        @Override
        protected void drain() {
            //do nothing
            //intercept buffer,the buffer will feed to MediaStream
        }

        public MediaCodec getMediaCodec()
        {
            return mMediaCodec;
        }

        public int getWidth()
        {
            return mWidth;
        }

        public int getHeight()
        {
            return mHeight;
        }

    }


    //intercept VideoStream method
    @Override
    protected synchronized void createCamera() throws RuntimeException {
        //do nothing
    }

    @Override
    protected synchronized void destroyCamera() {
        //do nothing
    }

    @Override
    public int getCamera() {
        //do nothing
        return -1;
    }

    @Override
    protected void lockCamera() {
        //do nothing
    }

    @Override
    public void setCamera(int camera) {
        //do nothing
    }

    @Override
    public void setPreviewOrientation(int orientation) {
        //do nothing
    }

    @Override
    public synchronized void setSurfaceView(SurfaceView view) {
        //do nothing
    }

    @Override
    public synchronized void startPreview() throws CameraInUseException, InvalidSurfaceException, RuntimeException {
        //do nothing
    }

    @Override
    public synchronized void stopPreview() {
        //do nothing
    }

    @Override
    public void switchCamera() throws RuntimeException, IOException {
        //do nothing
    }

    @Override
    protected void unlockCamera() {
        //do nothing
    }

    @Override
    protected synchronized void updateCamera() throws RuntimeException {
        //do nothing
    }
}
