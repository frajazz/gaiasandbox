package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.AbstractOctreeWrapper;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.utils.LongMap;

public abstract class AbstractSceneGraph implements ISceneGraph {

    private static final long serialVersionUID = 1L;

    /** The root of the tree **/
    public SceneGraphNode root;
    /** Quick lookup map. Name to node. **/
    HashMap<String, SceneGraphNode> stringToNode;
    /** Star id map **/
    LongMap<Star> starMap;
    /** Number of objects per thread **/
    int[] objectsPerThread;

    public AbstractSceneGraph() {
	// Id = -1 for root
	root = new SceneGraphNode(-1);
	root.name = SceneGraphNode.ROOT_NAME;

	// Objects per thread
	objectsPerThread = new int[1];
    }

    @Override
    public void initialize(List<SceneGraphNode> nodes, ITimeFrameProvider time) {
	EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.sg.insert", nodes.size()));

	// Set the reference
	SceneGraphNode.sg = this;

	// Initialize stringToNode and starMap maps
	stringToNode = new HashMap<String, SceneGraphNode>(nodes.size() * 2);
	stringToNode.put(root.name, root);
	starMap = new LongMap<Star>();
	for (SceneGraphNode node : nodes) {
	    if (node.name != null && !node.name.isEmpty()) {
		stringToNode.put(node.name, node);
		stringToNode.put(node.name.toLowerCase(), node);

		// Unwrap octree objects
		if (node instanceof AbstractOctreeWrapper) {
		    AbstractOctreeWrapper ow = (AbstractOctreeWrapper) node;
		    for (SceneGraphNode ownode : ow.children) {
			if (ownode.name != null && !ownode.name.isEmpty()) {
			    stringToNode.put(ownode.name, ownode);
			    stringToNode.put(ownode.name.toLowerCase(), ownode);
			}
		    }
		}
	    }

	    // Star map
	    if (node.getStarCount() == 1) {
		Star s = (Star) node.getStars();
		starMap.put(s.id, s);
	    } else if (node.getStarCount() > 1) {
		List<AbstractPositionEntity> stars = (List<AbstractPositionEntity>) node.getStars();
		for (AbstractPositionEntity s : stars) {
		    if (s instanceof Star)
			starMap.put(s.id, (Star) s);
		}
	    }
	}

	// Insert all the nodes
	for (SceneGraphNode node : nodes) {
	    SceneGraphNode parent = stringToNode.get(node.parentName);
	    if (parent != null) {
		parent.addChild(node, true);
		node.setUp();
	    } else {
		throw new RuntimeException("Parent of node " + node.name + " not found: " + node.parentName);
	    }
	}

	EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.sg.init", root.numChildren));
    }

    /**
     * Only clears the toRender list.
     */
    @Override
    public void update(ITimeFrameProvider time, ICamera camera) {
    }

    public HashMap<String, SceneGraphNode> getStringToNodeMap() {
	return stringToNode;
    }

    public boolean containsNode(String name) {
	return stringToNode.containsKey(name);
    }

    public SceneGraphNode getNode(String name) {
	//return root.getNode(name);
	return stringToNode.get(name);
    }

    public List<SceneGraphNode> getNodes() {
	List<SceneGraphNode> objects = new ArrayList<SceneGraphNode>();
	root.addNodes(objects);
	return objects;
    }

    public List<CelestialBody> getFocusableObjects() {
	List<CelestialBody> objects = new ArrayList<CelestialBody>();
	root.addFocusableObjects(objects);
	return objects;
    }

    public CelestialBody findFocus(String name) {
	List<CelestialBody> objects = new ArrayList<CelestialBody>();
	root.addFocusableObjects(objects);
	for (CelestialBody fo : objects) {
	    if (fo.getName().equals(name)) {
		return fo;
	    }
	}
	return null;
    }

    public int getSize() {
	return root.getAggregatedChildren();
    }

    public int getSize(Class<? extends SceneGraphNode> clazz) {
	return root.getNumNodes(clazz);
    }

    public <T extends SceneGraphNode> List<T> getNodes(Class<T> clazz) {
	List<T> l = new ArrayList<T>(getSize(clazz));
	root.getNodes(clazz, l);
	return l;
    }

    public void dispose() {
	root.dispose();
    }

    @Override
    public SceneGraphNode getRoot() {
	return root;
    }

    @Override
    public LongMap<Star> getStarMap() {
	return starMap;
    }

}
