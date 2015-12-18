package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.math.Matrix4;

import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public abstract class Satellite extends ModelBody {

    protected static final double TH_ANGLE_NONE = ModelBody.TH_ANGLE_POINT / 1e18;
    protected static final double TH_ANGLE_POINT = ModelBody.TH_ANGLE_POINT / 1e17;
    protected static final double TH_ANGLE_QUAD = ModelBody.TH_ANGLE_POINT / 4d;

    @Override
    public double THRESHOLD_NONE() {
        return TH_ANGLE_NONE;
    }

    @Override
    public double THRESHOLD_POINT() {
        return TH_ANGLE_POINT;
    }

    @Override
    public double THRESHOLD_QUAD() {
        return TH_ANGLE_QUAD;
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
        forceUpdatePosition(time, false);
    }

    /**
     * Default implementation, only sets the result of the coordinates call to pos
     * @param time Time to get the coordinates
     * @param force Whether to force the update
     */
    protected void forceUpdatePosition(ITimeFrameProvider time, boolean force) {
        if (time.getDt() != 0 || force) {
            coordinates.getEquatorialCartesianCoordinates(time.getTime(), pos);
        }
    }

    @Override
    protected void updateLocalTransform() {
        setToLocalTransform(1, localTransform, true);
    }

    /**
     * Sets the local transform of this satellite
     */
    public void setToLocalTransform(float sizeFactor, Matrix4 localTransform, boolean forceUpdate) {
        if (sizeFactor != 1 || forceUpdate) {
            localTransform.set(transform.getMatrix().valuesf()).scl(size * sizeFactor);
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
