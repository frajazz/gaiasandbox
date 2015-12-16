package gaia.cu9.ari.gaiaorbit.data.stars;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

/**
 * Adds the sun manually
 * @author Toni Sagrista
 *
 */
public class SunLoader extends AbstractCatalogLoader {

    @Override
    public List<? extends CelestialBody> loadData() throws FileNotFoundException {
        List<Star> result = new ArrayList<Star>(1);
        /** ADD SUN MANUALLY **/
        Star sun = new Star(new Vector3d(0, 0, 0), 4.83f, 4.83f, 0.656f, "Sol", (int) System.currentTimeMillis());
        if (runFiltersAnd(sun)) {
            sun.initialize();
            result.add(sun);
        }
        return result;
    }

}
