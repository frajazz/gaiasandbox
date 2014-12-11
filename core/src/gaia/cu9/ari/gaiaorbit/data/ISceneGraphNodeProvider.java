package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;

import java.util.List;
import java.util.Properties;

/**
 * Interface that must be implemented by any provider of celestial objects, being
 * them stars, planets or whatever.
 * @author Toni Sagrista
 *
 */
public interface ISceneGraphNodeProvider {

    public void initialize(Properties properties);

    public List<? extends SceneGraphNode> loadObjects();

}
