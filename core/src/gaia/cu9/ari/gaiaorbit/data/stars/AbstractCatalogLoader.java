package gaia.cu9.ari.gaiaorbit.data.stars;

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
    protected String file;

    public void initialize(Properties p) {
        file = p.getProperty("file");
    }

    public List<? extends CelestialBody> loadCatalog(InputStream data) throws FileNotFoundException {
        return loadCatalog();
    }

    public abstract List<? extends CelestialBody> loadCatalog() throws FileNotFoundException;
}
