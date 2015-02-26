package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.filters.Blur.BlurType;

public class PixelBloomRenderSystem extends AbstractRenderSystem implements IObserver {

    ImmediateModeRenderer20 renderer;
    boolean starColorTransit = false;
    PostProcessor pp;
    FrameBuffer screen_fb;
    FrameBuffer frame_fb;

    public PixelBloomRenderSystem(RenderGroup rg, int priority, float[] alphas) {
	super(rg, priority, alphas);

	// Initialise renderer
	ShaderProgram pointShader = new ShaderProgram(Gdx.files.internal("shader/point.vertex.glsl"), Gdx.files.internal("shader/point.fragment.glsl"));
	if (!pointShader.isCompiled()) {
	    Gdx.app.error(this.getClass().getName(), "Point shader compilation failed:\n" + pointShader.getLog());
	}
	this.renderer = new ImmediateModeRenderer20(8000000, false, true, 0, pointShader);

	// Init bloom
	pp = new PostProcessor(true, true, true);
	Bloom bloom = new Bloom(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	bloom.setThreshold(0.0f);
	bloom.setBaseIntesity(1f);
	bloom.setBaseSaturation(1f);
	bloom.setBloomIntesity(4f);
	bloom.setBloomSaturation(0.4f);
	bloom.setBlurPasses(1);
	bloom.setBlurAmount(0.0f);
	bloom.setBlurType(BlurType.Gaussian5x5b);
	pp.addEffect(bloom);
	pp.setEnabled(true);

	// Own frame buffer
	screen_fb = new FrameBuffer(Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
	frame_fb = new FrameBuffer(Format.RGB888, GlobalConf.screenshot.SCREENSHOT_WIDTH, GlobalConf.screen.SCREEN_HEIGHT, true);

	EventManager.getInstance().subscribe(this, Events.TRANSIT_COLOUR_CMD, Events.SCREEN_RESIZE, Events.TOGGLE_STEREOSCOPIC);
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
	if (rc.ppb != null) {
	    rc.ppb.captureEnd();
	}
	FrameBuffer our_fb = screen_fb;
	if (rc.fb != null) {
	    if (rc.fb.getWidth() != frame_fb.getWidth() || rc.fb.getHeight() != frame_fb.getHeight()) {
		frame_fb = new FrameBuffer(Format.RGB888, rc.fb.getWidth(), rc.fb.getHeight(), true);
	    }
	    our_fb = frame_fb;
	}

	pp.capture();
	renderer.begin(camera.getCamera().combined, ShapeType.Point.getGlType());
	int size = renderables.size();
	for (int i = 0; i < size; i++) {
	    IRenderable s = renderables.get(i);
	    s.render(renderer, alphas[s.getComponentType().ordinal()], starColorTransit);
	}
	renderer.end();
	pp.render(our_fb);

	Texture tex = our_fb.getColorBufferTexture();

	if (rc.ppb != null) {
	    rc.ppb.capture();
	}

	Viewport vp = camera.getCurrent().getViewport();

	GlobalResources.spriteBatch.begin();
	GlobalResources.spriteBatch.draw(tex, vp.getScreenX(), vp.getScreenY(), 0, 0, our_fb.getWidth(), our_fb.getHeight(), 1, 1, 0, 0, 0, our_fb.getWidth(), our_fb.getHeight(), false, true);
	GlobalResources.spriteBatch.end();

    }

    @Override
    public void notify(Events event, Object... data) {
	switch (event) {
	case TRANSIT_COLOUR_CMD:
	    starColorTransit = (boolean) data[1];
	    break;
	case SCREEN_RESIZE:
	    screen_fb = new FrameBuffer(Format.RGB888, (Integer) data[0], (Integer) data[1], true);
	    break;
	case TOGGLE_STEREOSCOPIC:
	    // Update size
	    if (GlobalConf.program.STEREOSCOPIC_MODE) {
		screen_fb = new FrameBuffer(Format.RGB888, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight(), true);
	    } else {
		screen_fb = new FrameBuffer(Format.RGB888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
	    }
	    break;
	}
    }

}
