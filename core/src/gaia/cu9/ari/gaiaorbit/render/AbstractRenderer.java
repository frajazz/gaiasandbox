package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;

/**
 * An abstract renderer.
 * @author Toni Sagrista
 *
 */
public abstract class AbstractRenderer {

    protected static ISceneGraph sg;

    public static void initialize(ISceneGraph sg) {
        AbstractRenderer.sg = sg;
    }

    public AbstractRenderer() {
        super();
    }

}
