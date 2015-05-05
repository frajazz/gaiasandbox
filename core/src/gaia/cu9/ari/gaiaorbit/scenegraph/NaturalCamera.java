package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.util.CamRecorder;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Models the movement of the camera
 * @author Toni Sagrista
 *
 */
public class NaturalCamera extends AbstractCamera implements IObserver {

    /** Camera far value **/
    public static final double CAM_FAR = 1e15 * Constants.KM_TO_U;
    /** Camera near values **/
    public static final double CAM_NEAR = 1e4 * Constants.KM_TO_U;

    /** Acceleration, velocity and position of the entity **/
    public Vector3d accel, vel;
    /** The force acting on the entity and the friction **/
    private Vector3d force, friction;

    public Vector3d direction, up, focusDirection;
    /** Indicates whether the camera is facing the focus or not **/
    public boolean facingFocus;

    /** Auxiliary vectors **/
    private Vector3d aux1, aux2, aux3, state;
    /** Acceleration, velocity and position for pitch, yaw and roll **/
    private Vector3d pitch, yaw, roll;
    /** Acceleration, velocity and position for the horizontal and vertical rotation around the focus **/
    private Vector3d hor, vert;
    /** Time since last forward control issued, in seconds **/
    private double lastFwdTime = 0d;

    /** Info about whether the previous state is saved **/
    protected boolean stateSaved = false;

    private CameraMode lastMode;

    /**
     * The focus entity
     */
    public CelestialBody focus, focusBak, closest;

    /** 
     * Distance from the center (Sun)
     */
    public double distance;

    /**
     * The direction point to seek
     */
    private Vector3d lastvel;
    private Vector3d focusPos;

    private Vector3d desired;

    /** Velocity module, in case it comes from a gamepad **/
    private double velocityGamepad = 0;
    private double gamepadMultiplier = 1;

    Viewport viewport;
    boolean diverted = false;

    boolean accelerometer = false;

    public static float[] upSensor, lookAtSensor;

    public NaturalCamera(AssetManager assetManager, CameraManager parent) {
        super(parent);
        vel = new Vector3d();
        accel = new Vector3d();
        force = new Vector3d();
        initialize(assetManager);
    }

    public void initialize(AssetManager assetManager) {
        camera = new PerspectiveCamera(GlobalConf.scene.CAMERA_FOV, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = (float) CAM_NEAR;
        camera.far = (float) CAM_FAR;

        fovFactor = camera.fieldOfView / 40f;

        up = new Vector3d(-0.024214629529207728, 0.7563044458865531, -0.6537715479041569);
        direction = new Vector3d();
        focusDirection = new Vector3d();
        desired = new Vector3d();
        pitch = new Vector3d(0.0f, 0.0f, -3.0291599E-6f);
        yaw = new Vector3d(0.0f, 0.0f, -7.9807205E-6f);
        roll = new Vector3d(0.0f, 0.0f, -1.4423944E-4f);
        hor = new Vector3d();
        vert = new Vector3d();

        friction = new Vector3d();
        lastvel = new Vector3d();
        focusPos = new Vector3d();

        aux1 = new Vector3d();
        aux2 = new Vector3d();
        aux3 = new Vector3d();
        state = new Vector3d();

        //viewport = new ExtendViewport(200, 200, camera);

        accelerometer = Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer);

        // Focus is changed from GUI
        EventManager.instance.subscribe(this, Events.FOCUS_CHANGE_CMD, Events.FOV_CHANGED_CMD, Events.FOCUS_LOCK_CMD, Events.CAMERA_POS_CMD, Events.CAMERA_DIR_CMD, Events.CAMERA_UP_CMD, Events.CAMERA_FWD, Events.CAMERA_ROTATE, Events.CAMERA_PAN, Events.CAMERA_ROLL, Events.CAMERA_TURN, Events.CAMERA_STOP, Events.CAMERA_CENTER);
    }

    public void update(float dt, ITimeFrameProvider time) {
        // Set up direction and lookAtSensor if accelerometer is enabled
        if (accelerometer) {
            synchronized (lookAtSensor) {
                direction.set(lookAtSensor).nor();
                up.set(upSensor).nor();
            }
            updatePerspectiveCamera();
        } else {
            camUpdate(dt, time);
        }

    }

    private void camUpdate(float dt, ITimeFrameProvider time) {
        // The whole update thread must lock the value of direction and up
        distance = pos.len();
        CameraMode m = (parent.current == this ? parent.mode : lastMode);
        double translateUnits = Math.max(10d * Constants.M_TO_U, getTranslateUnits());
        switch (m) {
        case Focus:
            if (focus.withinMagLimit()) {
                focusBak = focus;
                focus = (CelestialBody) focus.getComputedAncestor();
                focus.getPosition(focusPos);
                if (GlobalConf.scene.FOCUS_LOCK && time.getDt() != 0) {
                    // Get copy of focus and update it to know where it will be in the next step
                    AbstractPositionEntity fc = (AbstractPositionEntity) focus;
                    AbstractPositionEntity fccopy = fc.getLineCopy();
                    fccopy.getRoot().transform.position.set(posinv);
                    fccopy.getRoot().update(time, null, this);
                    // Work out difference vector
                    aux1.set(fccopy.transform.getTranslation());
                    aux1.sub(focusPos);
                    // Add displacement
                    pos.add(aux1);

                    // Return to poolvec
                    SceneGraphNode ape = fccopy;
                    do {
                        ape.returnToPool();
                        ape = ape.parent;
                    } while (ape != null);
                }

                // Update direction to follow focus and activate custom input listener
                updatePosition(dt, translateUnits);
                updateRotation(dt, focusPos);

                if (!diverted) {
                    directionToTarget(dt, focusPos, GlobalConf.scene.TURNING_SPEED / 1e3f);
                } else {
                    updateRotationFree(dt, GlobalConf.scene.TURNING_SPEED);
                }
                updateRoll(dt, GlobalConf.scene.TURNING_SPEED);

                // Update focus direction
                focus.transform.getTranslation(focusDirection);
                focus = focusBak;

                EventManager.instance.post(Events.FOCUS_INFO_UPDATED, focus.distToCamera - focus.getRadius(), ((AbstractPositionEntity) focus).viewAngle);
            } else {
                EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Free_Camera);
            }
            break;
        case Free_Camera:
            updatePosition(dt, translateUnits);

            // Update direction with pitch, yaw, roll
            updateRotationFree(dt, GlobalConf.scene.TURNING_SPEED);
            updateRoll(dt, GlobalConf.scene.TURNING_SPEED);
            updateLateral(dt, translateUnits);
            break;
        default:
            break;
        }

        // Camera recording
        CamRecorder.instance.update(dt, pos, direction, up);

        // Update actual camera
        lastFwdTime += dt;
        lastMode = m;

        updatePerspectiveCamera();
    }

    private void updatePerspectiveCamera() {

        if (closest != null) {
            camera.near = (float) Math.min(CAM_NEAR, (closest.distToCamera - closest.getRadius()) / 2);
        }
        camera.position.set(0f, 0f, 0f);
        camera.direction.set(direction.valuesf());
        camera.up.set(up.valuesf());
        camera.update();

        posinv.set(pos).scl(-1);

    }

    /**
     * Adds a forward movement by the given amount.
     * @param amount Positive for forward force, negative for backward force.
     */
    public void addForwardForce(double amount) {
        double tu = getTranslateUnits();
        if (amount <= 0) {
            // Avoid getting stuck in surface
            tu = Math.max(10d * Constants.M_TO_U, tu);
        }
        if (parent.mode == CameraMode.Focus) {
            desired.set(focusDirection);
        } else {
            desired.set(direction);
        }

        desired.nor().scl(amount * tu * 10);
        force.add(desired);
        // We reset the time counter
        lastFwdTime = 0;
    }

    /**
     * Sets the gamepad velocity as it comes from the joystick sensor. 
     * @param amount The amount in [-1, 1].
     */
    public void setVelocity(double amount) {
        velocityGamepad = amount;
    }

    /**
     * Adds a pan movement to the camera.
     * @param deltaX Amount of horizontal movement.
     * @param deltaY Amount of vertical movement.
     */
    public void addPanMovement(double deltaX, double deltaY) {
        double tu = getTranslateUnits();
        desired.set(direction).crs(up).nor().scl(-deltaX * tu);
        desired.add(aux1.set(up).nor().scl(-deltaY * tu));
        force.add(desired);
    }

    /**
     * Adds a rotation force to the camera. DeltaX corresponds to yaw (right/left) and deltaY 
     * corresponds to pitch (up/down).
     * @param deltaX The yaw amount.
     * @param deltaY The pitch amount.
     * @param focusLookKeyPressed The key to look around when on focus mode is pressed.
     */
    public void addRotateMovement(double deltaX, double deltaY, boolean focusLookKeyPressed) {
        // Just update yaw with X and pitch with Y
        if (parent.mode.equals(CameraMode.Free_Camera)) {
            addYaw(deltaX);
            addPitch(deltaY);
        } else if (parent.mode.equals(CameraMode.Focus)) {
            if (focusLookKeyPressed) {
                diverted = true;
                addYaw(deltaX);
                addPitch(deltaY);
            } else {
                addHorizontalRotation(deltaX);
                addVerticalRotation(deltaY);
            }
        }
    }

    public void setGamepadMultiplier(double amount) {
        gamepadMultiplier = amount;
    }

    public void addAmountX(Vector3d vec, double amount) {
        vec.x += amount;
    }

    /** Adds the given amount to the camera yaw acceleration **/
    public void addYaw(double amount) {
        addAmountX(yaw, amount);
    }

    public void setYaw(double amount) {
        yaw.x = 0;
        yaw.y = amount;
    }

    /** Adds the given amount to the camera pitch acceleration **/
    public void addPitch(double amount) {
        addAmountX(pitch, amount);
    }

    public void setPitch(double amount) {
        pitch.x = 0;
        pitch.y = amount;
    }

    /** Adds the given amount to the camera roll acceleration **/
    public void addRoll(double amount) {
        addAmountX(roll, amount);
    }

    public void setRoll(double amount) {
        roll.x = 0;
        roll.y = amount;
    }

    /** Adds the given amount to camera horizontal rotation around the focus acceleration **/
    public void addHorizontalRotation(double amount) {
        addAmountX(hor, amount);
    }

    public void setHorizontalRotation(double amount) {
        hor.x = 0;
        hor.y = amount;
    }

    /** Adds the given amount to camera vertical rotation around the focus acceleration **/
    public void addVerticalRotation(double amount) {
        addAmountX(vert, amount);
    }

    public void setVerticalRotation(double amount) {
        vert.x = 0;
        vert.y = amount;
    }

    /**
     * Stops the camera movement.
     * @return True if the camera had any movement at all and it has been stopped. False if camera was already still.
     */
    public boolean stopMovement() {
        boolean stopped = (vel.len2() != 0 || yaw.y != 0 || pitch.y != 0 || roll.y != 0 || vert.y != 0 || hor.y != 0);
        force.scl(0f);
        vel.scl(0f);
        yaw.y = 0;
        pitch.y = 0;
        roll.y = 0;
        hor.y = 0;
        vert.y = 0;
        return stopped;
    }

    /**
     * Stops the camera movement.
     * @return True if the camera had any movement at all and it has been stopped. False if camera was already still.
     */
    public boolean stopTotalMovement() {
        boolean stopped = (vel.len2() != 0 || yaw.y != 0 || pitch.y != 0 || roll.y != 0 || vert.y != 0 || hor.y != 0);
        force.scl(0f);
        vel.scl(0f);
        yaw.scl(0f);
        pitch.scl(0f);
        roll.scl(0f);
        hor.scl(0f);
        vert.scl(0f);
        return stopped;
    }

    /**
     * Stops the camera movement.
     * @return True if the camera had any movement at all and it has been stopped. False if camera was already still.
     */
    public boolean stopForwardMovement() {
        boolean stopped = (vel.len2() != 0);
        force.scl(0f);
        vel.scl(0f);
        return stopped;
    }

    /**
     * Updates the position of this entity using the current force
     * @param dt
     * @param multiplier
     */
    protected void updatePosition(double dt, double multiplier) {
        // Calculate velocity if coming from gamepad
        if (velocityGamepad != 0) {
            vel.set(direction).nor().scl(velocityGamepad * gamepadMultiplier * multiplier);
        }

        double forceLen = force.len();
        double velocity = vel.len();

        // Half a second after we have stopped zooming, real friction kicks in
        friction.set(force).nor().scl(-forceLen * dt * (lastFwdTime > 0.5 ? (lastFwdTime - 0.5) * 1000 : 1));
        force.add(friction);

        if (lastFwdTime > 1.2 && velocityGamepad == 0) {
            stopForwardMovement();
        }

        applyForce(force);

        if (!(force.isZero() && velocity == 0 && accel.isZero())) {
            vel.add(accel.scl(dt));

            // Clamp to top speed
            if (GlobalConf.scene.CAMERA_SPEED_LIMIT > 0 && vel.len() > GlobalConf.scene.CAMERA_SPEED_LIMIT) {
                vel.clamp(0, GlobalConf.scene.CAMERA_SPEED_LIMIT);
            }

            // Velocity changed direction
            if (lastvel.dot(vel) < 0) {
                vel.scl(0);
                force.scl(0);
            }

            velocity = vel.len();

            if (parent.mode.equals(CameraMode.Focus)) {
                //Use direction vector as velocity so that if we turn the velocity also turns
                double sign = Math.signum(vel.dot(focusDirection));
                focus.getPosition(vel).nor().scl(sign * velocity);
            }

            vel.clamp(0, multiplier);
            // Aux1 is the step to take
            aux1.set(vel).scl(dt);
            // Aux2 contains the new position
            aux2.set(pos).add(aux1);
            pos.add(aux1);

            accel.scl(0);

            lastvel.set(vel);
        }
    }

    /**
     * Updates the rotation for the free camera.
     * @param dt
     */
    private void updateRotationFree(float dt, float rotateSpeed) {
        // Add position to compensate for coordinates centered on camera
        if (updatePosition(pitch, dt)) {
            // Pitch
            aux1.set(direction).crs(up).nor();
            rotate(aux1, pitch.z * rotateSpeed);
        }
        if (updatePosition(yaw, dt)) {
            // Yaw
            rotate(up, -yaw.z * rotateSpeed);
        }

        // Set acceleration to 0
        pitch.x = 0f;
        yaw.x = 0f;
    }

    private void updateRoll(float dt, float rotateSpeed) {
        if (updatePosition(roll, dt)) {
            // Roll
            rotate(direction, -roll.z * rotateSpeed);
        }
        roll.x = 0f;
    }

    /**
     * Updates the direction vector using the pitch, yaw and roll forces.
     * @param dt
     */
    private void updateRotation(float dt, final Vector3d rotationCenter) {
        // Add position to compensate for coordinates centered on camera
        rotationCenter.add(pos);
        if (updatePosition(vert, dt)) {
            // Pitch
            aux1.set(direction).crs(up).nor();
            rotateAround(rotationCenter, aux1, vert.z * GlobalConf.scene.ROTATION_SPEED);
        }
        if (updatePosition(hor, dt)) {
            // Yaw
            rotateAround(rotationCenter, up, -hor.z * GlobalConf.scene.ROTATION_SPEED);
        }

        // Set acceleration to 0
        vert.x = 0f;
        hor.x = 0f;
    }

    private void updateLateral(float dt, double translateUnits) {
        // Pan with hor
        aux1.set(direction).crs(up).nor();
        aux1.scl(hor.y * dt * translateUnits);
        translate(aux1);

    }

    /**
     * Updates the given accel/vel/pos of the angle using dt.
     * @param angle
     * @param dt
     * @return
     */
    private boolean updatePosition(Vector3d angle, float dt) {
        if (angle.x != 0 || angle.y != 0) {
            // Calculate velocity from acceleration
            angle.y += angle.x * dt;
            // Cap velocity
            angle.y = Math.signum(angle.y) * Math.abs(angle.y);
            // Update position
            angle.z = (angle.y * dt) % 360f;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Updates the direction vector with a gentle turn
     * @param target The target point of the direction vector
     */
    private void directionToTarget(double dt, final Vector3d target, double turnVelocity) {
        desired.set(target).sub(pos).nor();
        double dist = desired.dst(direction);
        if (dist > Constants.KM_TO_U) {
            // Add desired to direction with given turn velocity (v*dt)
            desired.scl(turnVelocity * dt);
            direction.add(desired).nor();

            // Update up so that it is always perpendicular
            aux1.set(direction).crs(up);
            up.set(aux1).crs(direction).nor();
            facingFocus = false;
        } else {
            facingFocus = true;
        }
    }

    /**
     * Updates the camera mode
     */
    @Override
    public void updateMode(CameraMode mode, boolean postEvent) {
        if (mode.equals(CameraMode.Focus)) {
            diverted = false;
        }
    }

    public void setFocus(CelestialBody focus) {
        if (focus != null) {
            this.focus = focus;
            // Create event to notify focus change
            EventManager.instance.post(Events.FOCUS_CHANGED, focus);
        }
    }

    /**
     * This depends on the distance from the focus.
     * @return
     */
    public double getTranslateUnits() {
        double dist;
        if (parent.mode == CameraMode.Focus) {
            AbstractPositionEntity ancestor = focus.getComputedAncestor();
            dist = ancestor.distToCamera - ancestor.getRadius();
        } else {
            dist = distance;
        }
        return dist * GlobalConf.scene.CAMERA_SPEED;
    }

    /**
     * Depends on the distance to the focus
     * @return
     */
    public double getRotationUnits() {
        double dist;
        if (parent.mode == CameraMode.Focus) {
            AbstractPositionEntity ancestor = focus.getComputedAncestor();
            dist = ancestor.distToCamera - ancestor.getRadius();
        } else {
            dist = distance;
        }
        return Math.max(2000, Math.min(dist * Constants.U_TO_KM, GlobalConf.scene.ROTATION_SPEED));
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case FOCUS_CHANGE_CMD:
            // Check the type of the parameter: CelestialBody or String
            CelestialBody focus = null;
            if (data[0] instanceof String) {
                SceneGraphNode sgn = GaiaSandbox.instance.sg.getNode((String) data[0]);
                if (sgn instanceof CelestialBody) {
                    focus = (CelestialBody) sgn;
                    diverted = false;
                }
            } else if (data[0] instanceof CelestialBody) {
                focus = (CelestialBody) data[0];
                diverted = false;
            }
            if (focus != null) {
                setFocus(focus);
            }
            break;
        case FOV_CHANGED_CMD:
            float fov = MathUtilsd.clamp((float) data[0], Constants.MIN_FOV, Constants.MAX_FOV);
            camera.fieldOfView = fov;
            if (parent.current == this) {
                EventManager.instance.post(Events.FOV_CHANGE_NOTIFICATION, fov);
            }
            fovFactor = camera.fieldOfView / 40f;
            break;
        case CAMERA_POS_CMD:
            pos.set((double[]) data[0]);
            posinv.set(pos).scl(-1d);
            break;
        case CAMERA_DIR_CMD:
            direction.set((double[]) data[0]);
            break;
        case CAMERA_UP_CMD:
            up.set((double[]) data[0]);
            break;
        case CAMERA_FWD:
            addForwardForce((double) data[0]);
            break;
        case CAMERA_ROTATE:
            addRotateMovement((double) data[0], (double) data[1], false);
            break;
        case CAMERA_TURN:
            addRotateMovement((double) data[0], (double) data[1], true);
            break;
        case CAMERA_PAN:

            break;
        case CAMERA_ROLL:
            addRoll((double) data[0]);
            break;
        case CAMERA_STOP:
            stopTotalMovement();
            break;
        case CAMERA_CENTER:
            diverted = false;
            break;
        default:
            break;
        }

    }

    /** 
     * Rotates the direction and up vector of this camera by the given angle around the given axis, with the axis attached to given
     * point. The direction and up vector will not be orthogonalized.
     * 
     * @param point the point to attach the axis to
     * @param axis the axis to rotate around
     * @param angle the angle */
    public void rotateAround(final Vector3d point, Vector3d axis, double angle) {
        aux3.set(point);
        aux3.sub(pos);
        translate(aux3);
        rotate(axis, angle);
        aux3.rotate(axis, angle);
        translate(-aux3.x, -aux3.y, -aux3.z);
    }

    public void rotate(Vector3d axis, double angle) {
        direction.rotate(axis, angle);
        up.rotate(axis, angle);
    }

    /** Moves the camera by the given amount on each axis.
     * @param x the displacement on the x-axis
     * @param y the displacement on the y-axis
     * @param z the displacement on the z-axis */
    public void translate(double x, double y, double z) {
        pos.add(x, y, z);
    }

    /** Moves the camera by the given vector.
     * @param vec the displacement vector */
    public void translate(Vector3d vec) {
        pos.add(vec);
    }

    @Override
    public void saveState() {
        stateSaved = parent.mode == CameraMode.Focus;
        if (state != null && stateSaved && focus != null) {
            // Relative position to focus
            state.set(pos).sub(focus.pos);
            // Stop motion
            stopMovement();
        }
    }

    @Override
    public void restoreState() {
        if (state != null && stateSaved && focus != null) {
            pos.set(focus.pos).add(state);
            direction.set(focus.pos).sub(pos).nor();
            stateSaved = false;
        }
    }

    /**
     * Applies the given force to this entity's acceleration
     * @param force
     */
    protected void applyForce(Vector3d force) {
        if (force != null)
            accel.add(force);
    }

    @Override
    public PerspectiveCamera[] getFrontCameras() {
        return new PerspectiveCamera[] { camera };
    }

    @Override
    public PerspectiveCamera getCamera() {
        return camera;
    }

    @Override
    public Viewport getViewport() {
        return viewport;
    }

    @Override
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    @Override
    public Vector3d getDirection() {
        return direction;
    }

    @Override
    public Vector3d getUp() {
        return up;
    }

    @Override
    public Vector3d[] getDirections() {
        return new Vector3d[] { direction };
    }

    @Override
    public int getNCameras() {
        return 1;
    }

    @Override
    public CameraMode getMode() {
        return parent.mode;
    }

    @Override
    public float getMotionMagnitude() {
        return (float) vel.len();
    }

    @Override
    public double getVelocity() {
        return parent.getVelocity();
    }

    @Override
    public boolean superVelocity() {
        return parent.superVelocity();
    }

    @Override
    public boolean isFocus(CelestialBody cb) {
        return focus != null && cb == focus;
    }

    @Override
    public void checkClosest(CelestialBody cb) {
        if (closest == null) {
            closest = cb;
        } else {
            if (closest.distToCamera - closest.getRadius() > cb.distToCamera - cb.getRadius()) {
                closest = cb;
            }
        }
    }

    @Override
    public CelestialBody getFocus() {
        return getMode().equals(CameraMode.Focus) ? focus : null;
    }

}
