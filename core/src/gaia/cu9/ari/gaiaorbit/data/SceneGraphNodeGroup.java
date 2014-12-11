package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;

import java.util.List;

/**
 * Bean that holds a list of scene graph nodes.
 * @author Toni Sagrista
 *
 */
public class SceneGraphNodeGroup {

    public List<SceneGraphNode> nodes;

    public SceneGraphNodeGroup() {
	super();
    }

    public SceneGraphNodeGroup(List<SceneGraphNode> nodes) {
	this();
	this.nodes = nodes;
    }

}
