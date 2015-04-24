package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.TwoWayHashmap;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.utils.viewport.Viewport;

public class CameraManager implements ICamera, IObserver {

    /**
     * Convenience enum to describe the camera mode
     * @author Toni Sagrista
     *
     */
    public enum CameraMode {
	/** Free navigation **/
	Free_Camera,
	/** Focus **/
	Focus,
	/** FOV1 **/
	Gaia_FOV1,
	/** FOV2 **/
	Gaia_FOV2,
	/** Both fields of view **/
	Gaia_FOV1and2;

	static TwoWayHashmap<String, CameraMode> equivalences;

	static {
	    String fc = "Free camera";
	    String foc = "Focus object";
	    String f1 = "Gaia FoV 1";
	    String f2 = "Gaia FoV 2";
	    String f12 = "Gaia FoV1 and FoV2";

	    equivalences = new TwoWayHashmap<String, CameraMode>();
	    equivalences.add(fc, Free_Camera);
	    equivalences.add(foc, Focus);
	    equivalences.add(f1, Gaia_FOV1);
	    equivalences.add(f2, Gaia_FOV2);
	    equivalences.add(f12, Gaia_FOV1and2);

	}

	public static CameraMode getMode(int idx) {
	    if (idx >= 0 && idx < CameraMode.values().length) {
		return CameraMode.values()[idx];
	    } else {
		return null;
	    }
	}

	@Override
	public String toString() {
	    return equivalences.getBackward(this);
	}

	public static CameraMode fromString(String str) {
	    return equivalences.getForward(str);
	}
    }

    public CameraMode mode;

    public ICamera current;

    public NaturalCamera naturalCamera;
    public FovCamera fovCamera;

    /** Last position, for working out velocity **/
    private Vector3d lastPos;

    /** Are we moving at high speeds? **/
    private boolean supervelocity;

    /** Current velocity in km/h **/
    private double velocity;

    public CameraManager(AssetManager manager, CameraMode mode) {
	// Initialize
	// Initialize Cameras
	naturalCamera = new NaturalCamera(manager, this);
	fovCamera = new FovCamera(manager, this);
	this.mode = mode;
	lastPos = new Vector3d();
	supervelocity = true;

	updateCurrentCamera();

	EventManager.instance.subscribe(this, Events.CAMERA_MODE_CMD, Events.FOV_CHANGE_NOTIFICATION);
    }

    public void updateCurrentCamera() {

	// Update
	switch (mode) {
	case Free_Camera:
	case Focus:
	    current = naturalCamera;
	    break;
	case Gaia_FOV1:
	case Gaia_FOV2:
	case Gaia_FOV1and2:
	    current = fovCamera;
	    break;
	}

    }

    public boolean isNatural() {
	return current == naturalCamera;
    }

    @Override
    public PerspectiveCamera getCamera() {
	return current.getCamera();
    }

    @Override
    public float getFovFactor() {
	return current.getFovFactor();
    }

    @Override
    public Viewport getViewport() {
	return current.getViewport();
    }

    @Override
    public void setViewport(Viewport viewport) {
	current.setViewport(viewport);
    }

    @Override
    public Vector3d getPos() {
	return current.getPos();
    }

    @Override
    public Vector3d getInversePos() {
	return current.getInversePos();
    }

    @Override
    public Vector3d getDirection() {
	return current.getDirection();
    }

    @Override
    public Vector3d getUp() {
	return current.getUp();
    }

    /**
     * Update method.
     * @param dt Delta time in seconds.
     * @time time The time frame provider.
     * @Override
     */
    public void update(float dt, ITimeFrameProvider time) {
	current.update(dt, time);
	if (current != fovCamera && GlobalConf.scene.COMPUTE_GAIA_SCAN) {
	    fovCamera.updateDirections(time);
	}

	// Velocity = dx/dt
	velocity = (lastPos.sub(current.getPos()).len() * Constants.U_TO_KM) / (dt * Constants.S_TO_H);

	// Post event with camera motion parameters
	EventManager.instance.post(Events.CAMERA_MOTION_UPDATED, current.getPos(), velocity);

	// Update last pos
	lastPos.set(current.getPos());
    }

    /**
     * Sets the new camera mode and updates the frustum
     * @param mode
     */
    public void updateMode(CameraMode mode, boolean postEvent) {
	boolean modeChange = mode != this.mode;
	// Save state of current if mode is different
	if (modeChange)
	    saveState();

	// Save state of old camera
	this.mode = mode;
	updateCurrentCamera();
	naturalCamera.updateMode(mode, postEvent);
	fovCamera.updateMode(mode, postEvent);

	// Restore state of new camera
	if (modeChange)
	    restoreState();

	if (postEvent)
	    EventManager.instance.post(Events.FOV_CHANGE_NOTIFICATION, this.getCamera().fieldOfView);
    }

    @Override
    public void notify(Events event, Object... data) {
	switch (event) {
	case CAMERA_MODE_CMD:
	    CameraMode cm = (CameraMode) data[0];
	    updateMode(cm, true);
	    break;
	case FOV_CHANGE_NOTIFICATION:
	    updateAngleEdge(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	default:
	    break;
	}

    }

    @Override
    public Vector3d[] getDirections() {
	return current.getDirections();
    }

    @Override
    public int getNCameras() {
	return current.getNCameras();
    }

    @Override
    public PerspectiveCamera[] getFrontCameras() {
	return current.getFrontCameras();
    }

    @Override
    public CameraMode getMode() {
	return mode;
    }

    @Override
    public void updateAngleEdge(int width, int height) {
	naturalCamera.updateAngleEdge(width, height);
	fovCamera.updateAngleEdge(width, height);
    }

    @Override
    public float getAngleEdge() {
	return current.getAngleEdge();
    }

    @Override
    public CameraManager getManager() {
	return this;
    }

    @Override
    public void render() {
	current.render();
    }

    @Override
    public float getMotionMagnitude() {
	return current.getMotionMagnitude();
    }

    @Override
    public ICamera getCurrent() {
	return current;
    }

    @Override
    public void saveState() {
	if (current != null)
	    current.saveState();
    }

    @Override
    public void restoreState() {
	if (current != null)
	    current.restoreState();
    }

    @Override
    public double getVelocity() {
	return velocity;
    }

    @Override
    public boolean superVelocity() {
	return supervelocity;
    }

    @Override
    public boolean isFocus(CelestialBody cb) {
	return current.isFocus(cb);
    }

    @Override
    public void checkClosest(CelestialBody cb) {
	current.checkClosest(cb);
    }

    @Override
    public CelestialBody getFocus() {
	return current.getFocus();
    }

    @Override
    public boolean isVisible(ITimeFrameProvider time, CelestialBody cb, boolean computeGaiaScan) {
	return current.isVisible(time, cb, computeGaiaScan);
    }

}
