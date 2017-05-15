/*
 * Copyright (C) 2011-2014 GUIGUI Simon, fyhertz@gmail.com
 * 
 * This file is part of libstreaming (https://github.com/fyhertz/libstreaming)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package cn.ysu.edu.realtimeshare.librtsp;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Camera.CameraInfo;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;


import java.io.File;
import java.io.IOException;

import cn.ysu.edu.realtimeshare.librtsp.audio.AACStream;
import cn.ysu.edu.realtimeshare.librtsp.audio.AMRNBStream;
import cn.ysu.edu.realtimeshare.librtsp.audio.AudioQuality;
import cn.ysu.edu.realtimeshare.librtsp.audio.AudioStream;
import cn.ysu.edu.realtimeshare.librtsp.gl.SurfaceView;
import cn.ysu.edu.realtimeshare.librtsp.video.H263Stream;
import cn.ysu.edu.realtimeshare.librtsp.video.H264Stream;
import cn.ysu.edu.realtimeshare.librtsp.video.ScreenReplayStream;
import cn.ysu.edu.realtimeshare.librtsp.video.VideoQuality;
import cn.ysu.edu.realtimeshare.librtsp.video.VideoStream;
import cn.ysu.edu.realtimeshare.screenreplay.media.MediaEncoder;
import cn.ysu.edu.realtimeshare.screenreplay.media.MediaMuxerWrapper;
import cn.ysu.edu.realtimeshare.screenreplay.media.MediaScreenEncoder;

/**
 * Call {@link #getInstance()} to get access to the SessionBuilder.
 */
public class SessionBuilder {

	public final static String TAG = "SessionBuilder";

	/** Can be used with {@link #setVideoEncoder}. */
	public final static int VIDEO_NONE = 0;

	/** Can be used with {@link #setVideoEncoder}. */
	public final static int VIDEO_H264 = 1;

	/** Can be used with {@link #setVideoEncoder}. */
	public final static int VIDEO_H263 = 2;

	/** Customize stream to streaming screen stream */
	public final static int VIDEO_SCREEN = 3;

	/** Can be used with {@link #setAudioEncoder}. */
	public final static int AUDIO_NONE = 0;

	/** Can be used with {@link #setAudioEncoder}. */
	public final static int AUDIO_AMRNB = 3;

	/** Can be used with {@link #setAudioEncoder}. */
	public final static int AUDIO_AAC = 5;

	// Default configuration
	private VideoQuality mVideoQuality = VideoQuality.DEFAULT_VIDEO_QUALITY;
	private AudioQuality mAudioQuality = AudioQuality.DEFAULT_AUDIO_QUALITY;
	private Context mContext;
	private int mVideoEncoder = VIDEO_H263; 
	private int mAudioEncoder = AUDIO_AMRNB;
	private int mCamera = CameraInfo.CAMERA_FACING_BACK;
	private int mTimeToLive = 64;
	private int mOrientation = 0;
	private boolean mFlash = false;
	private cn.ysu.edu.realtimeshare.librtsp.gl.SurfaceView mSurfaceView = null;
	private String mOrigin = null;
	private String mDestination = null;
	private Session.Callback mCallback = null;

	// Removes the default public constructor
	private SessionBuilder() {}

	// The SessionManager implements the singleton pattern
	private static volatile SessionBuilder sInstance = null;

	/**
	 * Returns a reference to the {@ librtsp.SessionBuilder}.
	 * @return The reference to the {@ librtsp.SessionBuilder}
	 */
	public final static SessionBuilder getInstance() {
		if (sInstance == null) {
			synchronized (SessionBuilder.class) {
				if (sInstance == null) {
					SessionBuilder.sInstance = new SessionBuilder();
				}
			}
		}
		return sInstance;
	}

	/**
	 * Creates a new {@link Session}.
	 * @return The new Session
	 * @throws IOException
	 */
	public Session build() {
		Session session;

		session = new Session();
		session.setOrigin(mOrigin);
		session.setDestination(mDestination);
		session.setTimeToLive(mTimeToLive);
		session.setCallback(mCallback);

		switch (mAudioEncoder) {
		case AUDIO_AAC:
			AACStream stream = new AACStream();
			session.addAudioTrack(stream);
			if (mContext!=null)
				stream.setPreferences(PreferenceManager.getDefaultSharedPreferences(mContext));
			break;
		case AUDIO_AMRNB:
			session.addAudioTrack(new AMRNBStream());
			break;
		}

		switch (mVideoEncoder) {
		case VIDEO_H263:
			session.addVideoTrack(new H263Stream(mCamera));
			break;
		case VIDEO_H264:
			H264Stream stream = new H264Stream(mCamera);
			if (mContext!=null)
				stream.setPreferences(PreferenceManager.getDefaultSharedPreferences(mContext));
			session.addVideoTrack(stream);
			break;
		case VIDEO_SCREEN:
			ScreenReplayStream stream2=new ScreenReplayStream(_mediaProjection,mVideoQuality.resX
					,mVideoQuality.resY,1);
			if(mContext!=null)
				stream2.setPreferences(PreferenceManager.getDefaultSharedPreferences(mContext));
			session.addVideoTrack(stream2);
			break;
		}

		if (session.getVideoTrack()!=null) {
			VideoStream video = session.getVideoTrack();
			video.setVideoQuality(mVideoQuality);
			if(mSurfaceView!=null)
			{
				video.setFlashState(mFlash);
				video.setSurfaceView(mSurfaceView);
				video.setPreviewOrientation(mOrientation);
			}
			video.setDestinationPorts(5006);
		}

		if (session.getAudioTrack()!=null) {
			AudioStream audio = session.getAudioTrack();
			audio.setAudioQuality(mAudioQuality);
			audio.setDestinationPorts(5004);
		}

		return session;

	}

	/**
	 * Access to the context is needed for the H264Stream class to store some stuff in the SharedPreferences.
	 * Note that you should pass the Application context, not the context of an Activity.
	 **/
	public SessionBuilder setContext(Context context) {
		mContext = context;
		return this;
	}

	/** Sets the destination of the session. */
	public SessionBuilder setDestination(String destination) {
		mDestination = destination;
		return this;
	}

	/** Sets the origin of the session. It appears in the SDP of the session. */
	public SessionBuilder setOrigin(String origin) {
		mOrigin = origin;
		return this;
	}

	/** Sets the video stream quality. */
	public SessionBuilder setVideoQuality(VideoQuality quality) {
		mVideoQuality = quality.clone();
		return this;
	}

	/** Sets the audio encoder. */
	public SessionBuilder setAudioEncoder(int encoder) {
		mAudioEncoder = encoder;
		return this;
	}

	/** Sets the audio quality. */
	public SessionBuilder setAudioQuality(AudioQuality quality) {
		mAudioQuality = quality.clone();
		return this;
	}

	/** Sets the default video encoder. */
	public SessionBuilder setVideoEncoder(int encoder) {
		mVideoEncoder = encoder;
		return this;
	}

	public SessionBuilder setFlashEnabled(boolean enabled) {
		mFlash = enabled;
		return this;
	}

	public SessionBuilder setCamera(int camera) {
		mCamera = camera;
		return this;
	}

	public SessionBuilder setTimeToLive(int ttl) {
		mTimeToLive = ttl;
		return this;
	}

	/**
	 * Sets the SurfaceView required to preview the video stream.
	 **/
	public SessionBuilder setSurfaceView(SurfaceView surfaceView) {
		mSurfaceView = surfaceView;
		return this;
	}

	/**
	 * Sets the orientation of the preview.
	 * @param orientation The orientation of the preview
	 */
	public SessionBuilder setPreviewOrientation(int orientation) {
		mOrientation = orientation;
		return this;
	}

	public SessionBuilder setCallback(Session.Callback callback) {
		mCallback = callback;
		return this;
	}

	/** Returns the context set with {@link #setContext(Context)}*/
	public Context getContext() {
		return mContext;
	}

	/** Returns the destination ip address set with {@link #setDestination(String)}. */
	public String getDestination() {
		return mDestination;
	}

	/** Returns the origin ip address set with {@link #setOrigin(String)}. */
	public String getOrigin() {
		return mOrigin;
	}

	/** Returns the audio encoder set with {@link #setAudioEncoder(int)}. */
	public int getAudioEncoder() {
		return mAudioEncoder;
	}

	/** Returns the id of the {@link android.hardware.Camera} set with {@link #setCamera(int)}. */
	public int getCamera() {
		return mCamera;
	}

	/** Returns the video encoder set with {@link #setVideoEncoder(int)}. */
	public int getVideoEncoder() {
		return mVideoEncoder;
	}

	/** Returns the VideoQuality set with {@link #setVideoQuality(VideoQuality)}. */
	public VideoQuality getVideoQuality() {
		return mVideoQuality;
	}

	/** Returns the AudioQuality set with {@link #setAudioQuality(AudioQuality)}. */
	public AudioQuality getAudioQuality() {
		return mAudioQuality;
	}

	/** Returns the flash state set with {@link #setFlashEnabled(boolean)}. */
	public boolean getFlashState() {
		return mFlash;
	}

	/** Returns the SurfaceView set with {@link #setSurfaceView(SurfaceView)}. */
	public SurfaceView getSurfaceView() {
		return mSurfaceView;
	}


	/** Returns the time to live set with {@link #setTimeToLive(int)}. */
	public int getTimeToLive() {
		return mTimeToLive;
	}

	/** Returns a new {@link .SessionBuilder} with the same configuration. */
	public SessionBuilder clone() {
		return new SessionBuilder()
		.setDestination(mDestination)
		.setOrigin(mOrigin)
		.setSurfaceView(mSurfaceView)
		.setPreviewOrientation(mOrientation)
		.setVideoQuality(mVideoQuality)
		.setVideoEncoder(mVideoEncoder)
		.setFlashEnabled(mFlash)
		.setCamera(mCamera)
		.setTimeToLive(mTimeToLive)
		.setAudioEncoder(mAudioEncoder)
		.setAudioQuality(mAudioQuality)
		.setContext(mContext)
		.setCallback(mCallback)
		/**Customize element,add in 2016/6/3 by KerriGan*/
		.setMediaProjection(_mediaProjection);
	}

	/**Customize element */
	private MediaProjection _mediaProjection;

	public SessionBuilder setMediaProjection(MediaProjection projection)
	{
		_mediaProjection=projection;
		return this;
	}

	public MediaProjection getMediaProjection()
	{
		return _mediaProjection;
	}

	public static String MP4_CONFIG_PATH = Environment.getExternalStorageDirectory().getPath() + "/cn.ysu.edu.realtimeshare/ShareScreen";
	public static String MP4_CONFIG_NAME="MP4Config.mp4";
	public static void checkMP4Config(Context context, final int width, final int height,MediaProjection
									  mediaProjection,int density, final ICheck listener)
	{
		final SharedPreferences settings=PreferenceManager.getDefaultSharedPreferences(context);

		final boolean[] isCallBack=new boolean[]{false};


		File root=new File(MP4_CONFIG_PATH);
		if(!root.isDirectory())
			root.mkdirs();

		final File testFile=new File(MP4_CONFIG_PATH+"/"+MP4_CONFIG_NAME);

		int prevWidth=settings.getInt("screen_capture_width",0);
		int prevHeight=settings.getInt("screen_capture_height",0);

		if(prevWidth!=width ||
				prevHeight!=height||
				prevWidth==0||
				prevHeight==0)
		{
			if(testFile.exists())
				testFile.delete();
		}

		boolean isMP4ConfigSuccess=settings.getBoolean(PreferenceInfo.PREF_MP4CONFIG_SUCCESS,false);
		if(isMP4ConfigSuccess)
		{
			//do nothing
		}
		else
		{
			testFile.delete();
		}

		if(!testFile.exists())
		{
			try {
				final MediaMuxerWrapper testWrapper=new MediaMuxerWrapper(".mp4");
				new MediaScreenEncoder(testWrapper, new MediaEncoder.MediaEncoderListener() {
					@Override
					public void onPrepared(MediaEncoder encoder) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									synchronized (this)
									{
										this.wait(3000);
									}
									if(testWrapper.isStarted())
									{
										testWrapper.stopRecording();
									}
								} catch (InterruptedException e) {
									e.printStackTrace();
									if(!isCallBack[0])
									{
										isCallBack[0]=true;
										listener.finish(false);
									}
								}
							}
						}).start();
					}

					@Override
					public void onStopped(MediaEncoder encoder) {
						Log.i(TAG, "MP4Config.mp4 结束重新创建");

						settings.edit().
								putInt("screen_capture_width",width).
								putInt("screen_capture_height",height).commit();
						if(!isCallBack[0])
						{
							isCallBack[0]=true;
							listener.finish(true);
						}
					}
				}, mediaProjection, width, height, density);

				String outputPath=testWrapper.getOutputPath();
				outputPath=outputPath.substring(0,outputPath.lastIndexOf("/")+1);
				outputPath+=MP4_CONFIG_NAME;


				testWrapper.setOutputPath(outputPath,true);

				testWrapper.prepare();
				testWrapper.startRecording();


			} catch (IOException e) {
				e.printStackTrace();
				if(!isCallBack[0])
				{
					isCallBack[0]=true;
					listener.finish(false);
				}
			}
		}
		else
		{
			if(!isCallBack[0])
			{
				isCallBack[0]=true;
				listener.finish(true);
			}
		}
	}

	public interface ICheck
	{
		void finish(boolean success);
	}
}