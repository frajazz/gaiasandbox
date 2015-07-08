package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import com.badlogic.gdx.Gdx;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.Fxaa;
import com.bitfire.utils.ShaderLoader;

public class SimplePostProcessor implements IPostProcessor {

    /** Post processor effect **/
    private PostProcessor postProcessor;
    private Bloom bloom;
    private Fxaa fxaa;

    public SimplePostProcessor() {
        // Post process effects
        ShaderLoader.BasePath = "shaders/";
        postProcessor = new PostProcessor(true, true, true);

        // BLOOM
        bloom = new Bloom(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        bloom.setBloomIntesity(GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY);
        postProcessor.addEffect(bloom);

        // ANTIALIAS
        if (GlobalConf.postprocess.POSTPROCESS_ANTIALIAS < 0) {
            fxaa = new Fxaa(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            postProcessor.addEffect(fxaa);
        }

    }

    private boolean postProcess() {
        return bloom.getBloomIntensity() > 0 || fxaa != null;
    }

    public void capture() {
        if (postProcess())
            postProcessor.capture();
    }

    public void render() {
        if (postProcess())
            postProcessor.render();
    }

    @Override
    public PostProcessBean getPostProcessBean() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void resize(int width, int height) {
        // TODO Auto-generated method stub

    }

}
