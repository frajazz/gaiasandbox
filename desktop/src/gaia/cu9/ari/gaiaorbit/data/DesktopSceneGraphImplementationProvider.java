package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.desktop.concurrent.SceneGraphConcurrent;
import gaia.cu9.ari.gaiaorbit.desktop.concurrent.SceneGraphConcurrentOctree;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraph;

public class DesktopSceneGraphImplementationProvider extends SceneGraphImplementationProvider {

    @Override
    public ISceneGraph getImplementation(boolean multithreading, boolean hasOctree, int maxThreads) {
        ISceneGraph sg = null;
        if (multithreading) {
            if (!hasOctree) {
                // No octree, local data
                sg = new SceneGraphConcurrent(maxThreads);
            } else {
                // Object server, we use octree mode
                sg = new SceneGraphConcurrentOctree(maxThreads);
            }
        } else {
            sg = new SceneGraph();
        }
        return sg;
    }

}
