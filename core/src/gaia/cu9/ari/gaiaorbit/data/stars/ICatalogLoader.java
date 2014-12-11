package gaia.cu9.ari.gaiaorbit.data.stars;

import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public interface ICatalogLoader {

    public List<CelestialBody> loadStars(InputStream data) throws FileNotFoundException;

}
