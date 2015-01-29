package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.util.g3d.ModelBuilder2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;

public class ModelCache {
    final Map<String, Model> modelCache;
    public ModelBuilder2 mb;
    /** Model cache **/
    public static ModelCache cache = new ModelCache();

    public ModelCache() {
	modelCache = new HashMap<String, Model>();
	mb = new ModelBuilder2();
    }

    public Model getModel(String shape, Map<String, Object> params, int attributes) {

	String key = getKey(shape, params, attributes);
	Model model = null;
	if (modelCache.containsKey(key)) {
	    model = modelCache.get(key);
	} else {
	    switch (shape) {
	    case "sphere":
		Integer quality = ((Long) params.get("quality")).intValue();
		Float diameter = ((Double) params.get("diameter")).floatValue();
		Boolean flip = (Boolean) params.get("flip");
		model = mb.createSphere(diameter, quality, quality, flip, new Material(), attributes);
		modelCache.put(key, model);
		break;
	    case "disc":
		// Prepare model
		Material mat = new Material();
		float diameter2 = ((Double) params.get("diameter")).floatValue() / 2f;
		// Initialize milky way model
		mb.begin();
		mb.part("mw-up", GL20.GL_TRIANGLES, attributes, mat).
			rect(diameter2, 0, -diameter2,
				diameter2, 0, diameter2,
				-diameter2, 0, diameter2,
				-diameter2, 0, -diameter2,
				0, 1, 0);
		mb.part("mw-down", GL20.GL_TRIANGLES, attributes, mat).
			rect(-diameter2, -0.001f, diameter2,
				diameter2, -0.001f, diameter2,
				diameter2, -0.001f, -diameter2,
				-diameter2, -0.001f, -diameter2,
				0, 1, 0);
		model = mb.end();
		break;
	    case "cylinder":
		// Use builder
		mat = new Material();
		Float width = ((Double) params.get("width")).floatValue();
		Float height = ((Double) params.get("height")).floatValue();
		Float depth = ((Double) params.get("depth")).floatValue();
		Integer divisions = ((Long) params.get("divisions")).intValue();
		flip = (Boolean) params.get("flip");

		model = mb.createCylinder(width, height, depth, divisions, flip, mat, attributes);

		break;
	    }
	}
	return model;
    }

    private String getKey(String shape, Map<String, Object> params, int attributes) {
	String key = shape + "-" + attributes;
	Set<String> keys = params.keySet();
	Object[] par = keys.toArray();
	for (int i = 0; i < par.length; i++) {
	    key += "-" + par[i].toString();
	}
	return key;

    }

    public void dispose() {
	Collection<Model> models = modelCache.values();
	for (Model model : models) {
	    model.dispose();
	}
    }
}
