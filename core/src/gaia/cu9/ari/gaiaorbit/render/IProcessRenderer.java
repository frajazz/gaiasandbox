package gaia.cu9.ari.gaiaorbit.render;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;

/**
 * Interface for component renderers.
 * @author Toni Sagrista
 *
 */
public interface IProcessRenderer {

    /**
     * Renders the scene.
     * @param camera The camera to use.
     * @param rw The render width.
     * @param rh The render height.
     * @param fb The frame buffer to write to, if any.
     * @param ppb The post process bean.
     */
    public void render(ICamera camera, int rw, int rh, FrameBuffer fb, PostProcessBean ppb);

    public void initialize(AssetManager assetManager);

}
