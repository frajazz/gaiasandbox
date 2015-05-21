package gaia.cu9.ari.gaiaorbit.render;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;

public class RenderContext {

    /** The post process bean. It may have no effects enabled. **/
    public PostProcessBean ppb;

    /** In case this is not null, we are using the screenshot or frame output feature. This is the renderToFile frame buffer. **/
    public FrameBuffer fb;

    /** Render width and height **/
    public int w, h;

}
