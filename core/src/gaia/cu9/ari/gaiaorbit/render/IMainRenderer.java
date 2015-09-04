package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public interface IMainRenderer {

    public FrameBuffer getFrameBuffer(int w, int h);

    public void preRenderScene();

    public void renderSgr(ICamera camera, int width, int height, FrameBuffer frameBuffer, PostProcessBean ppb);

    public ICamera getICamera();

    public CameraManager getCameraManager();

    public IPostProcessor getPostProcessor();
}
