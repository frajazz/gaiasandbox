package gaia.cu9.ari.gaiaorbit.client.render;

import gaia.cu9.ari.gaiaorbit.render.IPostProcessor;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import com.badlogic.gdx.Gdx;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.utils.ShaderLoader;

public class WebGLPostProcessor implements IPostProcessor {

    private PostProcessBean pps;

    float bloomFboScale = 0.5f;
    float lensFboScale = 0.25f;

    public WebGLPostProcessor() {
        ShaderLoader.BasePath = "shaders/";
        pps = newPostProcessor(getWidth(RenderType.screen), getHeight(RenderType.screen));
    }

    private int getWidth(RenderType type) {
        switch (type) {
        case screen:
            return Gdx.graphics.getWidth();
        case screenshot:
            return GlobalConf.screenshot.SCREENSHOT_WIDTH;
        case frame:
            return GlobalConf.frame.RENDER_WIDTH;
        }
        return 0;
    }

    private int getHeight(RenderType type) {
        switch (type) {
        case screen:
            return Gdx.graphics.getHeight();
        case screenshot:
            return GlobalConf.screenshot.SCREENSHOT_HEIGHT;
        case frame:
            return GlobalConf.frame.RENDER_HEIGHT;
        }
        return 0;
    }

    private PostProcessBean newPostProcessor(int width, int height) {
        PostProcessBean ppb = new PostProcessBean();

        ppb.pp = new PostProcessor(width, height, true, false, true);
        return ppb;
    }

    @Override
    public PostProcessBean getPostProcessBean(RenderType type) {
        return pps;
    }

    @Override
    public void resize(final int width, final int height) {

    }

    /**
     * Reloads the postprocessor at the given index with the given width and height.
     * @param index
     * @param width
     * @param height
     */
    private void replace(int index, final int width, final int height) {
        pps = newPostProcessor(width, height);

    }

    private boolean changed(PostProcessor postProcess, int width, int height) {
        return postProcess.getCombinedBuffer().width != width || postProcess.getCombinedBuffer().height != height;
    }

}
