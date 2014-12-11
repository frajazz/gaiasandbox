package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.util.g3d.ModelBuilder2;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

    public Model getModel(String shape, int quality, float diameter, boolean flip, int attributes) {
	String key = shape + "-" + quality + "-" + (int) diameter + "-" + attributes + "-" + flip;
	Model model = null;
	if (modelCache.containsKey(key)) {
	    model = modelCache.get(key);
	} else {
	    switch (shape) {
	    case "sphere":
		model = mb.createSphere(diameter, quality, quality, flip, new Material(), attributes);
		modelCache.put(key, model);
		break;
	    }
	}
	return model;
    }

    public void dispose() {
	Collection<Model> models = modelCache.values();
	for (Model model : models) {
	    model.dispose();
	}
    }
}
