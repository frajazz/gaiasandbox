package gaia.cu9.ari.gaiaorbit.data.stars;

import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Abstract catalog loader with the transformation from spherical to cartesian coordinates
 * @author Toni Sagrista
 *
 */
public abstract class AbstractCatalogLoader {
    public String[] files;

    public void initialize(String[] files) {
        this.files = files;
    }

    public abstract List<? extends CelestialBody> loadData() throws FileNotFoundException;
}
