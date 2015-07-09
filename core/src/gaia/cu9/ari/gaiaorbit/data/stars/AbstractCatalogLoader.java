package gaia.cu9.ari.gaiaorbit.data.stars;

import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;

import java.io.FileNotFoundException;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;

/**
 * Abstract catalog loader with the transformation from spherical to cartesian coordinates
 * @author Toni Sagrista
 *
 */
public abstract class AbstractCatalogLoader {
    public FileHandle file;

    public void initialize(FileHandle p) {
        file = p;
    }

    public abstract List<? extends CelestialBody> loadCatalog() throws FileNotFoundException;
}
