package gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper;

import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.Transform;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

/**
 * Static octree wrapper that can be inserted into the scene graph.
 * This implementation is single-threaded.
 * @author Toni Sagrista
 *
 */
public class OctreeWrapper extends AbstractOctreeWrapper {

    public OctreeWrapper() {
	super();
    }

    public OctreeWrapper(String parentName, OctreeNode<AbstractPositionEntity> root) {
	super(parentName, root);
    }

    @Override
    protected void processOctree(ITimeFrameProvider time, Transform parentTransform, ICamera camera) {
	// TODO remove this, not all stars should be updated!
	if (children != null) {
	    for (int i = 0; i < children.size(); i++) {
		children.get(i).update(time, transform, camera);
	    }
	}
    }

}
