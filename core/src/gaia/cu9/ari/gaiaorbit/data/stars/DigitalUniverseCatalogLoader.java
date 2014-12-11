package gaia.cu9.ari.gaiaorbit.data.stars;

import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;

public class DigitalUniverseCatalogLoader extends AbstractCatalogLoader implements ICatalogLoader {
    private static final String separator = "\\s+";

    private static final float factor = 10000;
    private static final float magcut = 12f;
    private static final float distcut = 1000000f;

    @Override
    public List<CelestialBody> loadStars(InputStream data) throws FileNotFoundException {
	List<CelestialBody> stars = new ArrayList<CelestialBody>();
	BufferedReader br = new BufferedReader(new InputStreamReader(data));

	try {
	    //Skip first line
	    br.readLine();
	    String line;
	    while ((line = br.readLine()) != null) {
		//Add star
		if (line.startsWith("#") || line.startsWith("datavar") || line.startsWith("texture") || line.startsWith("\n") || line.isEmpty()) {
		    // Skipping line
		} else {
		    addStar(line, stars);
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
	Gdx.app.log(this.getClass().getCanonicalName(), "Catalog initialized, " + stars.size() + " stars.");
	return stars;
    }

    private void addStar(String line, List<CelestialBody> stars) {
	String[] st = line.split(separator);
	float x = Float.parseFloat(st[1]) * factor;
	float y = Float.parseFloat(st[2]) * factor;
	float z = Float.parseFloat(st[3]) * factor;
	float absmag = Float.parseFloat(st[6]);
	float appmag = Float.parseFloat(st[7]);
	float colorbv = Float.parseFloat(st[4]);
	String name = st[19];
	Vector3d pos = new Vector3d(x, y, z);
	float dist = (float) pos.len();
	if (appmag < magcut && dist < distcut) {
	    Star star = new Star(pos, absmag, appmag, colorbv, name, 0l);
	    stars.add(star);
	}
    }
}
