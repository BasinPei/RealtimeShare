package cn.ysu.edu.realtimeshare.screenreplay.glutils;

/**
 * Created by BasinPei on 2017/5/27.
 */
public class OffScreenSurface extends EglSurfaceBase {

    public OffScreenSurface(final EglCore eglBase, final int width, final int height) {
        super(eglBase);
        createOffscreenSurface(width, height);
        makeCurrent();
    }

    public void release() {
        releaseEglSurface();
    }

}
