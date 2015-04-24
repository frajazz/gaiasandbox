package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

import java.util.List;

public class SmoothLineRenderSystem extends AbstractRenderSystem {

    protected SmoothLineRenderSystem(RenderGroup rg, int priority, float[] alphas) {
	super(rg, priority, alphas);
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
	for (IRenderable renderable : renderables) {

	}

    }

}
