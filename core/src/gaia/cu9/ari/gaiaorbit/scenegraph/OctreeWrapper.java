package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/**
 * Static octree wrapper that can be inserted into the scene graph. 
 * Holds a static octree node which is the root of the tree, the 0 depth node.
 * @author Toni Sagrista
 *
 */
public class OctreeWrapper extends SceneGraphNode implements Iterable<OctreeNode<AbstractPositionEntity>> {

    public OctreeNode<AbstractPositionEntity> root;
    /** A collection of all the octants in this octree **/
    private ArrayList<OctreeNode<AbstractPositionEntity>> octants;

    /**
     * Is this just a copy?
     */
    protected boolean copy = false;

    public OctreeWrapper() {
    }

    public OctreeWrapper(String parentName, OctreeNode<AbstractPositionEntity> root) {
	super("Octree", null);
	this.ct = ComponentType.Others;
	this.root = root;
	this.parentName = parentName;
    }

    /** 
     * An octree wrapper has as 'scene graph children' all the elements contained in the
     * octree, even though it acts as a hub that decides which are processed and which are not.
     */
    @Override
    public void initialize() {
	super.initialize();

	// An octree
	if (children == null) {
	    children = new ArrayList<SceneGraphNode>(root.nObjects);
	    Iterator<OctreeNode<AbstractPositionEntity>> it = iterator();
	    while (it.hasNext()) {
		OctreeNode<AbstractPositionEntity> node = it.next();
		this.add(node.objects);
	    }
	}
    }

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera) {
	update(time, parentTransform, camera, 1f);
    }

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera, float opacity) {
	this.opacity = opacity;
	transform.set(parentTransform);

	// Update octants
	if (!copy) {
	    root.update(camera);
	    updateLocal(time, camera);
	}

	// TODO remove this, not all stars should be updated!
	if (children != null) {
	    for (int i = 0; i < children.size(); i++) {
		children.get(i).update(time, transform, camera);
	    }
	}
    }

    /**
     * Updates the local transform matrix.
     * @param time
     */
    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
	if (!copy) {
	    addToRenderLists(camera);
	}
    }

    protected void addToRenderLists(ICamera camera) {
	// Add all children
	for (OctreeNode<AbstractPositionEntity> octant : octants) {
	    addToRender(octant, RenderGroup.LINE);
	}
    }

    public ArrayList<OctreeNode<AbstractPositionEntity>> toList() {
	if (octants == null) {
	    octants = new ArrayList<OctreeNode<AbstractPositionEntity>>();
	    octants.add(root);
	    root.addChildrenToList(octants);
	}
	return octants;
    }

    @Override
    public Iterator<OctreeNode<AbstractPositionEntity>> iterator() {
	return this.toList().iterator();
    }

    @Override
    public int getStarCount() {
	return root.nObjects;
    }

    @Override
    public Object getStars() {
	return children;
    }

    /**
     * Gets a copy of this object but does not copy its parent or children.
     * @return
     */
    @Override
    public <T extends SceneGraphNode> T getSimpleCopy() {
	Class<? extends OctreeWrapper> clazz = this.getClass();
	Pool<? extends OctreeWrapper> pool = Pools.get(clazz);
	try {
	    OctreeWrapper instance = pool.obtain();
	    instance.copy = true;
	    instance.name = this.name;
	    instance.transform.set(this.transform);
	    instance.ct = this.ct;
	    if (this.localTransform != null)
		instance.localTransform.set(this.localTransform);

	    return (T) instance;
	} catch (Exception e) {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	}
	return null;
    }

}
