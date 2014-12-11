package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class LineRenderSystem extends AbstractRenderSystem {

    private ShapeRenderer renderer;

    public LineRenderSystem(RenderGroup rg, int priority, float[] alphas, ShapeRenderer renderer) {
	super(rg, priority, alphas);
	this.renderer = renderer;
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
	Gdx.gl.glLineWidth(1f);
	renderer.setProjectionMatrix(camera.getCamera().combined);
	renderer.begin(ShapeType.Line);
	for (IRenderable l : renderables) {
	    l.render(renderer, alphas[l.getComponentType().ordinal()]);
	}
	renderer.end();
    }

}
