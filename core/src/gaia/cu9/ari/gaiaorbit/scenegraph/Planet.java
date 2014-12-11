package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.data.AssetBean;
import gaia.cu9.ari.gaiaorbit.render.IAtmosphereRenderable;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.override.AtmosphereAttribute;
import gaia.cu9.ari.gaiaorbit.util.override.Vector3Attribute;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class Planet extends ModelBody implements IAtmosphereRenderable {

    private static final float TH_ANGLE_NONE = ModelBody.TH_ANGLE_POINT / 1e5f;
    private static final float TH_ANGLE_POINT = ModelBody.TH_ANGLE_POINT / 1e3f;
    private static final float TH_ANGLE_SHADER = ModelBody.TH_ANGLE_POINT / 3f;

    @Override
    public float THRESHOLD_ANGLE_NONE() {
	return TH_ANGLE_NONE;
    }

    @Override
    public float THRESHOLD_ANGLE_POINT() {
	return TH_ANGLE_POINT;
    }

    @Override
    public float THRESHOLD_ANGLE_SHADER() {
	return TH_ANGLE_SHADER;
    }

    /**
     * Above this angle the hi-resolution texture is loaded and applied (if any)
     */
    private static final float HIRES_ANGLE_THRESHOLD = (float) Math.toRadians(20);

    private static AssetManager manager;

    public String texture, textureHires, textureNight, textureSpecular, textureNormal;
    public int quality;
    private boolean hiresTexFlag = false;
    Texture lo_resTex, hi_resTex;

    ICamera camera;

    /** RING **/
    private String textureRing;
    private int ringDivisions;
    private float ringInnerrad, ringOuterrad;

    /** ATMOSPHERES **/
    boolean atmosphere = false;
    int atmQuality;
    float atmSize;
    ModelComponent atmmc;
    Matrix4 localTransformAtm;
    float[] wavelengths;
    float m_fInnerRadius;
    float m_fOuterRadius;
    float m_fAtmosphereHeight;
    float m_Kr, m_Km;

    public Planet() {
	super();
    }

    @Override
    public void initialize() {
	super.initialize();

	if (texture != null)
	    AssetBean.addAsset(texture, Texture.class, textureParams);
	if (textureNormal != null)
	    AssetBean.addAsset(textureNormal, Texture.class, textureParams);
	if (textureSpecular != null)
	    AssetBean.addAsset(textureSpecular, Texture.class, textureParams);
	if (textureNight != null)
	    AssetBean.addAsset(textureNight, Texture.class, textureParams);
	if (textureRing != null)
	    AssetBean.addAsset(textureRing, Texture.class, textureParams);

    }

    @Override
    public void doneLoading(AssetManager manager) {
	if (Planet.manager == null) {
	    Planet.manager = manager;
	}

	Model planetModel = null;
	Material material = null;
	if (manager.isLoaded(model)) {
	    planetModel = manager.get(model, Model.class);
	    if (planetModel.materials.size == 0) {
		material = new Material();
		planetModel.materials.add(material);
	    } else {
		material = planetModel.materials.first();
	    }
	} else {
	    // Initialize planet model
	    if (textureRing != null) {
		// Create sphere with ring
		// Ring material
		Material ringMat = new Material();
		Texture tex = manager.get(textureRing, Texture.class);
		ringMat.set(new TextureAttribute(TextureAttribute.Diffuse, tex));
		ringMat.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));

		material = new Material();
		planetModel = ModelCache.cache.mb.createSphereRing(1, quality, quality, ringInnerrad, ringOuterrad, ringDivisions,
			material, ringMat, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
	    } else {
		// Regular sphere
		planetModel = ModelCache.cache.getModel("sphere", quality, 1, false, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
		material = planetModel.materials.first();
	    }
	}
	material.clear();

	// Set up material
	if (atmosphere)
	    setUpAtmosphericScatteringMaterial(material);

	if (texture != null) {
	    lo_resTex = manager.get(texture, Texture.class);
	    material.set(new TextureAttribute(TextureAttribute.Diffuse, lo_resTex));
	} else {
	    // If there is no diffuse texture, we add a colour
	    material.set(new ColorAttribute(ColorAttribute.Diffuse, cc[0], cc[1], cc[2], cc[3]));
	}
	if (textureNormal != null) {
	    Texture tex = manager.get(textureNormal, Texture.class);
	    material.set(new TextureAttribute(TextureAttribute.Normal, tex));
	}
	if (textureSpecular != null) {
	    Texture tex = manager.get(textureSpecular, Texture.class);
	    material.set(new TextureAttribute(TextureAttribute.Specular, tex));
	}
	if (textureNight != null) {
	    Texture tex = manager.get(textureNight, Texture.class);
	    material.set(new TextureAttribute(TextureAttribute.Emissive, tex));
	}
	// CREATE MAIN MODEL INSTANCE
	mc.instance = new ModelInstance(planetModel, this.localTransform);

	if (atmosphere) {
	    atmmc = new ModelComponent(false);
	    localTransformAtm = new Matrix4();
	    // Initialize atmosphere model
	    Model atmosphereModel = ModelCache.cache.getModel("sphere", atmQuality, 2, true, Usage.Position | Usage.Normal);
	    Material atmMat = atmosphereModel.materials.first();
	    atmMat.clear();
	    setUpAtmosphericScatteringMaterial(atmMat);
	    atmMat.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));

	    // CREATE ATMOSPHERE MODEL
	    atmmc.instance = new ModelInstance(atmosphereModel, this.localTransformAtm);
	}

	// INITIALIZE COORDINATES
	if (coordinates != null) {
	    coordinates.initialize(sg.getNode(orbitName));
	}

    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
	super.updateLocal(time, camera);
	// Check texture
	if (mc != null) {
	    if (!hiresTexFlag && this.viewAngle > HIRES_ANGLE_THRESHOLD * camera.getFovFactor() && textureHires != null) {
		// LOAD hi-res texture
		manager.load(textureHires, Texture.class, textureParams);
		hiresTexFlag = true;
	    } else if (hiresTexFlag && this.viewAngle <= HIRES_ANGLE_THRESHOLD * camera.getFovFactor() && hi_resTex != null) {
		// UNLOAD hi-res texture loaded
		for (Material mat : mc.instance.materials) {
		    mat.set(new TextureAttribute(TextureAttribute.Diffuse, lo_resTex));
		}
		Gdx.app.postRunnable(new Runnable() {
		    @Override
		    public void run() {
			manager.unload(textureHires);
		    }
		});

		hi_resTex = null;
		hiresTexFlag = false;
	    } else if (hiresTexFlag && hi_resTex == null) {
		// Check the loading, add the texture if loaded
		if (manager.isLoaded(textureHires)) {
		    hi_resTex = manager.get(textureHires, Texture.class);
		    for (Material mat : mc.instance.materials) {
			mat.set(new TextureAttribute(TextureAttribute.Diffuse, hi_resTex));
		    }
		}
	    }
	}
	this.camera = camera;
    }

    @Override
    protected void updateLocalTransform() {
	super.updateLocalTransform();
	if (atmosphere) {
	    localTransformAtm.set(transform.getMatrix().valuesf()).scl(atmSize);
	}
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time) {
	forceUpdateLocalValues(time, false);
    }

    protected void forceUpdateLocalValues(ITimeFrameProvider time, boolean force) {
	if (time.getDt() != 0 || force) {
	    Vector3d aux3 = auxVector3d.get();
	    // Load this planet's spherical ecliptic coordinates into pos
	    coordinates.getEquatorialCartesianCoordinates(time.getTime(), pos);

	    // Convert to cartesian coordinates and put them in aux3 vector
	    Coordinates.cartesianToSpherical(pos, aux3);
	    posSph.set(AstroUtils.TO_DEG * aux3.x, AstroUtils.TO_DEG * aux3.y);

	    // Angle
	    // Angle at J2000 = rc.meridianAngle
	    long t = time.getTime().getTime() - AstroUtils.J2000_MS;
	    rc.angle = (rc.meridianAngle + rc.angularVelocity * t * Constants.MS_TO_H) % 360d;
	}
    }

    @Override
    public void render(Object... params) {
	Object first = params[0];
	if (!(first instanceof ModelBatch) || params.length == 2) {
	    super.render(params);
	} else {
	    // TODO fix this hack of byte parameter
	    if (params.length > 2)
		// Atmosphere rendering
		render((ModelBatch) first, (Float) params[1], (Byte) params[2]);
	}
    }

    /**
     * Renders model
     */
    @Override
    public void render(ModelBatch modelBatch, float alpha) {
	if (atmosphere && GlobalConf.instance.VISIBILITY[ComponentType.Atmospheres.ordinal()]) {
	    updateAtmosphericScatteringParams(mc.instance.materials.first(), alpha, true);
	} else {
	    removeAtmosphericScattering(mc.instance.materials.first());
	}
	mc.setTransparency(alpha * opacity);
	modelBatch.render(mc.instance, mc.env);
    }

    /**
     *  Renders atmosphere
     */
    @Override
    public void render(ModelBatch modelBatch, float alpha, byte b) {
	// Shader parameters
	updateAtmosphericScatteringParams(atmmc.instance.materials.first(), alpha, false);

	// Render atmosphere?
	modelBatch.render(atmmc.instance, mc.env);
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
	super.addToRenderLists(camera);
	// Add atmosphere to default render group if necessary
	if (atmosphere && isInRender(this, RenderGroup.MODEL_F)) {
	    addToRender(this, RenderGroup.MODEL_F_ATM);
	}
    }

    /**
     * Sets up the atmospheric scattering parameters to the given material
     * @param mat The material to set up.
     */
    private void setUpAtmosphericScatteringMaterial(Material mat) {
	float camHeight = 1f;
	m_Kr = 0.0025f; // Rayleigh scattering constant
	float m_Kr4PI = m_Kr * 4.0f * (float) Math.PI;
	m_Km = 0.001f; // Mie scattering constant
	float m_Km4PI = m_Km * 4.0f * (float) Math.PI;
	float m_ESun = 15.0f; // Sun brightness (almost) constant
	float m_g = -0.95f; // The Mie phase asymmetry factor
	m_fInnerRadius = this.size / 2f;
	m_fOuterRadius = this.atmSize;
	m_fAtmosphereHeight = m_fOuterRadius - m_fInnerRadius;
	float m_fScaleDepth = .25f;
	float m_fScale = 1.0f / (m_fAtmosphereHeight);
	float m_fScaleOverScaleDepth = m_fScale / m_fScaleDepth;
	int m_nSamples = 7;

	float[] m_fWavelength = wavelengths;
	float[] m_fWavelength4 = new float[3];
	m_fWavelength4[0] = (float) Math.pow(m_fWavelength[0], 4.0);
	m_fWavelength4[1] = (float) Math.pow(m_fWavelength[1], 4.0);
	m_fWavelength4[2] = (float) Math.pow(m_fWavelength[2], 4.0);

	mat.set(new AtmosphereAttribute(AtmosphereAttribute.Alpha, 1));
	mat.set(new AtmosphereAttribute(AtmosphereAttribute.ColorOpacity, 0));

	mat.set(new AtmosphereAttribute(AtmosphereAttribute.CameraHeight2, camHeight * camHeight));

	mat.set(new AtmosphereAttribute(AtmosphereAttribute.OuterRadius, m_fOuterRadius));
	mat.set(new AtmosphereAttribute(AtmosphereAttribute.OuterRadius2, m_fOuterRadius * m_fOuterRadius));

	mat.set(new AtmosphereAttribute(AtmosphereAttribute.InnerRadius, m_fInnerRadius));
	mat.set(new AtmosphereAttribute(AtmosphereAttribute.InnerRadius2, m_fInnerRadius * m_fInnerRadius));

	mat.set(new AtmosphereAttribute(AtmosphereAttribute.KrESun, m_Kr * m_ESun));
	mat.set(new AtmosphereAttribute(AtmosphereAttribute.KmESun, m_Km * m_ESun));

	mat.set(new AtmosphereAttribute(AtmosphereAttribute.Kr4PI, m_Kr4PI));
	mat.set(new AtmosphereAttribute(AtmosphereAttribute.Km4PI, m_Km4PI));

	mat.set(new AtmosphereAttribute(AtmosphereAttribute.Scale, m_fScale));
	mat.set(new AtmosphereAttribute(AtmosphereAttribute.ScaleDepth, m_fScaleDepth));
	mat.set(new AtmosphereAttribute(AtmosphereAttribute.ScaleOverScaleDepth, m_fScaleOverScaleDepth));

	mat.set(new AtmosphereAttribute(AtmosphereAttribute.nSamples, m_nSamples));
	mat.set(new AtmosphereAttribute(AtmosphereAttribute.fSamples, m_nSamples));

	mat.set(new AtmosphereAttribute(AtmosphereAttribute.G, m_g));
	mat.set(new AtmosphereAttribute(AtmosphereAttribute.G2, m_g * m_g));

	mat.set(new Vector3Attribute(Vector3Attribute.PlanetPos, new Vector3()));
	mat.set(new Vector3Attribute(Vector3Attribute.CameraPos, new Vector3()));
	mat.set(new Vector3Attribute(Vector3Attribute.LightPos, new Vector3()));
	mat.set(new Vector3Attribute(Vector3Attribute.InvWavelength, new Vector3(1.0f / m_fWavelength4[0],
		1.0f / m_fWavelength4[1], 1.0f / m_fWavelength4[2])));
    }

    private void removeAtmosphericScattering(Material mat) {
	mat.remove(AtmosphereAttribute.CameraHeight);
    }

    /**
     * Updates the atmospheric scattering shader parameters
     * @param mat The material to update.
     * @param alpha The opacity value.
     * @param ground Whether it is the ground shader or the atmosphere.
     */
    private void updateAtmosphericScatteringParams(Material mat, float alpha, boolean ground) {
	Vector3d aux3 = auxVector3d.get();
	this.transform.getTranslation(aux3);
	// Dist to planet
	float camHeight = (float) (aux3.len());
	float m_ESun = 15f;
	float camHeightGr = camHeight - m_fInnerRadius;
	float atmFactor = (m_fAtmosphereHeight - camHeightGr) / m_fAtmosphereHeight;

	if (!ground && camHeightGr < m_fAtmosphereHeight) {
	    // Camera inside atmosphere
	    m_ESun = Math.max(15, atmFactor * 35f);
	}

	float colorOpacity = Math.min(1.5f, Math.max(0f, atmFactor * 3));

	// These are here to get the desired effect inside the atmosphere
	mat.set(new AtmosphereAttribute(AtmosphereAttribute.KrESun, m_Kr * m_ESun));
	mat.set(new AtmosphereAttribute(AtmosphereAttribute.KmESun, m_Km * m_ESun));

	// Color opacity
	((AtmosphereAttribute) mat.get(AtmosphereAttribute.ColorOpacity)).value = colorOpacity;

	// Camera height
	if (mat.has(AtmosphereAttribute.CameraHeight))
	    ((AtmosphereAttribute) mat.get(AtmosphereAttribute.CameraHeight)).value = camHeight;
	else
	    mat.set(new AtmosphereAttribute(AtmosphereAttribute.CameraHeight, camHeight));

	((AtmosphereAttribute) mat.get(AtmosphereAttribute.CameraHeight2)).value = camHeight * camHeight;

	// Earth pos
	if (ground) {
	    // Camera position must be corrected using the rotation angle of the planet
	    aux3.rotate(rc.inclination + rc.axialTilt, 0, 0, 1).rotate(-rc.angle, 0, 1, 0);
	}
	((Vector3Attribute) mat.get(Vector3Attribute.PlanetPos)).value.set(aux3.valuesf());
	// CameraPos = -EarthPos
	aux3.scl(-1f);

	((Vector3Attribute) mat.get(Vector3Attribute.CameraPos)).value.set(aux3.valuesf());

	// Light position respect the earth: LightPos = SunPos - EarthPos
	this.parent.transform.addTranslationTo(aux3).nor();
	if (ground) {
	    // Camera position must be corrected using the rotation angle of the planet
	    aux3.rotate(rc.inclination + rc.axialTilt, 0, 0, 1).rotate(-rc.angle, 0, 1, 0);
	}
	((Vector3Attribute) mat.get(Vector3Attribute.LightPos)).value.set(aux3.valuesf());

	// Alpha value
	((AtmosphereAttribute) mat.get(AtmosphereAttribute.Alpha)).value = alpha;
    }

    public void setModelQuality(Float modelQuality) {
	this.quality = modelQuality.intValue();
    }

    public void setModelTexture(String modelTexture) {
	this.texture = modelTexture;
    }

    public void setModelTextureHires(String modelTextureHires) {
	this.textureHires = modelTextureHires;
    }

    public void setModelTextureNight(String modelTexture) {
	this.textureNight = modelTexture;
    }

    public void setModelTextureSpecular(String modelTexture) {
	this.textureSpecular = modelTexture;
    }

    public void setModelTextureNormal(String modelTexture) {
	this.textureNormal = modelTexture;
    }

    public void setRingTexture(String ringTexture) {
	this.textureRing = ringTexture;
    }

    public void setAtmosphere(Boolean atm) {
	this.atmosphere = atm;
    }

    public void setAtmosphereQuality(Float atmQuality) {
	this.atmQuality = atmQuality.intValue();
    }

    public void setAtmosphereSize(Float size) {
	this.atmSize = (float) (size * Constants.KM_TO_U);
    }

    @Override
    public boolean hasAtmosphere() {
	return atmosphere;
    }

    @Override
    protected float labelFactor() {
	return 2e1f;
    }

    public void setRingDivisions(Float ringDivisions) {
	this.ringDivisions = ringDivisions.intValue();
    }

    public void setRingInnerrad(Float ringInnerrad) {
	this.ringInnerrad = ringInnerrad;
    }

    public void setRingOuterrad(Float ringOuterrad) {
	this.ringOuterrad = ringOuterrad;
    }

    public void setAtmosphereWavelengths(String wavelengths) {
	String ws[] = wavelengths.split("\\s+");
	this.wavelengths = new float[ws.length];
	for (int i = 0; i < ws.length; i++) {
	    this.wavelengths[i] = Float.parseFloat(ws[i]);
	}
    }

    public void dispose() {

    }

}
