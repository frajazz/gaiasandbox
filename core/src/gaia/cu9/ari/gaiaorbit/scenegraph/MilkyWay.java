package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.render.ILabelRenderable;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class MilkyWay extends Blob implements IModelRenderable, ILabelRenderable {
    float[] labelColour = new float[] { 1f, 1f, 1f, 1f };
    ModelComponent mc;
    String model, transformName;
    Matrix4 coordinateSystem;

    public MilkyWay() {
	super();
	localTransform = new Matrix4();

	lowAngle = (float) Math.toRadians(60);
	highAngle = (float) Math.toRadians(75.51);
    }

    public void initialize() {
	mc.initialize();
	mc.env.set(new ColorAttribute(ColorAttribute.AmbientLight, cc[0], cc[1], cc[2], 1));
    }

    @Override
    public void doneLoading(AssetManager manager) {
	super.doneLoading(manager);

	// Set static coordinates to position
	coordinates.getEquatorialCartesianCoordinates(null, pos);

	// Initialize transform
	if (transformName != null) {
	    coordinateSystem = new Matrix4();
	    Class<Coordinates> c = Coordinates.class;
	    try {
		Method m = c.getMethod(transformName);
		Matrix4d trf = (Matrix4d) m.invoke(null);
		coordinateSystem.set(trf.valuesf());
	    } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		Gdx.app.error(Mw.class.getName(), "Error getting/invoking method Coordinates." + transformName + "()");
	    }
	} else {
	    // Equatorial, nothing
	}
	// Model
	mc.doneLoading(manager, localTransform, null);
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
	if (viewAngle <= highAngle) {
	    addToRender(this, RenderGroup.MODEL_F);
	    if (renderLabel()) {
		addToRender(this, RenderGroup.LABEL);
	    }
	}
    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
	super.updateLocal(time, camera);
	// Directional light comes from camera
	if (mc != null) {
	    float[] camdir = camera.getDirection().valuesf();
	    mc.dlight.direction.set(-camdir[0], -camdir[1], -camdir[2]);
	}
	updateLocalTransform();
    }

    /**
     * Update the local transform with the transform and the rotations/scales necessary.
     * Override if your model contains more than just the position and size.
     */
    protected void updateLocalTransform() {
	// Scale + Rotate + Tilt + Translate 
	float[] trans = transform.getMatrix().getTranslationf();
	localTransform.idt().translate(trans[0], trans[1], trans[2]).scl(size);
	localTransform.mul(coordinateSystem);
    }

    @Override
    public void render(Object... params) {
	if (params[0] instanceof ModelBatch) {
	    // Render model
	    render((ModelBatch) params[0], (Float) params[1]);
	    // Render label?
	} else if (params[0] instanceof SpriteBatch) {
	    render((SpriteBatch) params[0], (ShaderProgram) params[1], (BitmapFont) params[2], (ICamera) params[3]);
	}
    }

    /**
     * Model rendering.
     */
    @Override
    public void render(ModelBatch modelBatch, float alpha) {
	mc.setTransparency(alpha * cc[3] * opacity);
	modelBatch.render(mc.instance, mc.env);
    }

    /**
     * Label rendering.
     */
    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font, ICamera camera) {
	Vector3d pos = auxVector3d.get();
	labelPosition(pos);
	shader.setUniformf("a_viewAngle", 90f);
	shader.setUniformf("a_thOverFactor", 1f);
	renderLabel(batch, shader, font, camera, label(), pos, labelScale(), labelSize(), labelColour());
    }

    @Override
    public boolean hasAtmosphere() {
	return false;
    }

    public void setTransformName(String transformName) {
	this.transformName = transformName;
    }

    public void setModel(String model) {
	this.model = model;
    }

    @Override
    public boolean renderLabel() {
	return true;
    }

    /**
     * Sets the absolute size of this entity
     * @param size
     */
    public void setSize(Double size) {
	this.size = (float) (size * Constants.KM_TO_U);
    }

    @Override
    public float[] labelColour() {
	return labelColour;
    }

    @Override
    public float labelSize() {
	return distToCamera * 3e-3f;
    }

    @Override
    public float labelScale() {
	return 3f;
    }

    @Override
    public void labelPosition(Vector3d out) {
	transform.getTranslation(out);
    }

    @Override
    public String label() {
	return name;
    }

    @Override
    public void labelDepthBuffer() {
	Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
	Gdx.gl.glDepthMask(false);
    }

    public void setModel(ModelComponent mc) {
	this.mc = mc;
    }

    public void setLabelcolor(double[] labelcolor) {
	this.labelColour = GlobalResources.toFloatArray(labelcolor);

    }

}
