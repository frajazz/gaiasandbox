package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;

public class LineRenderSystem extends AbstractRenderSystem {

    private ImmediateModeRenderer20 renderer;
    private final Matrix4 combinedMatrix = new Matrix4();

    public LineRenderSystem(RenderGroup rg, int priority, float[] alphas) {
	super(rg, priority, alphas);
	this.renderer = new ImmediateModeRenderer20(400000, false, true, 0);
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
	Gdx.gl.glLineWidth(1f);

	combinedMatrix.set(camera.getCamera().combined);
	renderer.begin(combinedMatrix, ShapeType.Line.getGlType());

	int size = renderables.size();
	for (int i = 0; i < size; i++) {
	    IRenderable l = renderables.get(i);
	    l.render(renderer, alphas[l.getComponentType().ordinal()]);
	}
	renderer.end();
    }

}
