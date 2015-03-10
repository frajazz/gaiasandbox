package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.render.ILineRenderable;

import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;

public abstract class LineObject extends AbstractPositionEntity implements ILineRenderable {

    @Override
    public void render(Object... params) {
	render((ImmediateModeRenderer20) params[0], (Float) params[1]);
    }

}
