package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.data.AssetBean;
import gaia.cu9.ari.gaiaorbit.data.FileLocator;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.RotationComponent;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.coord.IBodyCoordinates;
import gaia.cu9.ari.gaiaorbit.util.coord.vsop87.DummyVSOP87;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Matrix4;

public abstract class ModelBody extends CelestialBody {
    protected static final float TH_ANGLE_POINT = (float) Math.toRadians(0.35);

    /** Coordinates provider **/
    protected IBodyCoordinates coordinates;

    /** Name of orbit object, if any **/
    protected String orbitName;

    /**
     * Angle limit for rendering as point. If angle is any bigger, we render with shader.
     * Returns Math.toRadians(0.35)
     */
    public float THRESHOLD_ANGLE_POINT() {
	return TH_ANGLE_POINT;
    }

    public String model;
    public ModelComponent mc;

    public ModelBody() {
	super();
	localTransform = new Matrix4();
	orientation = new Matrix4d();
	rc = new RotationComponent();
    }

    public void initialize() {
	if (FileLocator.exists(model)) {
	    AssetBean.addAsset(model, Model.class);
	}
	mc = new ModelComponent(true);
	setColor2Data();
	setDerivedAttributes();
    }

    @Override
    public void doneLoading(AssetManager manager) {
	Model mod = manager.get(model, Model.class);
	mc.instance = new ModelInstance(mod, this.localTransform);
    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
	super.updateLocal(time, camera);
	// Update light with global position
	if (mc != null) {
	    mc.dlight.direction.set(transform.getTranslationf());
	    mc.dlight.direction.add((float) camera.getPos().x, (float) camera.getPos().y, (float) camera.getPos().z);
	}
	updateLocalTransform();
    }

    /**
     * Update the local transform with the transform and the rotations/scales necessary.
     * Override if your model contains more than just the position and size.
     */
    protected void updateLocalTransform() {
	// Scale + Rotate + Tilt + Translate 
	float[] trnsltn = transform.getTranslationf();
	localTransform.idt().translate(trnsltn[0], trnsltn[1], trnsltn[2]).scl(size).rotate(0, 1, 0, (float) rc.ascendingNode).rotate(0, 0, 1, (float) (rc.inclination + rc.axialTilt)).rotate(0, 1, 0, (float) rc.angle);
	if (children != null)
	    orientation.idt().rotate(0, 1, 0, (float) rc.ascendingNode).rotate(0, 0, 1, (float) (rc.inclination + rc.axialTilt));
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
	camera.checkClosest(this);
	if (viewAngle >= THRESHOLD_ANGLE_NONE() * camera.getFovFactor()) {
	    if (viewAngle < THRESHOLD_ANGLE_POINT() * camera.getFovFactor()) {
		addToRender(this, RenderGroup.POINT);
	    } else {
		float shaderCamera = THRESHOLD_ANGLE_SHADER() * camera.getFovFactor();
		float shaderCameraOverlap = shaderCamera * ModelBody.SHADER_MODEL_OVERLAP_FACTOR;
		if (viewAngle < shaderCameraOverlap) {
		    addToRender(this, RenderGroup.SHADER_F);
		}
		if (viewAngle > shaderCamera) {
		    addToRender(this, RenderGroup.MODEL_F);
		}
	    }
	    if (renderLabel()) {
		addToRender(this, RenderGroup.LABEL);
	    }
	}
    }

    @Override
    public float getInnerRad() {
	return .5f;
	//return .02f;
    }

    protected void setDerivedAttributes() {
	this.flux = (float) Math.pow(10, -absmag / 2.5f);
    }

    public void setModel(String model) {
	this.model = model;
    }

    public void dispose() {
	mc.dispose();
    }

    @Override
    public void render(ModelBatch modelBatch, float alpha) {
	mc.setTransparency(alpha * opacity);
	modelBatch.render(mc.instance, mc.env);
    }

    public boolean withinMagLimit() {
	return this.absmag <= GlobalConf.instance.LIMIT_MAG_RUNTIME;
    }

    @Override
    protected float labelMax() {
	return 2.5e-4f;
    }

    public void setCoordinates(String coordinatesClass) {
	String className = "gaia.cu9.ari.gaiaorbit.util.coord." + coordinatesClass;
	Class<?> clazz = null;
	try {
	    clazz = Class.forName(className);
	} catch (ClassNotFoundException e) {
	    clazz = DummyVSOP87.class;
	}
	try {
	    coordinates = (IBodyCoordinates) clazz.newInstance();
	} catch (InstantiationException | IllegalAccessException e) {
	    Gdx.app.error(this.getClass().getSimpleName(), e.getLocalizedMessage());
	}

    }

    public void setCoordinatesOrbit(String coordinatesOrbit) {
	this.orbitName = coordinatesOrbit;
    }

    @Override
    public <T extends SceneGraphNode> T getSimpleCopy() {
	ModelBody mb = (ModelBody) super.getSimpleCopy();
	mb.coordinates = this.coordinates;
	return (T) mb;
    }

}