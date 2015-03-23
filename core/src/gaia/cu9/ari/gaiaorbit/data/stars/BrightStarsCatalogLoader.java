package gaia.cu9.ari.gaiaorbit.data.stars;

import gaia.cu9.ari.gaiaorbit.data.FileLocator;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.badlogic.gdx.Gdx;

/**
 * Loads the converted BSC.
 * @author Toni Sagrista
 *
 */
public class BrightStarsCatalogLoader extends AbstractCatalogLoader implements ICatalogLoader {
    private static final String separator = ",";

    @Override
    public List<CelestialBody> loadCatalog() throws FileNotFoundException {
	List<CelestialBody> stars = new ArrayList<CelestialBody>();
	InputStream data = null;
	try {
	    data = FileLocator.getStream(file);
	} catch (FileNotFoundException e) {
	    EventManager.instance.post(Events.JAVA_EXCEPTION, e);
	}
	BufferedReader br = new BufferedReader(new InputStreamReader(data));

	try {
	    //Skip first line
	    br.readLine();
	    String line;
	    while ((line = br.readLine()) != null) {
		//Add star
		addStar(line, stars);
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	} finally {
	    try {
		br.close();
	    } catch (IOException e) {
		EventManager.instance.post(Events.JAVA_EXCEPTION, e);
	    }

	}
	Gdx.app.log(this.getClass().getCanonicalName(), "Catalog initialized, " + stars.size() + " stars.");
	return stars;
    }

    private void addStar(String line, List<CelestialBody> stars) {
	String[] st = line.split(separator);
	float ra = Parser.parseFloat(st[1]);
	float dec = Parser.parseFloat(st[2]);
	float dist = 1e5f;
	Vector3d pos = Coordinates.sphericalToCartesian(ra, dec, dist, new Vector3d());
	float absmag = Float.parseFloat(st[3]);
	String name = (st.length == 6 ? st[5].substring(1, st[5].length() - 1) : null);
	Star star = new Star(pos, absmag, absmag, 0, name, ra, dec, 0l);
	stars.add(star);
    }

    @Override
    public void initialize(Properties p) {
	super.initialize(p);
    }

}
