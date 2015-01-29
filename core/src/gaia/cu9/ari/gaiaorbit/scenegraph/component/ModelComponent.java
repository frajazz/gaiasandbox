package gaia.cu9.ari.gaiaorbit.scenegraph.component;

import gaia.cu9.ari.gaiaorbit.data.AssetBean;
import gaia.cu9.ari.gaiaorbit.data.FileLocator;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;

import java.util.Map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Matrix4;

public class ModelComponent {
    private static ColorAttribute ambient;

    static {
	ambient = new ColorAttribute(ColorAttribute.AmbientLight, .0f, .0f, .0f, 1f);
    }

    public static void toggleAmbientLight(boolean on) {
	if (on) {
	    ambient.color.set(.7f, .7f, .7f, 1f);
	} else {
	    ambient.color.set(0f, 0f, 0f, 1f);
	}
    }

    /**
     * Sets the ambient light
     * @param level Ambient light level between 0 and 1
     */
    public static void setAmbientLight(float level) {
	ambient.color.set(level, level, level, 1f);
    }

    public ModelInstance instance;
    public Environment env;
    /** Directional light **/
    public DirectionalLight dlight;

    public Map<String, Object> params;

    public String type, model;

    /**
     * COMPONENTS
     */
    // Texture
    public TextureComponent tc;
    // Ring
    public RingComponent rc;

    public ModelComponent() {
	this(true);
    }

    public ModelComponent(Boolean initEnvironment) {
	if (initEnvironment) {
	    env = new Environment();
	    env.set(ambient);
	    // Direction from Sun to Earth
	    dlight = new DirectionalLight();
	    dlight.color.set(1f, 1f, 1f, 0f);
	    env.add(dlight);
	}
    }

    public void initialize() {
	if (model != null && FileLocator.exists(model)) {
	    AssetBean.addAsset(model, Model.class);
	}

	if (tc != null) {
	    tc.initialize();
	}
    }

    public void doneLoading(AssetManager manager, Matrix4 localTransform, float[] cc) {

	Model finalModel = null;
	Material material = null;
	if (manager.isLoaded(model)) {
	    // Model comes from file (probably .obj or .g3db)
	    finalModel = manager.get(model, Model.class);
	    if (finalModel.materials.size == 0) {
		material = new Material();
		finalModel.materials.add(material);
	    } else {
		material = finalModel.materials.first();
	    }
	} else {
	    // We create the model
	    if (rc != null) {
		// Model with ring
		Material ringMat = new Material();
		Texture tex = manager.get(tc.ring, Texture.class);
		ringMat.set(new TextureAttribute(TextureAttribute.Diffuse, tex));
		ringMat.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));

		material = new Material();
		int quality = ((Long) params.get("quality")).intValue();
		finalModel = ModelCache.cache.mb.createSphereRing(1, quality, quality, rc.innerRadius, rc.outerRadius, rc.divisions,
			material, ringMat, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
	    } else {
		// Regular type
		finalModel = ModelCache.cache.getModel(type, params, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		material = finalModel.materials.first();
	    }
	}
	material.clear();

	// INITIALIZE MATERIAL
	if (tc != null) {
	    tc.initMaterial(manager, material, cc);
	}

	// CREATE MAIN MODEL INSTANCE
	instance = new ModelInstance(finalModel, localTransform);
    }

    public void addDirectionalLight(float r, float g, float b, float x, float y, float z) {
	DirectionalLight dl = new DirectionalLight();
	dl.set(r, g, b, x, y, z);
	env.add(dl);
    }

    public void dispose() {
	if (instance != null && instance.model != null)
	    instance.model.dispose();
    }

    public void setTransparency(float alpha) {
	if (instance != null) {
	    for (int i = 0; i < instance.materials.size; i++) {
		Material mat = instance.materials.get(i);
		BlendingAttribute ba = null;
		if (mat.has(BlendingAttribute.Type)) {
		    ba = (BlendingAttribute) mat.get(BlendingAttribute.Type);
		} else {
		    ba = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		    mat.set(ba);
		}
		ba.opacity = alpha;
	    }
	}
    }

    public void setTransparencyColor(float alpha) {
	if (instance != null) {
	    ((ColorAttribute) instance.materials.get(0).get(ColorAttribute.Diffuse)).color.a = alpha;
	}
    }

    public void setType(String type) {
	this.type = type;
    }

    public void setTexture(TextureComponent tc) {
	this.tc = tc;
    }

    public void setRing(RingComponent rc) {
	this.rc = rc;
    }

    public void setModel(String model) {
	this.model = model;
    }

    public void setParams(Map<String, Object> params) {
	this.params = params;
    }

}
