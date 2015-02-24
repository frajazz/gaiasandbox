package gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Transform;
import gaia.cu9.ari.gaiaorbit.util.ds.Multilist;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

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
	roulette = new Multilist<SceneGraphNode>(1, root.nObjects);
    }

    @Override
    protected void updateOctreeObjects(ITimeFrameProvider time, Transform parentTransform, ICamera camera) {
	int lists = roulette.size();
	for (int i = 0; i < lists; i++) {
	    roulette.get(i).update(time, parentTransform, camera);
	}
    }

    @Override
    protected String getRouletteDebug() {
	return "[" + roulette.size() + "]";
    }

}
