package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.utils.viewport.Viewport;

public interface ICamera {

    /**
     * Returns the perspective camera.
     * @return The perspective camera.
     */
    public PerspectiveCamera getCamera();

    public PerspectiveCamera[] getFrontCameras();

    public ICamera getCurrent();

    public float getFovFactor();

    public float getMotionMagnitude();

    public Viewport getViewport();

    public Vector3d getPos();

    public Vector3d getInversePos();

    public Vector3d getDirection();

    public Vector3d getUp();

    public Vector3d[] getDirections();

    public int getNCameras();

    /**
     * Updates the camera.
     * @param dt The time since the las frame in seconds.
     * @param time The frame time provider (simulation time).
     */
    public void update(float dt, ITimeFrameProvider time);

    public void updateMode(CameraMode mode, boolean postEvent);

    public CameraMode getMode();

    public void updateAngleEdge(int width, int height);

    /**
     * Gets the angle of the edge of the screen, diagonally. It assumes the vertical angle
     * is the field of view and corrects the horizontal using the aspect ratio. It depends on the viewport
     * size and the field of view itself.
     * @return The angle in radians.
     */
    public float getAngleEdge();

    public CameraManager getManager();

    public void render();

    public void saveState();

    public void restoreState();

    /**
     * Gets the current velocity of the camera in km/h.
     * @return The velocity in km/h.
     */
    public double getVelocity();

    /**
     * Returns true if the velocity is greater than certain value.
     * @return True if the velocity is bigger than a certain high value.
     */
    public boolean superVelocity();

    /**
     * Returns the foucs if any.
     * @return The foucs object if it is in focus mode. Null otherwise.
     */
    public CelestialBody getFocus();

    /**
     * Checks if this body is the current focus.
     * @param cb
     * @return
     */
    public boolean isFocus(CelestialBody cb);

    /**
     * Called after updating the body's distance to the cam, it updates
     * the closest body in the camera to figure out the camera near. 
     * @param cb The body to check
     */
    public void checkClosest(CelestialBody cb);
}
