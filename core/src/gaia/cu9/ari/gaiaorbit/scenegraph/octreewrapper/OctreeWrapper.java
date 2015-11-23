package gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Transform;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.ArrayList;

/**
 * Static Octree wrapper that can be inserted into the scene graph.
 * This implementation is single-threaded.
 * @author Toni Sagrista
 *
 */
public class OctreeWrapper extends AbstractOctreeWrapper {

    public OctreeWrapper() {
        super();
    }

    public OctreeWrapper(String parentName, OctreeNode<SceneGraphNode> root) {
        super(parentName, root);
        roulette = new ArrayList<SceneGraphNode>(root.nObjects);
    }

    @Override
    protected void updateOctreeObjects(ITimeFrameProvider time, Transform parentTransform, ICamera camera) {
        int size = roulette.size();
        for (int i = 0; i < size; i++) {
            roulette.get(i).update(time, parentTransform, camera, roulette.get(i).opacity);
        }
    }

    @Override
    protected String getRouletteDebug() {
        return "[" + roulette.size() + "]";
    }

}
