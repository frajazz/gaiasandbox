package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;

/**
 * Provides the scene graph implementation.
 * @author tsagrista
 *
 */
public abstract class SceneGraphImplementationProvider {
    public static SceneGraphImplementationProvider provider;

    public static void initialize(SceneGraphImplementationProvider provider) {
        SceneGraphImplementationProvider.provider = provider;
    }

    public abstract ISceneGraph getImplementation(boolean multithreading, boolean hasOctree, int maxThreads);

}
