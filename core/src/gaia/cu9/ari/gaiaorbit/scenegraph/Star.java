package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.Pair;
import gaia.cu9.ari.gaiaorbit.util.color.ColourUtils;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector2d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.TimeUtils;

import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.math.Matrix4;

public class Star extends CelestialBody {
    private static final float TH_ANGLE_POINT = (float) Math.toRadians(2e-7f);
    private static final float TH_ANGLE_NONE = 0;
    private static ThreadLocal<Random> rnd = new ThreadLocal<Random>() {
	@Override
	public Random initialValue() {
	    return new Random();
	}
    };

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
	return 0;
    }

    /** Has the model used to represent the star **/
    private static ModelComponent mc;
    private static Matrix4 modelTransform;

    double computedSize;
    double radius;
    boolean randomName = false;
    double modelDistance;

    public static void initModel() {
	if (mc == null) {
	    Texture tex = new Texture(Gdx.files.internal(GlobalConf.TEX_FOLDER + "star.jpg"));
	    Texture lut = new Texture(Gdx.files.internal(GlobalConf.TEX_FOLDER + "lut.jpg"));
	    tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);

	    Map<String, Object> params = new TreeMap<String, Object>();
	    params.put("quality", 120l);
	    params.put("diameter", 1d);
	    params.put("flip", false);

	    Pair<Model, Map<String, Material>> pair = ModelCache.cache.getModel("sphere", params, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
	    Model model = pair.getFirst();
	    Material mat = pair.getSecond().get("base");
	    mat.clear();
	    mat.set(new TextureAttribute(TextureAttribute.Diffuse, tex));
	    mat.set(new TextureAttribute(TextureAttribute.Normal, lut));
	    // Only to activate view vector (camera position)
	    mat.set(new TextureAttribute(TextureAttribute.Specular, lut));
	    mat.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
	    modelTransform = new Matrix4();
	    mc = new ModelComponent(false);
	    mc.env = new Environment();
	    mc.env.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
	    mc.env.set(new FloatAttribute(FloatAttribute.Shininess, 0f));
	    mc.instance = new ModelInstance(model, modelTransform);
	}
    }

    public Star() {
	this.parentName = ROOT_NAME;
    }

    public Star(Vector3d pos, float appmag, float absmag, float colorbv, String name, long starid) {
	this();
	this.pos = pos;
	this.name = name;
	this.appmag = appmag;
	this.absmag = absmag;
	this.colorbv = colorbv;
	this.id = starid;

	if (this.name == null) {
	    randomName = true;
	    this.name = "star_" + rnd.get().nextInt(10000000);
	}
    }

    public Star(Vector3d pos, float appmag, float absmag, float colorbv, String name, double ra, double dec, long starid) {
	this(pos, appmag, absmag, colorbv, name, starid);
	this.posSph = new Vector2d(ra, dec);

    }

    @Override
    public void initialize() {
	setDerivedAttributes();
	ct = ComponentType.Stars;
	// Relation between our star size and actual star size (normalized for the Sun, 1391600 Km of diameter
	radius = size * Constants.STAR_SIZE_FACTOR;
	modelDistance = 172.4643429 * radius;
    }

    private void setDerivedAttributes() {
	this.flux = (float) Math.pow(10, -absmag / 2.5f);
	setRGB(colorbv);

	// Calculate size
	size = (float) Math.min((Math.pow(flux, 0.5f) * Constants.PC_TO_U * 0.16f), 1e8f);
    }

    @Override
    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera) {
	update(time, parentTransform, camera, 1f);
    }

    /**
     * Re-implementation of update method of {@link CelestialBody} and {@link SceneGraphNode} 
     * that ignores the stars that are behind the camera.
     */
    @Override
    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera, float opacity) {
	if (appmag <= GlobalConf.instance.LIMIT_MAG_RUNTIME) {
	    this.opacity = opacity;
	    transform.position.set(parentTransform.position).add(pos);

	    distToCamera = (float) transform.position.len();
	    boolean visible = computeVisible(time, camera, GlobalConf.instance.COMPUTE_GAIA_SCAN) || camera.isFocus(this);

	    if (visible && !copy) {
		viewAngle = (float) Math.atan((getRadius()) / distToCamera) / camera.getFovFactor();
		viewAngleApparent = (float) Math.atan((getRadius() * GlobalConf.instance.STAR_BRIGHTNESS) / distToCamera) / camera.getFovFactor();
		addToRenderLists(camera);
	    }

	    if (distToCamera < size) {
		if (!expandedFlag) {
		    // Update computed to true
		    setComputedFlag(children, true);
		}
		// Compute nested
		if (children != null) {
		    for (int i = 0; i < children.size(); i++) {
			SceneGraphNode child = children.get(i);
			child.update(time, parentTransform, camera);
		    }
		}
		expandedFlag = true;
	    } else {
		if (expandedFlag) {
		    // Set computed to false
		    setComputedFlag(children, false);
		}
		expandedFlag = false;
	    }
	}
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
	if (viewAngleApparent >= THRESHOLD_ANGLE_NONE() * camera.getFovFactor()) {
	    if (camera.getCurrent() instanceof FovCamera) {
		// Only shader for FovCamera
		addToRender(this, RenderGroup.SHADER);
	    } else {
		if (viewAngleApparent < THRESHOLD_ANGLE_POINT() * camera.getFovFactor()) {
		    // Update opacity
		    opacity *= MathUtilsd.lint(viewAngleApparent, 0, THRESHOLD_ANGLE_POINT(), Constants.pointAlphaMin, Constants.pointAlphaMax);
		    addToRender(this, RenderGroup.POINT);
		} else {
		    addToRender(this, RenderGroup.POINT);
		    addToRender(this, RenderGroup.SHADER);
		    if (distToCamera < modelDistance) {
			camera.checkClosest(this);
			addToRender(this, RenderGroup.MODEL_S);
		    }
		}
	    }
	    if (renderLabel()) {
		addToRender(this, RenderGroup.LABEL);
	    }
	}

    }

    @Override
    public void render(ModelBatch modelBatch, float alpha) {
	mc.setTransparency(alpha * opacity);
	((ColorAttribute) mc.env.get(ColorAttribute.AmbientLight)).color.set(cc[0], cc[1], cc[2], 1f);
	((FloatAttribute) mc.env.get(FloatAttribute.Shininess)).value = TimeUtils.getRunningTimeSecs();
	// Local transform
	mc.instance.transform.set(transform.getMatrix().valuesf()).scl(getRadius() * 2);
	modelBatch.render(mc.instance, mc.env);
    }

    /**
     * Sets the color
     * @param bv B-V color index
     */
    private void setRGB(float bv) {
	cc = ColourUtils.BVtoRGB(bv);
	setColor2Data();
    }

    @Override
    public float getInnerRad() {
	return 0.04f;
    }

    @Override
    public float getRadius() {
	return (float) radius;
    }

    public boolean isStar() {
	return true;
    }

    @Override
    public float labelSizeConcrete() {
	return (float) computedSize;
    }

    @Override
    protected float labelFactor() {
	return 2e-1f;
    }

    @Override
    protected float labelMax() {
	return 0.02f;
    }

    public float getFuzzyRenderSize(ICamera camera) {
	computedSize = this.size;
	if (viewAngle > Constants.TH_ANGLE_DOWN / camera.getFovFactor()) {
	    double dist = distToCamera;
	    if (viewAngle > Constants.TH_ANGLE_UP / camera.getFovFactor()) {
		dist = getRadius() / Constants.TAN_TH_ANGLE_UP;
	    }
	    computedSize = dist * Constants.TAN_TH_ANGLE_DOWN * Constants.STAR_SIZE_FACTOR_INV;

	}
	computedSize *= GlobalConf.instance.STAR_BRIGHTNESS;
	return (float) computedSize;
    }

    @Override
    public void doneLoading(AssetManager manager) {
	initModel();
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time) {
    }

}
