package gaia.cu9.ari.gaiaorbit.scenegraph.component;

import gaia.cu9.ari.gaiaorbit.data.AssetBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;

/**
 * A basic component that contains the info on the textures.
 * @author Toni Sagrista
 *
 */
public class TextureComponent {
    /** Default texture parameters **/
    protected static final TextureParameter textureParams;
    static {
	textureParams = new TextureParameter();
	textureParams.magFilter = TextureFilter.Linear;
	textureParams.minFilter = TextureFilter.Linear;
    }
    /**
     * Above this angle the hi-resolution texture is loaded and applied (if any)
     */
    private static final float HIRES_ANGLE_THRESHOLD = (float) Math.toRadians(20);

    public String base, hires, specular, normal, night, ring;
    public Texture lo_resTex, hi_resTex;
    public boolean hiresTexFlag = false;

    public TextureComponent() {

    }

    public void initialize() {
	if (base != null)
	    AssetBean.addAsset(base, Texture.class, textureParams);
	if (normal != null)
	    AssetBean.addAsset(normal, Texture.class, textureParams);
	if (specular != null)
	    AssetBean.addAsset(specular, Texture.class, textureParams);
	if (night != null)
	    AssetBean.addAsset(night, Texture.class, textureParams);
	if (ring != null)
	    AssetBean.addAsset(ring, Texture.class, textureParams);
    }

    /**
     * Updates the texture of the associated model to a high/low texture if necessary.
     * @param manager
     * @param instance
     * @param viewAngle
     * @param camera
     */
    public void updateTexture(final AssetManager manager, ModelInstance instance, float viewAngle, ICamera camera) {
	if (!hiresTexFlag && viewAngle > HIRES_ANGLE_THRESHOLD * camera.getFovFactor() && hires != null) {
	    // LOAD hi-res texture
	    manager.load(hires, Texture.class, textureParams);
	    hiresTexFlag = true;
	} else if (hiresTexFlag && viewAngle <= HIRES_ANGLE_THRESHOLD * camera.getFovFactor() && hi_resTex != null) {
	    // UNLOAD hi-res texture loaded
	    for (Material mat : instance.materials) {
		mat.set(new TextureAttribute(TextureAttribute.Diffuse, lo_resTex));
	    }
	    Gdx.app.postRunnable(new Runnable() {
		@Override
		public void run() {
		    manager.unload(hires);
		}
	    });

	    hi_resTex = null;
	    hiresTexFlag = false;
	} else if (hiresTexFlag && hi_resTex == null) {
	    // Check the loading, add the texture if loaded
	    if (manager.isLoaded(hires)) {
		hi_resTex = manager.get(hires, Texture.class);
		for (Material mat : instance.materials) {
		    mat.set(new TextureAttribute(TextureAttribute.Diffuse, hi_resTex));
		}
	    }
	}
    }

    public void initMaterial(AssetManager manager, Material material, float[] cc) {
	if (base != null) {
	    lo_resTex = manager.get(base, Texture.class);
	    material.set(new TextureAttribute(TextureAttribute.Diffuse, lo_resTex));
	} else {
	    // If there is no diffuse texture, we add a colour
	    material.set(new ColorAttribute(ColorAttribute.Diffuse, cc[0], cc[1], cc[2], cc[3]));
	}
	if (normal != null) {
	    Texture tex = manager.get(normal, Texture.class);
	    material.set(new TextureAttribute(TextureAttribute.Normal, tex));
	}
	if (specular != null) {
	    Texture tex = manager.get(specular, Texture.class);
	    material.set(new TextureAttribute(TextureAttribute.Specular, tex));
	}
	if (night != null) {
	    Texture tex = manager.get(night, Texture.class);
	    material.set(new TextureAttribute(TextureAttribute.Emissive, tex));
	}
    }

    public void setBase(String base) {
	this.base = base;
    }

    public void setHires(String hires) {
	this.hires = hires;
    }

    public void setSpecular(String specular) {
	this.specular = specular;
    }

    public void setNormal(String normal) {
	this.normal = normal;
    }

    public void setNight(String night) {
	this.night = night;
    }

    public void setRing(String ring) {
	this.ring = ring;
    }

}
