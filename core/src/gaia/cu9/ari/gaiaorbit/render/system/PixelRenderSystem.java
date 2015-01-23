package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.filters.Blur.BlurType;

public class PixelRenderSystem extends AbstractRenderSystem implements IObserver {

    ImmediateModeRenderer20 renderer;
    boolean starColorTransit = false;
    PostProcessor pp;

    public PixelRenderSystem(RenderGroup rg, int priority, float[] alphas) {
	super(rg, priority, alphas);

	// Initialise renderer
	ShaderProgram pointShader = new ShaderProgram(Gdx.files.internal("shader/point.vertex.glsl"), Gdx.files.internal("shader/point.fragment.glsl"));
	if (!pointShader.isCompiled()) {
	    Gdx.app.error(this.getClass().getName(), "Point shader compilation failed:\n" + pointShader.getLog());
	}
	this.renderer = new ImmediateModeRenderer20(120000, false, true, 0, pointShader);

	// Init bloom
	pp = new PostProcessor(true, true, true);
	Bloom bloom = new Bloom(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	bloom.setThreshold(0.0f);
	bloom.setBaseIntesity(1f);
	bloom.setBaseSaturation(1f);
	bloom.setBloomIntesity(6f);
	bloom.setBloomSaturation(0.5f);
	bloom.setBlurPasses(1);
	bloom.setBlurAmount(0.0f);
	bloom.setBlurType(BlurType.Gaussian5x5b);
	pp.addEffect(bloom);
	pp.setEnabled(true);

	EventManager.getInstance().subscribe(this, Events.TRANSIT_COLOUR_CMD);
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
	FrameBuffer fb = null;
	//	if (rc.ppb != null) {
	//	    fb = rc.ppb.captureEnd();
	//	}
	pp.capture();
	renderer.begin(camera.getCamera().combined, ShapeType.Point.getGlType());
	for (int i = 0; i < renderables.size(); i++) {
	    IRenderable s = renderables.get(i);
	    s.render(renderer, alphas[s.getComponentType().ordinal()], starColorTransit);
	}
	renderer.end();
	pp.render(fb);

    }

    @Override
    public void notify(Events event, Object... data) {
	if (event == Events.TRANSIT_COLOUR_CMD) {
	    starColorTransit = (boolean) data[1];
	}

    }

}
