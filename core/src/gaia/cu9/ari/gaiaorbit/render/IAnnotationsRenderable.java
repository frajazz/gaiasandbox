package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface IAnnotationsRenderable extends IRenderable {

    public void render(SpriteBatch spriteBatch, ICamera camera, float alpha);
}
