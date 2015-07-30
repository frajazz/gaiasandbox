package gaia.cu9.ari.gaiaorbit.scenegraph.component;

import gaia.cu9.ari.gaiaorbit.data.AssetBean;

import java.util.Map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
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
        textureParams.genMipMaps = false;
        textureParams.magFilter = TextureFilter.Linear;
        //textureParams.minFilter = TextureFilter.MipMapLinearNearest;
        textureParams.minFilter = TextureFilter.Linear;
    }
    /**
     * Above this angle the hi-resolution texture is loaded and applied (if any)
     */
    private static final float HIRES_ANGLE_THRESHOLD = (float) Math.toRadians(20);

    public String base, hires, specular, normal, night, ring;
    public Texture baseTex;
    @Deprecated
    public Texture hi_resTex;

    @Deprecated
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
     * Initializes the materials by binding the necessary textures to them.
     * @param manager The asset manager.
     * @param materials A map with at least one material under the key "base".
     * @param cc Plain color used if there is no texture.
     */
    public void initMaterial(AssetManager manager, Map<String, Material> materials, float[] cc) {
        Material material = materials.get("base");
        if (base != null) {
            baseTex = manager.get(base, Texture.class);
            material.set(new TextureAttribute(TextureAttribute.Diffuse, baseTex));
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
        if (materials.containsKey("ring")) {
            // Ring material
            Material ringMat = materials.get("ring");
            Texture tex = manager.get(ring, Texture.class);
            ringMat.set(new TextureAttribute(TextureAttribute.Diffuse, tex));
            ringMat.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
        }
    }

    public void setBase(String base) {
        this.base = base;
    }

    /**
     * @deprecated Hires textures no longer supported. Using mipmapping.
     * @param hires
     */
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
