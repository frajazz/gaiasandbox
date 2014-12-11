package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.utils.LongMap;

public abstract class AbstractSceneGraph implements ISceneGraph {

    private static final long serialVersionUID = 1L;

    /** The root of the tree **/
    public SceneGraphNode root;
    /** Updated every frame with the visible entities **/
    public List<IRenderable> toRender;
    /** Quick lookup map. Name to node. **/
    HashMap<String, SceneGraphNode> stringToNode;
    /** Star id map **/
    LongMap<Star> starMap;

    public AbstractSceneGraph() {
	// Id = -1 for root
	root = new SceneGraphNode(-1);
	root.name = SceneGraphNode.ROOT_NAME;
    }

    @Override
    public void initialize(List<SceneGraphNode> nodes, ITimeFrameProvider time) {
	EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "Inserting " + nodes.size() + " nodes into the scene graph...");

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
	    }
	    if (node instanceof Star) {
		Star s = (Star) node;
		starMap.put(s.id, s);
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

	toRender = Collections.synchronizedList(new ArrayList<IRenderable>(nodes.size()));

	EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "Scene graph initialized with " + root.numChildren + " objects");
    }

    /**
     * Only clears the toRender list.
     */
    @Override
    public void update(ITimeFrameProvider time, ICamera camera) {
	toRender.clear();
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
    public List<IRenderable> getToRenderList() {
	return toRender;
    }

    @Override
    public LongMap<Star> getStarMap() {
	return starMap;
    }

}
