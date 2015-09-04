package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraph;

public class WebGLSceneGraphImplementationProvider extends SceneGraphImplementationProvider {

    @Override
    public ISceneGraph getImplementation(boolean multithreading, boolean hasOctree, int maxThreads) {
        return new SceneGraph();
    }

}
