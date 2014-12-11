package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.render.ILabelRenderable;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.render.IPointRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.RotationComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.DecalUtils;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.color.ColourUtils;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector2d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

/**
 * These are entities that have a model and is always loaded.
 * @author Toni Sagrista
 *
 */
public abstract class CelestialBody extends AbstractPositionEntity implements ILabelRenderable, IPointRenderable, IModelRenderable {
    private static float[] labelColour = new float[] { 1, 1, 1, 1 };

    protected static ThreadLocal<Quaternion> rotation = new ThreadLocal<Quaternion>() {
	@Override
	public Quaternion initialValue() {
	    return new Quaternion();
	}
    };
    protected static ThreadLocal<Matrix4> transf = new ThreadLocal<Matrix4>() {
	@Override
	public Matrix4 initialValue() {
	    return new Matrix4();
	}
    };

    /**
     * Angle limit for rendering at all. If angle is smaller than this quantity, no rendering happens.
     */
    public abstract float THRESHOLD_ANGLE_NONE();

    /**
     * Angle limit for rendering as shader. If angle is any bigger, we render as a model.
     */
    public abstract float THRESHOLD_ANGLE_SHADER();

    /**
     * Angle limit for rendering as point. If angle is any bigger, we render with shader.
     */
    public abstract float THRESHOLD_ANGLE_POINT();

    /** Absolute magnitude, m = -2.5 log10(flux), with the flux at 10 pc **/
    public float absmag;
    /** Apparent magnitude, m = -2.5 log10(flux) **/
    public float appmag;
    /** Red, green and blue colors and their revamped cousins **/
    public float[] ccPale;
    /** Colour for stars that have been observed by Gaia **/
    public float[] ccTransit;
    /** The B-V color index, calculated as the magnitude in B minus the magnitude in V **/
    public float colorbv;
    /** The one-dimensional flux **/
    public float flux;
    /** Holds information about the rotation of the body **/
    public RotationComponent rc;

    /** Number of times this body has been observed by Gaia **/
    public int transits = 0;
    /** Last observations increase in ms **/
    public long lastTransitIncrease = 0;

    public CelestialBody() {
	super();
    }

    public CelestialBody(SceneGraphNode parent) {
	super(parent);
    }

    /**
     * Overrides the update adding the magnitude limit thingy.
     * @override
     */
    @Override
    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera) {
	if (appmag <= GlobalConf.instance.LIMIT_MAG_RUNTIME) {
	    super.update(time, parentTransform, camera);
	}
    }

    @Override
    public void render(Object... params) {
	Object first = params[0];
	if (first instanceof ImmediateModeRenderer) {
	    // POINT
	    render((ImmediateModeRenderer) first, (Float) params[1], (Boolean) params[2]);
	} else if (first instanceof ShaderProgram) {
	    // QUAD - SHADER
	    render((ShaderProgram) first, (Float) params[1], (Boolean) params[2], (Mesh) params[3], (ICamera) params[4]);
	} else if (first instanceof SpriteBatch) {
	    // LABEL
	    render((SpriteBatch) first, (ShaderProgram) params[1], (BitmapFont) params[2], (ICamera) params[3], (Float) params[4]);
	} else if (first instanceof ModelBatch) {
	    // Normal model
	    render((ModelBatch) first, (Float) params[1]);
	}
    }

    /**
     * Point rendering.
     */
    @Override
    public void render(ImmediateModeRenderer renderer, float alpha, boolean colorTransit) {
	float[] col = colorTransit ? ccTransit : cc;
	renderer.color(col[0], col[1], col[2], opacity * alpha);
	Vector3 aux = auxVector3f.get();
	transform.getTranslationf(aux);
	renderer.vertex(aux.x, aux.y, aux.z);
    }

    float precomp = -1;

    /**
     * Shader render, for planets and bodies, not stars.
     */
    @Override
    public void render(ShaderProgram shader, float alpha, boolean colorTransit, Mesh mesh, ICamera camera) {
	Quaternion rotation = CelestialBody.rotation.get();
	// Set rotation matrix so that the star faces us at all times
	DecalUtils.setBillboardRotation(rotation, camera.getCamera().direction, camera.getCamera().up);
	float size = getFuzzyRenderSize(camera);

	Vector3d selectedPos = auxVector3d.get();
	transform.getTranslation(selectedPos);

	Matrix4 transf = CelestialBody.transf.get();
	transf.set(camera.getCamera().combined).translate((float) selectedPos.x, (float) selectedPos.y, (float) selectedPos.z).rotate(rotation).scale(size, size, size);
	shader.setUniformMatrix("u_projTrans", transf);
	float[] col = ccPale;
	if (colorTransit)
	    col = ccTransit;
	shader.setUniformf("u_color", col[0], col[1], col[2], alpha * opacity);
	shader.setUniformf("u_inner_rad", getInnerRad());
	shader.setUniformf("u_distance", (float) (distToCamera * Constants.U_TO_KM));
	shader.setUniformf("u_apparent_angle", viewAngleApparent);

	if (precomp < 0) {
	    precomp = (float) (getRadius() * Constants.U_TO_KM * 172.4643429);
	}
	shader.setUniformf("u_th_dist_up", precomp);

	// Sprite.render
	mesh.render(shader, GL20.GL_TRIANGLES, 0, 6);
    }

    @Override
    public boolean hasAtmosphere() {
	return false;
    }

    public float getFuzzyRenderSize(ICamera camera) {
	float size;
	float thShaderOverlap = THRESHOLD_ANGLE_SHADER() * ModelBody.SHADER_MODEL_OVERLAP_FACTOR;
	float tanThShaderOverlapDist = (float) Math.tan(thShaderOverlap) * distToCamera;
	float thPointOverlap = THRESHOLD_ANGLE_POINT() * ModelBody.SHADER_MODEL_OVERLAP_FACTOR;
	// Size stays the same until angle gets very small, in which case it starts to decrease
	if (viewAngle < thPointOverlap) {
	    // Angle is small, we interpolate the size until we get to the point
	    size = MathUtilsd.lint(viewAngle,
		    THRESHOLD_ANGLE_POINT(),
		    thPointOverlap,
		    tanThShaderOverlapDist / 5,
		    tanThShaderOverlapDist);
	} else if (viewAngle < thShaderOverlap) {
	    // Constant size
	    size = tanThShaderOverlapDist;
	} else {
	    size = this.size;
	}
	size /= camera.getFovFactor();
	return size;
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

    protected void setColor2Data() {
	//ccPale = ColourUtils.brighten(cc, 1.0f);
	float plus = 0.1f;
	ccPale = new float[] { Math.min(1, cc[0] + plus), Math.min(1, cc[1] + plus), Math.min(1, cc[2] + plus) };
	ccTransit = new float[] { cc[0], cc[1], cc[2], cc[3] };
    }

    public abstract float getInnerRad();

    public void setMag(Float mag) {
	this.absmag = mag;
	this.appmag = mag;
    }

    public void setAbsmag(Float absmag) {
	this.absmag = absmag;
    }

    public void setAppmag(Float appmag) {
	this.appmag = appmag;
    }

    public Vector2d getPositionSph() {
	return posSph;
    }

    /**
     * Adds all the children that are focusable objects to the list.
     * @param list
     */
    public void addFocusableObjects(List<CelestialBody> list) {
	list.add(this);
	super.addFocusableObjects(list);
    }

    public float getViewAngle() {
	return viewAngle;
    }

    /**
     * Sets the size of this entity in kilometers
     * @param size
     */
    public void setSize(Float size) {
	// Size gives us the radius, and we want the diameter
	this.size = (float) (size * 2 * Constants.KM_TO_U);
    }

    public boolean isStar() {
	return false;
    }

    /**
     * Sets the rotation period in hours
     */
    public void setRotationPeriod(Float rotationPeriod) {
	rc.setRotationPeriod(rotationPeriod);
    }

    public void setRotationInclination(Float i) {
	rc.setInclination(i);
    }

    public void setRotationAxialtilt(Float at) {
	rc.setAxialTilt(at);
    }

    public void setRotationMeridianangle(Float ma) {
	rc.setMeridianAngle(ma);
    }

    public void setRotationAscendingnode(Float an) {
	rc.setAscendingNode(an);
    }

    public boolean withinMagLimit() {
	return this.appmag <= GlobalConf.instance.LIMIT_MAG_RUNTIME;
    }

    @Override
    public <T extends SceneGraphNode> T getSimpleCopy() {
	CelestialBody copy = (CelestialBody) super.getSimpleCopy();
	copy.absmag = this.absmag;
	copy.appmag = this.appmag;
	copy.colorbv = this.colorbv;
	copy.flux = this.flux;
	return (T) copy;
    }

    /**
     * Returns true if a body with the given position is observed in any of the given directions using the given cone angle
     * @param pos The position of the body.
     * @param coneAngle The whole observation angle
     * @param dirs The directions
     * @return True if the body is observed. False otherwise.
     */
    protected boolean computeVisibleFovs(Vector3d pos, FovCamera fcamera) {
	boolean visible = false;
	float coneAngle = fcamera.angleEdgeRad;
	Vector3d[] dirs = null;
	double poslen = pos.len();
	if (GlobalConf.instance.COMPUTE_GAIA_SCAN && !fcamera.interpolatedDirections.isEmpty()) {
	    // We need to interpolate...
	    for (Vector3d[] interpolatedDirection : fcamera.interpolatedDirections) {
		visible = visible ||
			MathUtilsd.acos(pos.dot(interpolatedDirection[0]) / poslen) < coneAngle ||
			MathUtilsd.acos(pos.dot(interpolatedDirection[1]) / poslen) < coneAngle;
		if (visible)
		    return true;
	    }
	}
	dirs = fcamera.directions;
	visible = visible ||
		MathUtilsd.acos(pos.dot(dirs[0]) / poslen) < coneAngle ||
		MathUtilsd.acos(pos.dot(dirs[1]) / poslen) < coneAngle;
	return visible;
    }

    /**
     * Computes whether a body with the given position is visible by a camera with the given direction
     * and angle.
     * @param pos The position of the body.
     * @param coneAngle The cone angle of the camera.
     * @param dir The direction.
     * @return True if the body is visible.
     */
    protected boolean computeVisibleFov(Vector3d pos, float coneAngle, Vector3d dir) {
	return MathUtilsd.acos(pos.dot(dir) / pos.len()) < coneAngle;
    }

    /**
     * Computes the visible value, which indicates whether this body is visible or not
     * in the given time with the given camera.
     * If updateGaia is true, it also detects if this body is observed by Gaia and updates 
     * its number of observations and its observation mode colour.
     * @param time The current time frame.
     * @param camera The camera.
     * @param computeGaiaScan Detect if Gaia observes this body 
     * @return
     */
    protected boolean computeVisible(ITimeFrameProvider time, ICamera camera, boolean computeGaiaScan) {
	boolean visible = false;
	if (camera.getNCameras() > 1) {
	    // This means we are in Fov1&Fov2 mode
	    visible = computeVisibleFovs(pos, camera.getManager().fovCamera);

	    updateTransitNumber(visible && time.getDt() != 0, time, camera.getManager().fovCamera);
	} else {
	    // We are in Free, Focus, Fov1 or Fov2 mode
	    visible = computeVisibleFov(transform.position, camera.getAngleEdge(), camera.getDirection());

	    /** If time is running, check Gaia **/
	    if (computeGaiaScan && time.getDt() != 0) {
		boolean visibleByGaia = computeVisibleFovs(pos, camera.getManager().fovCamera);

		updateTransitNumber(visibleByGaia, time, camera.getManager().fovCamera);
	    }
	}
	return visible && !(GlobalConf.instance.ONLY_OBSERVED_STARS && transits == 0);
    }

    /**
     * Updates the transit number of this body if visible is true and it is a new transit.
     * It also updates the colour if needed.
     * @param visible
     * @param time
     */
    protected void updateTransitNumber(boolean visible, ITimeFrameProvider time, FovCamera fcamera) {
	if (GlobalConf.instance.COMPUTE_GAIA_SCAN && visible && timeCondition(time)) {
	    // Update observations. Add if forward time, subtract if backward time
	    transits = Math.max(0, transits + (int) Math.signum(time.getDt()));
	    lastTransitIncrease = time.getTime().getTime();
	    // Update transit colour
	    ColourUtils.long_rainbow(ColourUtils.normalize(transits, 0, 30), this.ccTransit);
	}
    }

    protected boolean timeCondition(ITimeFrameProvider time) {
	// 95 seconds minimum since last increase, this ensures we are not increasing more than once in the same transit
	if (time.getDt() < 0 && lastTransitIncrease - time.getTime().getTime() < 0) {
	    lastTransitIncrease = time.getTime().getTime();
	    return true;
	} else if (time.getDt() > 0 && time.getTime().getTime() - lastTransitIncrease < 0) {
	    lastTransitIncrease = time.getTime().getTime();
	    return true;
	} else {
	    return (time.getDt() > 0 && time.getTime().getTime() - lastTransitIncrease > 90000) ||
		    (time.getDt() < 0 && lastTransitIncrease - time.getTime().getTime() > 90000);
	}
    }

    @Override
    public boolean renderLabel() {
	return viewAngle > THRESHOLD_ANGLE_POINT();
    }

    @Override
    public float[] labelColour() {
	return labelColour;
    }

    @Override
    public float labelAlpha() {
	return Math.max(0, Math.min(.95f, (viewAngle - THRESHOLD_ANGLE_POINT()) / (THRESHOLD_ANGLE_POINT() * 2)));
    }

    @Override
    public float labelScale() {
	return (float) Math.atan(labelMax()) * labelFactor() * 5e2f;
    }

    @Override
    public float labelSize() {
	return (float) Math.min(labelSizeConcrete() / distToCamera, labelMax()) * distToCamera * labelFactor();
    }

    protected float labelSizeConcrete() {
	return this.size;
    }

    protected abstract float labelFactor();

    protected abstract float labelMax();

    @Override
    public void labelPosition(Vector3d out) {
	transform.getTranslation(out);
	double len = out.len();
	out.clamp(0, len - getRadius());
    }

    @Override
    public String label() {
	return name;
    }

    @Override
    public void labelDepthBuffer() {
	Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
	Gdx.gl.glDepthMask(true);
    }

}