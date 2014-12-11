package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

/**
 * Interface for component renderers.
 * @author Toni Sagrista
 *
 */
public interface IProcessRenderer {

    public void render(ICamera camera, FrameBuffer fb, float alpha);

    public void render(ICamera camera, FrameBuffer fb);

    public void initialize(AssetManager assetManager);

}
