package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import com.badlogic.gdx.graphics.PerspectiveCamera;

public abstract class AbstractCamera implements ICamera {

    public Vector3d pos, posinv;
    /** Angle from the center to the corner of the screen in scene coordinates, in radians **/
    protected float angleEdgeRad;
    /** Aspect ratio **/
    protected float ar;

    /** The parent **/
    protected CameraManager parent;

    /** The main camera **/
    public PerspectiveCamera camera;

    public float fovFactor;

    public AbstractCamera(CameraManager parent) {
	this.parent = parent;
	pos = new Vector3d();
	posinv = new Vector3d();
    }

    @Override
    public void updateAngleEdge(int width, int height) {
	ar = (float) width / (float) height;
	float h = camera.fieldOfView;
	float w = h * ar;
	angleEdgeRad = (float) (Math.toRadians(Math.sqrt(h * h + w * w))) / 2f;
    }

    @Override
    public float getFovFactor() {
	return fovFactor;
    }

    @Override
    public Vector3d getPos() {
	return pos;
    }

    @Override
    public Vector3d getInversePos() {
	return posinv;
    }

    @Override
    public float getAngleEdge() {
	return angleEdgeRad;
    }

    @Override
    public CameraManager getManager() {
	return parent;
    }

    @Override
    public void render() {

    }

    @Override
    public ICamera getCurrent() {
	return this;
    }

}
