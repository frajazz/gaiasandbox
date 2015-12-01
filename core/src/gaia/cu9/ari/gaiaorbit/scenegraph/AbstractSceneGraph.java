package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.utils.IntMap;

import gaia.cu9.ari.gaiaorbit.render.system.PixelRenderSystem;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.AbstractOctreeWrapper;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public abstract class AbstractSceneGraph implements ISceneGraph {

    private static final long serialVersionUID = 1L;

    /** The root of the tree **/
    public SceneGraphNode root;
    /** Quick lookup map. Name to node. **/
    HashMap<String, SceneGraphNode> stringToNode;
    /** Star HIP map **/
    IntMap<Star> starMap;
    /** Number of objects per thread **/
    protected int[] objectsPerThread;

    public AbstractSceneGraph() {
        // Id = -1 for root
        root = new SceneGraphNode(-1);
        root.name = SceneGraphNode.ROOT_NAME;

        // Objects per thread
        objectsPerThread = new int[1];
    }

    @Override
    public void initialize(List<SceneGraphNode> nodes, ITimeFrameProvider time) {
        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.sg.insert", nodes.size()));

        // Set the reference
        SceneGraphNode.sg = this;

        // Initialize stringToNode and starMap maps
        stringToNode = new HashMap<String, SceneGraphNode>(nodes.size() * 2);
        stringToNode.put(root.name, root);
        starMap = new IntMap<Star>();
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
                CelestialBody s = (CelestialBody) node.getStars();
                if (s instanceof Star && ((Star) s).hip >= 0) {
                    if (starMap.containsKey(((Star) s).hip)) {
                        Logger.error(new RuntimeException(("Duplicated HIP id: " + ((Star) s).hip)));
                    } else {
                        starMap.put(((Star) s).hip, (Star) s);
                    }
                }
            } else if (node.getStarCount() > 1) {
                List<AbstractPositionEntity> stars = (List<AbstractPositionEntity>) node.getStars();
                for (AbstractPositionEntity s : stars) {
                    if (s instanceof Star && ((Star) s).hip >= 0) {
                        if (starMap.containsKey(((Star) s).hip)) {
                            Logger.error(new RuntimeException(("Duplicated HIP id: " + ((Star) s).hip)));
                        } else {
                            starMap.put(((Star) s).hip, (Star) s);
                        }
                    }
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

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.sg.init", root.numChildren));
    }

    @Override
    public void update(ITimeFrameProvider time, ICamera camera) {
        // Check if we need to update the points
        if (GlobalConf.scene.COMPUTE_GAIA_SCAN && time.getDt() != 0) {
            PixelRenderSystem.POINT_UPDATE_FLAG = true;
        }
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

    public void dispose() {
        root.dispose();
    }

    @Override
    public SceneGraphNode getRoot() {
        return root;
    }

    @Override
    public IntMap<Star> getStarMap() {
        return starMap;
    }

}
