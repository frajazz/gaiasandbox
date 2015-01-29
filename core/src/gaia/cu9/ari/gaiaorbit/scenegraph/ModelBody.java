package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;

public abstract class ModelBody extends CelestialBody {
    protected static final float TH_ANGLE_POINT = (float) Math.toRadians(0.35);

    /**
     * Angle limit for rendering as point. If angle is any bigger, we render with shader.
     * Returns Math.toRadians(0.35)
     */
    public float THRESHOLD_ANGLE_POINT() {
	return TH_ANGLE_POINT;
    }

    /** MODEL **/
    public ModelComponent mc;

    public ModelBody() {
	super();
	localTransform = new Matrix4();
	orientation = new Matrix4d();
    }

    public void initialize() {
	if (mc != null) {
	    mc.initialize();
	}
	setColor2Data();
	setDerivedAttributes();
    }

    @Override
    public void doneLoading(AssetManager manager) {
	super.doneLoading(manager);
	if (mc != null) {
	    mc.doneLoading(manager, localTransform, cc);
	}
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

    public void setModel(ModelComponent mc) {
	this.mc = mc;
    }

}
