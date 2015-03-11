package gaia.cu9.ari.gaiaorbit.data.stars;

import gaia.cu9.ari.gaiaorbit.data.FileLocator;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * Abstract catalog loader with the transformation from spherical to cartesian coordinates
 * @author Toni Sagrista
 *
 */
public abstract class AbstractCatalogLoader {
    protected InputStream data;

    public void initialize(Properties p) {
	try {
	    data = FileLocator.getStream(p.getProperty("file"));
	} catch (FileNotFoundException e) {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	}
    }

    public List<? extends CelestialBody> loadCatalog(InputStream data) throws FileNotFoundException {
	this.data = data;
	return loadCatalog();
    }

    public abstract List<? extends CelestialBody> loadCatalog() throws FileNotFoundException;
}
