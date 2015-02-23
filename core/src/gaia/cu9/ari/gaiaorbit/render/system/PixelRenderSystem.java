package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class PixelRenderSystem extends AbstractRenderSystem implements IObserver {

    ImmediateModeRenderer20 renderer;
    boolean starColorTransit = false;

    public PixelRenderSystem(RenderGroup rg, int priority, float[] alphas) {
	super(rg, priority, alphas);

	// Initialise renderer
	ShaderProgram pointShader = new ShaderProgram(Gdx.files.internal("shader/point.vertex.glsl"), Gdx.files.internal("shader/point.fragment.glsl"));
	if (!pointShader.isCompiled()) {
	    Gdx.app.error(this.getClass().getName(), "Point shader compilation failed:\n" + pointShader.getLog());
	}
	this.renderer = new ImmediateModeRenderer20(1500000, false, true, 0, pointShader);

	EventManager.getInstance().subscribe(this, Events.TRANSIT_COLOUR_CMD);
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
	renderer.begin(camera.getCamera().combined, ShapeType.Point.getGlType());
	int size = renderables.size();
	for (int i = 0; i < size; i++) {
	    IRenderable s = renderables.get(i);
	    s.render(renderer, alphas[s.getComponentType().ordinal()], starColorTransit);
	}
	renderer.end();
    }

    @Override
    public void notify(Events event, Object... data) {
	if (event == Events.TRANSIT_COLOUR_CMD) {
	    starColorTransit = (boolean) data[1];
	}

    }

}
