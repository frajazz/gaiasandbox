package gaia.cu9.ari.gaiaorbit.desktop.render;

import gaia.cu9.ari.gaiaorbit.render.IPostProcessor;
import gaia.cu9.ari.gaiaorbit.render.PostProcessorFactory;

public class DesktopPostProcessorFactory extends PostProcessorFactory {

    @Override
    public IPostProcessor getPostProcessor() {
        return new DesktopPostProcessor();
    }

}
