package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.Constellation;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.badlogic.gdx.Gdx;

public class ConstellationsLoader<T extends SceneGraphNode> implements ISceneGraphNodeProvider {
    private static final String separator = "\\t|,";
    private String dataPath;

    @Override
    public void initialize(Properties properties) {
	try {
	    dataPath = properties.getProperty("file");

	} catch (Exception e) {
	    Gdx.app.error(this.getClass().getSimpleName(), e.getLocalizedMessage());
	}
    }

    @Override
    public List<Constellation> loadObjects() {
	List<Constellation> constellations = new ArrayList<Constellation>();
	try {
	    // load constellations
	    BufferedReader br = new BufferedReader(new InputStreamReader(FileLocator.getStream(dataPath)));

	    try {
		//Skip first line
		String lastName = "";
		List<long[]> partial = null;
		long lastid = -1;
		String line;
		String name = null;
		while ((line = br.readLine()) != null) {
		    if (!line.startsWith("#")) {
			String[] tokens = line.split(separator);
			name = tokens[0].trim();

			if (!lastName.isEmpty() && !name.equals("JUMP") && !name.equals(lastName)) {
			    // We finished a constellation object
			    Constellation cons = new Constellation(lastName, SceneGraphNode.ROOT_NAME);
			    cons.ct = ComponentType.Constellations;
			    cons.ids = partial;
			    constellations.add(cons);
			    partial = null;
			    lastid = -1;
			}

			if (partial == null) {
			    partial = new ArrayList<long[]>();
			}

			// Break point sequence
			if (name.equals("JUMP") && tokens[1].trim().equals("JUMP")) {
			    lastid = -1;
			} else {

			    long newid = Long.parseLong(tokens[1].trim());
			    if (lastid > 0) {
				partial.add(new long[] { lastid, newid });
			    }
			    lastid = newid;

			    lastName = name;
			}
		    }
		}
		// Add last
		if (!lastName.isEmpty() && !name.equals("JUMP")) {
		    // We finished a constellation object
		    Constellation cons = new Constellation(lastName, SceneGraphNode.ROOT_NAME);
		    cons.ct = ComponentType.Constellations;
		    cons.ids = partial;
		    constellations.add(cons);
		    partial = null;
		    lastid = -1;
		}
	    } catch (IOException e) {
		Gdx.app.error(this.getClass().getSimpleName(), e.getLocalizedMessage());
	    }

	    EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), constellations.size() + " constellations initialized");

	} catch (Exception e) {
	    Gdx.app.error(this.getClass().getSimpleName(), e.getLocalizedMessage());
	}
	return constellations;
    }
}