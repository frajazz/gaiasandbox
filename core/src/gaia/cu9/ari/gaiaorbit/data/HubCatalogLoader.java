package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import java.util.List;
import java.util.Properties;

/**
 * This class acts as a hub that redirects to one or another implementation
 * depending on the properties.
 * @author tsagrista
 *
 */
public class HubCatalogLoader implements ISceneGraphNodeProvider {

    /** These only apply to the StarLoader, but anyway... **/
    Properties starLoaderProperties;

    @Override
    public void initialize(Properties properties) {
	this.starLoaderProperties = properties;
    }

    @Override
    public List<? extends SceneGraphNode> loadObjects() {
	ISceneGraphNodeProvider loader = null;
	if (GlobalConf.instance.DATA_SOURCE_LOCAL) {
	    // Use local
	    loader = new StarLoader();
	} else {
	    // Use object server
	    loader = new ObjectServerLoader();
	}
	loader.initialize(starLoaderProperties);
	return loader.loadObjects();
    }

}
