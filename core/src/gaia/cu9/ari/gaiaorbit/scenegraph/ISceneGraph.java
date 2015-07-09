package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.utils.LongMap;

public interface ISceneGraph extends Serializable {
    public void initialize(List<SceneGraphNode> nodes, ITimeFrameProvider time);

    public void update(ITimeFrameProvider time, ICamera camera);

    public boolean containsNode(String name);

    /**
     * Returns the node with the given name, or null if it does not exist.
     * @param name The name of the node.
     * @return The node with the name.
     */
    public SceneGraphNode getNode(String name);

    public HashMap<String, SceneGraphNode> getStringToNodeMap();

    public LongMap<CelestialBody> getStarMap();

    public List<SceneGraphNode> getNodes();

    public SceneGraphNode getRoot();

    public List<CelestialBody> getFocusableObjects();

    public CelestialBody findFocus(String name);

    public int getSize();

    public void dispose();

}
