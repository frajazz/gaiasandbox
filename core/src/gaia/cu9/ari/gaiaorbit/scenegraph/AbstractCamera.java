package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

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

    private static final double VIEW_ANGLE = 0.43633231;

    @Override
    public boolean isVisible(ITimeFrameProvider time, CelestialBody cb, boolean computeGaiaScan) {
        boolean visible = cb.viewAngle > VIEW_ANGLE || GlobalResources.isInView(cb.transform.position, cb.distToCamera, angleEdgeRad, getDirection());
        /** If time is running, check Gaia **/
        if (visible && computeGaiaScan && time.getDt() != 0) {
            boolean visibleByGaia = computeVisibleFovs(cb.pos, parent.fovCamera, cb.distToCamera);

            cb.updateTransitNumber(visibleByGaia, time, parent.fovCamera);
        }
        return visible && !(GlobalConf.scene.ONLY_OBSERVED_STARS && cb.transits == 0);
    }

    /**
     * Returns true if a body with the given position is observed in any of the given directions using the given cone angle
     * @param pos The position of the body.
     * @param coneAngle The whole observation angle
     * @param dirs The directions
     * @return True if the body is observed. False otherwise.
     */
    protected boolean computeVisibleFovs(Vector3d pos, FovCamera fcamera, double poslen) {
        boolean visible = false;
        float coneAngle = fcamera.angleEdgeRad;
        Vector3d[] dirs = null;
        if (GlobalConf.scene.COMPUTE_GAIA_SCAN && !fcamera.interpolatedDirections.isEmpty()) {
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

}
