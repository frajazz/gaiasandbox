package gaia.cu9.ari.gaiaorbit.scenegraph.component;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;

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

    public ModelComponent() {
    }

    public ModelComponent(boolean initEnvironment) {
	if (initEnvironment) {
	    env = new Environment();
	    env.set(ambient);
	    // Direction from Sun to Earth
	    dlight = new DirectionalLight();
	    dlight.color.set(1f, 1f, 1f, 0f);
	    env.add(dlight);
	}
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

}
