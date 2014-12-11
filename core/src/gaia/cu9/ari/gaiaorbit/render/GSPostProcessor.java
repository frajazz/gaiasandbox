package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.Fxaa;
import com.bitfire.postprocessing.effects.LensFlare2;
import com.bitfire.postprocessing.effects.Nfaa;
import com.bitfire.utils.ShaderLoader;

public class GSPostProcessor implements IPostProcessor, IObserver {

    private PostProcessBean[] pps;

    float bloomFboScale = 0.5f;
    float lensFboScale = 0.25f;

    public GSPostProcessor() {
	ShaderLoader.BasePath = "shaders/";
	GlobalConf conf = GlobalConf.instance;

	pps = new PostProcessBean[RenderType.values().length];

	pps[RenderType.screen.index] = newPostProcessor(getWidth(RenderType.screen), getHeight(RenderType.screen));
	pps[RenderType.screenshot.index] = newPostProcessor(getWidth(RenderType.screenshot), getHeight(RenderType.screenshot));
	pps[RenderType.frame.index] = newPostProcessor(getWidth(RenderType.frame), getHeight(RenderType.frame));

	// Output AA info.
	if (conf.POSTPROCESS_ANTIALIAS == -1) {
	    EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "FXAA selected");
	} else if (conf.POSTPROCESS_ANTIALIAS == -2) {
	    EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "NFAA selected");
	}

	EventManager.getInstance().subscribe(this, Events.PROPERTIES_WRITTEN, Events.BLOOM_CMD, Events.LENS_FLARE_CMD);

    }

    private int getWidth(RenderType type) {
	switch (type) {
	case screen:
	    return Gdx.graphics.getWidth();
	case screenshot:
	    return GlobalConf.instance.SCREENSHOT_WIDTH;
	case frame:
	    return GlobalConf.instance.RENDER_WIDTH;
	}
	return 0;
    }

    private int getHeight(RenderType type) {
	switch (type) {
	case screen:
	    return Gdx.graphics.getHeight();
	case screenshot:
	    return GlobalConf.instance.SCREENSHOT_HEIGHT;
	case frame:
	    return GlobalConf.instance.RENDER_HEIGHT;
	}
	return 0;
    }

    private PostProcessBean newPostProcessor(int width, int height) {
	GlobalConf conf = GlobalConf.instance;
	PostProcessBean ppb = new PostProcessBean();
	ppb.pp = new PostProcessor(width, height, true, false, true);

	// LENS FLARE
	ppb.lens = new LensFlare2((int) (width * lensFboScale), (int) (height * lensFboScale));
	ppb.lens.setGhosts(8);
	ppb.lens.setHaloWidth(0.5f);
	ppb.lens.setLensColorTexture(new Texture(Gdx.files.internal("img/lenscolor.png")));
	ppb.lens.setFlareIntesity(.17f);
	ppb.lens.setFlareSaturation(0.6f);
	ppb.lens.setBaseIntesity(1f);
	ppb.lens.setBias(-0.98f);
	ppb.lens.setBlurAmount(0.0f);
	ppb.lens.setBlurPasses(2);
	ppb.lens.setEnabled(conf.POSTPROCESS_LENS_FLARE);
	ppb.pp.addEffect(ppb.lens);

	// BLOOM
	ppb.bloom = new Bloom((int) (width * bloomFboScale), (int) (height * bloomFboScale));
	ppb.bloom.setBloomIntesity(conf.POSTPROCESS_BLOOM_INTENSITY);
	ppb.bloom.setThreshold(0f);
	ppb.bloom.setEnabled(conf.POSTPROCESS_BLOOM_INTENSITY > 0);
	ppb.pp.addEffect(ppb.bloom);

	// ANTIALIAS

	if (conf.POSTPROCESS_ANTIALIAS == -1) {
	    ppb.antialiasing = new Fxaa(width, height);
	} else {
	    ppb.antialiasing = new Nfaa(width, height);
	}
	ppb.antialiasing.setEnabled(conf.POSTPROCESS_ANTIALIAS < 0);
	ppb.pp.addEffect(ppb.antialiasing);

	return ppb;
    }

    @Override
    public PostProcessBean getPostProcessBean(RenderType type) {
	return pps[type.index];
    }

    @Override
    public void resize(final int width, final int height) {
	if (pps[RenderType.screen.index].antialiasing != null) {
	    Gdx.app.postRunnable(new Runnable() {
		@Override
		public void run() {
		    replace(RenderType.screen.index, width, height);
		}
	    });
	}

    }

    @Override
    public void notify(Events event, final Object... data) {
	GlobalConf conf = GlobalConf.instance;
	switch (event) {
	case PROPERTIES_WRITTEN:
	    if (changed(pps[RenderType.screenshot.index].pp, conf.SCREENSHOT_WIDTH, conf.SCREENSHOT_HEIGHT)) {
		replace(RenderType.screenshot.index, conf.SCREENSHOT_WIDTH, conf.SCREENSHOT_HEIGHT);
	    }

	    if (changed(pps[RenderType.frame.index].pp, conf.RENDER_WIDTH, conf.RENDER_HEIGHT)) {
		replace(RenderType.frame.index, conf.RENDER_WIDTH, conf.RENDER_HEIGHT);
	    }
	    break;
	case BLOOM_CMD:
	    Gdx.app.postRunnable(new Runnable() {
		@Override
		public void run() {
		    float intensity = (float) data[0];
		    for (int i = 0; i < RenderType.values().length; i++) {
			PostProcessBean ppb = pps[i];
			ppb.bloom.setBloomIntesity(intensity);
			ppb.bloom.setEnabled(intensity > 0);
		    }
		}
	    });
	    break;
	case LENS_FLARE_CMD:
	    boolean active = (Boolean) data[0];
	    for (int i = 0; i < RenderType.values().length; i++) {
		PostProcessBean ppb = pps[i];
		ppb.lens.setEnabled(active);
	    }
	    break;
	case CAMERA_MOTION_UPDATED:
	    //	    rts = RenderType.values();
	    //	    float strength = (float) MathUtilsd.lint(((double) data[1] * Constants.KM_TO_U * Constants.U_TO_PC), 0, 100, 0, 0.05);
	    //	    for (int i = 0; i < rts.length; i++) {
	    //		pps[i].zoomer.setBlurStrength(strength);
	    //	    }
	    break;

	}

    }

    /**
     * Reloads the postprocessor at the given index with the given width and height.
     * @param index
     * @param width
     * @param height
     */
    private void replace(int index, final int width, final int height) {
	PostProcessBean ppb = pps[index];
	ppb.pp.dispose(false);

	pps[index] = newPostProcessor(width, height);

    }

    private boolean changed(PostProcessor postProcess, int width, int height) {
	return postProcess.getCombinedBuffer().width != width || postProcess.getCombinedBuffer().height != height;
    }

}
