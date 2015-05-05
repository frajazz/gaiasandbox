package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.render.ILineRenderable;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;

public abstract class LineObject extends AbstractPositionEntity implements ILineRenderable {

    @Override
    public void render(Object... params) {
        render((LineRenderSystem) params[0], (ICamera) params[1], (Float) params[2]);
    }

}
