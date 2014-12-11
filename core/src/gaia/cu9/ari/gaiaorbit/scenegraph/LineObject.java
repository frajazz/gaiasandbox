package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.render.ILineRenderable;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public abstract class LineObject extends AbstractPositionEntity implements ILineRenderable {

    @Override
    public void render(Object... params) {
	render((ShapeRenderer) params[0], (Float) params[1]);
    }

}
