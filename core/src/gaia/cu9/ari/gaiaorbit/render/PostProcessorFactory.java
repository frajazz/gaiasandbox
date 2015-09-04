package gaia.cu9.ari.gaiaorbit.render;

public abstract class PostProcessorFactory {
    public static PostProcessorFactory instance;

    public static void initialize(PostProcessorFactory instance) {
        PostProcessorFactory.instance = instance;
    }

    public abstract IPostProcessor getPostProcessor();
}
