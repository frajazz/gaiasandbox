package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public class RenderContext {

    public PostProcessBean ppb;
    /** In case this is not null, we are using the screenshot or frame output feature **/
    public FrameBuffer fb;

}
