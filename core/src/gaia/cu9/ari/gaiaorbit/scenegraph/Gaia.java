package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.gaia.Attitude;
import gaia.cu9.ari.gaiaorbit.util.gaia.Nsl37AttitudeServer;
import gaia.cu9.ari.gaiaorbit.util.math.Quaterniond;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Quaternion;

public class Gaia extends ModelBody {

    @Override
    public float THRESHOLD_ANGLE_NONE() {
	return super.THRESHOLD_ANGLE_POINT() / 5e7f;
    }

    @Override
    public float THRESHOLD_ANGLE_POINT() {
	return super.THRESHOLD_ANGLE_POINT() / 5e6f;
    }

    @Override
    public float THRESHOLD_ANGLE_SHADER() {
	return super.THRESHOLD_ANGLE_POINT() / 5f;
    }

    public Vector3d unrotatedPos;
    int currentIndex = 0;
    boolean display = true;
    Attitude attitude;
    Quaterniond quat;

    public Gaia() {
	super();
	unrotatedPos = new Vector3d();
    }

    @Override
    public void initialize() {
	super.initialize();
    }

    @Override
    public void doneLoading(AssetManager manager) {
	super.doneLoading(manager);
	EventManager.getInstance().post(Events.GAIA_LOADED, this);

	// INITIALIZE COORDINATES
	if (coordinates != null) {
	    coordinates.initialize(sg.getNode(orbitName));
	}
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time) {
	forceUpdatePosition(time, false);
    }

    private void forceUpdatePosition(ITimeFrameProvider time, boolean force) {
	if (time.getDt() != 0 || force) {
	    coordinates.getEquatorialCartesianCoordinates(time.getTime(), pos);
	    unrotatedPos.set(pos);
	    // Undo rotation
	    unrotatedPos.mul(Coordinates.eclipticToEquatorial()).rotate(-AstroUtils.getSunLongitude(time.getTime()) - 180, 0, 1, 0);
	    attitude = Nsl37AttitudeServer.getAttitude(time.getTime());
	}
    }

    @Override
    protected void updateLocalTransform() {
	// Local attitude
	localTransform.set(transform.getMatrix().valuesf()).scl(size);
	if (attitude != null) {
	    quat = attitude.getQuaternion();
	    // QuatRotation * Flip (upside down)
	    localTransform.rotate(new Quaternion((float) quat.x, (float) quat.y, (float) quat.z, (float) quat.w));
	    // Flip satellite along field of view axis (Z)
	    localTransform.rotate(0, 0, 1, 180);
	}
    }

    @Override
    public void render(ModelBatch modelBatch, float alpha) {
	if (display)
	    super.render(modelBatch, alpha);
    }

    @Override
    public void labelPosition(Vector3d out) {
	transform.getTranslation(out);
    }

    @Override
    protected float labelMax() {
	return 2.5e-7f;
    }

    @Override
    protected float labelFactor() {
	return 2e4f;
    }

    @Override
    public void labelDepthBuffer() {
	Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
	Gdx.gl.glDepthMask(false);
    }
}
