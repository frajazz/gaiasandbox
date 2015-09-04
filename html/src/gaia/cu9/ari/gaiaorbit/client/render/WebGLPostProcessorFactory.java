package gaia.cu9.ari.gaiaorbit.client.render;

import gaia.cu9.ari.gaiaorbit.render.IPostProcessor;
import gaia.cu9.ari.gaiaorbit.render.PostProcessorFactory;

public class WebGLPostProcessorFactory extends PostProcessorFactory {

    @Override
    public IPostProcessor getPostProcessor() {
        return new WebGLPostProcessor();
    }

}
