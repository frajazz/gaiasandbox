package gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Transform;
import gaia.cu9.ari.gaiaorbit.util.ds.Multilist;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;

/**
 * Abstract octree wrapper with the common parts of the regular octree wrapper and
 * the concurrent one.
 * @author Toni Sagrista
 *
 */
public abstract class AbstractOctreeWrapper extends SceneGraphNode implements Iterable<OctreeNode<SceneGraphNode>> {

    public OctreeNode<SceneGraphNode> root;
    /** A collection of all the octants in this octree **/
    private ArrayList<OctreeNode<SceneGraphNode>> octants;
    /** Roulette list with the objects to process **/
    protected Multilist<SceneGraphNode> roulette;
    /**
     * Is this just a copy?
     */
    protected boolean copy = false;

    protected AbstractOctreeWrapper() {
	super("Octree", null);
    }

    protected AbstractOctreeWrapper(String parentName, OctreeNode<SceneGraphNode> root) {
	this();
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

	// Add all objects into children list
	if (children == null) {
	    children = new ArrayList<SceneGraphNode>(root.nObjects);
	    Iterator<OctreeNode<SceneGraphNode>> it = iterator();
	    while (it.hasNext()) {
		OctreeNode<SceneGraphNode> node = it.next();
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
	    // Compute observed octants and fill roulette list
	    root.update(transform, camera.getCamera().frustum, roulette);
	    updateLocal(time, camera);
	}

	// Call the update method of all entities in the roulette list. This is implemented in the subclass.
	EventManager.getInstance().post(Events.DEBUG3, "Octree threads: " + getRouletteDebug());
	updateOctreeObjects(time, transform, camera);

	// Reset mask
	roulette.clear();

    }

    /**
     * Runs the update on all the observed and selected octree objects.
     * @param time
     * @param parentTransform
     * @param camera
     */
    protected abstract void updateOctreeObjects(ITimeFrameProvider time, final Transform parentTransform, ICamera camera);

    /**
     * Adds the octants to the render lists.
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
	int size = octants.size();
	for (int i = 0; i < size; i++) {
	    OctreeNode<SceneGraphNode> octant = octants.get(i);
	    if (octant.observed)
		addToRender(octant, RenderGroup.LINE);
	}
    }

    public ArrayList<OctreeNode<SceneGraphNode>> toList() {
	if (octants == null) {
	    octants = new ArrayList<OctreeNode<SceneGraphNode>>();
	    octants.add(root);
	    root.addChildrenToList(octants);
	}
	return octants;
    }

    @Override
    public Iterator<OctreeNode<SceneGraphNode>> iterator() {
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
	Class<? extends AbstractOctreeWrapper> clazz = this.getClass();
	Pool<? extends AbstractOctreeWrapper> pool = Pools.get(clazz);
	try {
	    AbstractOctreeWrapper instance = pool.obtain();
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

    protected abstract String getRouletteDebug();
}
