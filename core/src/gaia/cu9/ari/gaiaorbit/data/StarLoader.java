package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.data.stars.ICatalogLoader;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;

import java.util.List;
import java.util.Properties;

import com.badlogic.gdx.Gdx;

/**
 * {@link ISceneGraphNodeProvider} that loads stars. It gets the stars from an implementation
 * of {@link ICatalogLoader}.
 * @author Toni Sagrista
 *
 */
public class StarLoader implements ISceneGraphNodeProvider {
    private ICatalogLoader loader;

    /**
     * Creates and initialized the catalog
     */
    public StarLoader() {
	super();
    }

    @Override
    public void initialize(Properties properties) {
	try {
	    Class<?> loaderClass = Class.forName(properties.getProperty("loader"));
	    loader = (ICatalogLoader) loaderClass.newInstance();
	    loader.initialize(properties);

	} catch (Exception e) {
	    Gdx.app.error(this.getClass().getSimpleName(), e.getLocalizedMessage());
	}
    }

    @Override
    public List<? extends SceneGraphNode> loadObjects() {
	List<? extends SceneGraphNode> nodes = null;
	try {
	    nodes = loader.loadCatalog();
	    for (SceneGraphNode s : nodes) {
		s.initialize();
	    }

	} catch (Exception e) {
	    Gdx.app.error(this.getClass().getSimpleName(), e.getLocalizedMessage());
	}
	return nodes;
    }

}
