package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.filters.Blur.BlurType;

public class PixelBloomRenderSystem extends PixelPostProcessRenderSystem implements IObserver {

    public PixelBloomRenderSystem(RenderGroup rg, int priority, float[] alphas) {
	super(rg, priority, alphas);
    }

    protected PostProcessor getPostProcessor(int w, int h) {
	String key = getKey(w, h);
	if (!ppmap.containsKey(key)) {
	    PostProcessor pp = new PostProcessor(w, h, true, true, true);

	    // Bloom
	    Bloom bloom = new Bloom(w, h);
	    bloom.setThreshold(0.0f);
	    bloom.setBaseIntesity(1f);
	    bloom.setBaseSaturation(.9f);
	    bloom.setBloomIntesity(6f);
	    bloom.setBloomSaturation(.5f);
	    bloom.setBlurPasses(1);
	    bloom.setBlurAmount(10.0f);
	    bloom.setBlurType(BlurType.Gaussian5x5b);
	    bloom.setEnabled(true);
	    pp.addEffect(bloom);

	    // Enable post processor
	    pp.setEnabled(true);

	    ppmap.put(key, pp);
	}
	return ppmap.get(key);
    }

}
