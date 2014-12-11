package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.data.orbit.IOrbitDataProvider;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitDataLoader;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.util.Date;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class Orbit extends LineObject {
    public class OrbitalParameters {
	/** Orbital period in days **/
	public double period;
	/** Base epoch **/
	public long epoch;
	/** Semi major axis of the ellipse, a.**/
	public double semiMajorAxis;
	/** Eccentricity of the ellipse. **/
	public double e;
	/** Inclination, angle between the reference plane (ecliptic) and the orbital plane. **/
	public double i;
	/** Longitude of the ascending node in degrees. **/
	public double ascendingNode;
	/** Argument of perihelion in degrees. **/
	public double argOfPericenter;
	/** Mean anomaly at epoch, in degrees. **/
	public double meanAnomaly;
    }

    /** Threshold angle **/
    protected static final float ANGLE_LIMIT = (float) Math.toRadians(.3);
    /**
     * Special overlap factor
     */
    protected static final float SHADER_MODEL_OVERLAP_FACTOR = 50f;

    protected String source;
    public OrbitData orbitData;
    protected Vector3d prev, curr;
    protected float alpha;
    public Matrix4d localTransformD;
    protected String provider;
    protected Class<? extends IOrbitDataProvider> providerClass;
    public OrbitalParameters params;

    public Orbit() {
	super();
	localTransformD = new Matrix4d();
	prev = new Vector3d();
	curr = new Vector3d();
    }

    @Override
    public void initialize() {
	try {
	    providerClass = (Class<? extends IOrbitDataProvider>) Class.forName(provider);
	    // Orbit data
	    //AssetBean.addAsset(source, OrbitData.class, new OrbitDataLoader.OrbitDataLoaderParameter(providerClass));
	    IOrbitDataProvider provider;
	    try {
		provider = providerClass.newInstance();
		provider.load(source, new OrbitDataLoader.OrbitDataLoaderParameter(providerClass, params));
		orbitData = provider.getData();
	    } catch (Exception e) {
		Gdx.app.error(getClass().getSimpleName(), e.getMessage());
	    }
	} catch (ClassNotFoundException e) {
	    Gdx.app.error(getClass().getSimpleName(), e.getMessage());
	}

    }

    @Override
    public void doneLoading(AssetManager manager) {
	//orbitData = manager.get(source, OrbitData.class);
	alpha = cc[3];
	int last = orbitData.getNumPoints() - 1;
	Vector3d v = new Vector3d(orbitData.x.get(last), orbitData.y.get(last), orbitData.z.get(last));
	this.size = (float) v.len() * 5;
    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
	super.updateLocal(time, camera);
	updateLocalTransform(time.getTime());
    }

    protected void updateLocalTransform(Date date) {
	if (params == null) {
	    // Orbit is sampled, only get position
	    localTransformD.set(transform.getMatrix());
	} else {
	    // Orbit is defined by its parameters and not sampled
	    // Set to parent orientation
	    localTransformD.set(transform.getMatrix()).mul(parent.orientation);

	    localTransformD.rotate(0, 1, 0, params.argOfPericenter);
	    localTransformD.rotate(0, 0, 1, params.i);
	    localTransformD.rotate(0, 1, 0, params.ascendingNode);

	}

    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time) {
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
	float angleLimit = ANGLE_LIMIT * camera.getFovFactor();
	if (viewAngle > angleLimit) {
	    if (viewAngle < angleLimit * SHADER_MODEL_OVERLAP_FACTOR) {
		float alpha = MathUtilsd.lint(viewAngle, angleLimit, angleLimit * SHADER_MODEL_OVERLAP_FACTOR, 0, cc[3]);
		this.alpha = alpha;
	    } else {
		this.alpha = cc[3];
	    }

	    addToRender(this, RenderGroup.LINE);
	}

    }

    public void setSource(String source) {
	this.source = source;
    }

    @Override
    public void render(ShapeRenderer renderer, float alpha) {
	alpha *= this.alpha;
	renderer.setColor(cc[0], cc[1], cc[2], alpha);

	// Make origin Gaia
	Vector3d parentPos = null;
	if (parent instanceof Gaia) {
	    parentPos = ((Gaia) parent).unrotatedPos;
	}

	// This is so that the shape renderer does not mess up the z-buffer
	for (int i = 1; i < orbitData.getNumPoints(); i++) {
	    orbitData.loadPoint(prev, i - 1);
	    orbitData.loadPoint(curr, i);

	    if (parentPos != null) {
		prev.sub(parentPos);
		curr.sub(parentPos);
	    }

	    prev.mul(localTransformD);
	    curr.mul(localTransformD);

	    renderer.line((float) prev.x, (float) prev.y, (float) prev.z, (float) curr.x, (float) curr.y, (float) curr.z);
	}
    }

    /**
     * Sets the absolute size of this entity
     * @param size
     */
    public void setSize(Float size) {
	this.size = size * (float) Constants.KM_TO_U;
    }

    public String getProvider() {
	return provider;
    }

    public void setProvider(String provider) {
	this.provider = provider;
    }

    private void initParams() {
	if (params == null) {
	    params = new OrbitalParameters();
	}
    }

    /**
     * Sets the orbital period in days.
     * @param orbitalperiod In days
     */
    public void setOrbitPeriod(Float orbitalperiod) {
	initParams();
	params.period = orbitalperiod;
    }

    public void setOrbitEpoch(Float epoch) {
	initParams();
	params.epoch = epoch.longValue();
    }

    public void setOrbitSemimajoraxis(Float sma) {
	initParams();
	params.semiMajorAxis = sma;
    }

    public void setOrbitEccentricity(Float e) {
	initParams();
	params.e = e;
    }

    public void setOrbitInclination(Float i) {
	initParams();
	params.i = i;
    }

    public void setOrbitAscendingnode(Float ascendingNode) {
	initParams();
	params.ascendingNode = ascendingNode;
    }

    public void setOrbitArgofpericenter(Float aop) {
	initParams();
	params.argOfPericenter = aop;
    }

    public void setOrbitMeananomaly(Float meanAnomaly) {
	initParams();
	params.meanAnomaly = meanAnomaly;
    }

}
