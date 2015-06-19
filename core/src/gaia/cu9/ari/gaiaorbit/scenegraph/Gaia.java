package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.gaia.Attitude;
import gaia.cu9.ari.gaiaorbit.util.gaia.AttitudeServer;
import gaia.cu9.ari.gaiaorbit.util.math.Quaterniond;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class Gaia extends ModelBody {

    private static final double TH_ANGLE_NONE = ModelBody.TH_ANGLE_POINT / 1e18;
    private static final double TH_ANGLE_POINT = ModelBody.TH_ANGLE_POINT / 1e17;
    private static final double TH_ANGLE_QUAD = ModelBody.TH_ANGLE_POINT / 4d;

    @Override
    public double THRESHOLD_ANGLE_NONE() {
        return TH_ANGLE_NONE;
    }

    @Override
    public double THRESHOLD_ANGLE_POINT() {
        return TH_ANGLE_POINT;
    }

    @Override
    public double THRESHOLD_ANGLE_QUAD() {
        return TH_ANGLE_QUAD;
    }

    public Vector3d unrotatedPos;
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
        EventManager.instance.post(Events.GAIA_LOADED, this);

    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        if (display)
            super.addToRenderLists(camera);
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
        forceUpdatePosition(time, false);
    }

    private void forceUpdatePosition(ITimeFrameProvider time, boolean force) {
        if (time.getDt() != 0 || force) {
            display = coordinates.getEquatorialCartesianCoordinates(time.getTime(), pos) != null;
            unrotatedPos.set(pos);
            // Undo rotation
            unrotatedPos.mul(Coordinates.eclipticToEquatorial()).rotate(-AstroUtils.getSunLongitude(time.getTime()) - 180, 0, 1, 0);
            attitude = AttitudeServer.getAttitude(time.getTime());
        }
    }

    @Override
    protected void updateLocalTransform() {
        setToLocalTransform(1, localTransform, true);
    }

    public void setToLocalTransform(float sizeFactor, Matrix4 localTransform, boolean forceUpdate) {
        if (sizeFactor != 1 || forceUpdate) {
            localTransform.set(transform.getMatrix().valuesf()).scl(size * sizeFactor);
            if (attitude != null) {
                quat = attitude.getQuaternion();
                // QuatRotation * Flip (upside down)
                localTransform.rotate(new Quaternion((float) quat.x, (float) quat.y, (float) quat.z, (float) quat.w));
                // Flip satellite along field of view axis (Z)
                localTransform.rotate(0, 0, 1, 180);
            }
        } else {
            localTransform.set(this.localTransform);
        }

    }

    @Override
    public void textPosition(Vector3d out) {
        transform.getTranslation(out);
    }


    @Override
    protected float labelFactor() {
        return 2e1f;
    }

    @Override
    protected float labelMax() {
        return super.labelMax() * 10;
    }

    @Override
    public float textScale() {
        return labelSizeConcrete() * .5e5f;
    }

    @Override
    public boolean renderText() {
        return name != null && viewAngle > TH_ANGLE_POINT;
    }

}
