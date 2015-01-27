package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.render.ILabelRenderable;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.g3d.ModelBuilder2;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class MilkyWay extends Blob implements IModelRenderable, ILabelRenderable {
    static float[] labelColour = new float[] { 1f, .4f, .7f, 1f };
    String texture;
    Texture tex;
    double[] position;
    ModelComponent mc;
    String model, transformName;
    Matrix4 coordinateSystem;

    public MilkyWay() {
	super();
	localTransform = new Matrix4();
	coordinateSystem = new Matrix4();
	lowAngle = (float) Math.toRadians(60);
	highAngle = (float) Math.toRadians(75.51);
    }

    public void initialize() {
	mc.initialize();
	pos.set(position);
	mc.env.set(new ColorAttribute(ColorAttribute.AmbientLight, cc[0], cc[1], cc[2], 1));
    }

    @Override
    public void doneLoading(AssetManager manager) {
	// Initialize transform
	if (transformName != null) {
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
	Model mwModel = null;
	if (manager.isLoaded(model)) {
	    // Get model from file
	    mwModel = manager.get(this.model, Model.class);
	} else {
	    // Prepare model
	    Material mat = new Material();
	    tex = manager.get(texture);
	    mat.set(new TextureAttribute(TextureAttribute.Diffuse, tex));
	    ModelBuilder2 mb = ModelCache.cache.mb;
	    // Initialize milky way model
	    mb.begin();
	    mb.part("mw-up", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).
		    rect(0.5f, 0, -0.5f,
			    0.5f, 0, 0.5f,
			    -0.5f, 0, 0.5f,
			    -0.5f, 0, -0.5f,
			    0, 1, 0);
	    mb.part("mw-down", GL20.GL_TRIANGLES, Usage.Position | Usage.Normal | Usage.TextureCoordinates, mat).
		    rect(-0.5f, -0.001f, 0.5f,
			    0.5f, -0.001f, 0.5f,
			    0.5f, -0.001f, -0.5f,
			    -0.5f, -0.001f, -0.5f,
			    0, 1, 0);
	    mwModel = mb.end();
	}
	mc.instance = new ModelInstance(mwModel, this.localTransform);

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
	localTransform.idt().translate(trans[0], trans[1], trans[2]).scl(size).mul(coordinateSystem);
    }

    public void setPosition(String position) {
	String[] p = position.split("\\s+");
	this.position = new double[3];
	this.position[0] = Double.parseDouble(p[0]) * Constants.KM_TO_U;
	this.position[1] = Double.parseDouble(p[1]) * Constants.KM_TO_U;
	this.position[2] = Double.parseDouble(p[2]) * Constants.KM_TO_U;
    }

    @Override
    public void render(Object... params) {
	if (params[0] instanceof ModelBatch) {
	    render((ModelBatch) params[0], (Float) params[1]);
	} else if (params[0] instanceof SpriteBatch) {
	    render((SpriteBatch) params[0], (ShaderProgram) params[1], (BitmapFont) params[2], (ICamera) params[3], (Float) params[4]);
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
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font, ICamera camera, float alpha) {
	Vector3d pos = auxVector3d.get();
	labelPosition(pos);
	renderLabel(batch, shader, font, camera, alpha * labelAlpha(), label(), pos, labelScale(), labelSize(), labelColour());
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

    @Override
    public float[] labelColour() {
	return labelColour;
    }

    @Override
    public float labelAlpha() {
	return opacity;
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

    public void setTexture(String texture) {
	this.texture = texture;
    }

}
