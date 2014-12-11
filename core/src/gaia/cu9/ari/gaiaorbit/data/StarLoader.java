package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.data.stars.ICatalogLoader;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;

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
    private String dataPath;
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
	    dataPath = properties.getProperty("file");

	} catch (Exception e) {
	    Gdx.app.error(this.getClass().getSimpleName(), e.getLocalizedMessage());
	}
    }

    @Override
    public List<CelestialBody> loadObjects() {
	List<CelestialBody> stars = null;
	try {
	    stars = loader.loadStars(FileLocator.getStream(dataPath));
	    for (CelestialBody s : stars) {
		s.initialize();
	    }

	} catch (Exception e) {
	    Gdx.app.error(this.getClass().getSimpleName(), e.getLocalizedMessage());
	}
	return stars;
    }

}
