package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.gaia.Attitude;
import gaia.cu9.ari.gaiaorbit.util.gaia.GaiaAttitudeServer;
import gaia.cu9.ari.gaiaorbit.util.math.Quaterniond;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class Gaia extends Satellite {

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
    protected void forceUpdatePosition(ITimeFrameProvider time, boolean force) {
        super.forceUpdatePosition(time, force);
        if (time.getDt() != 0 || force) {
            unrotatedPos.set(pos);
            // Undo rotation
            unrotatedPos.mul(Coordinates.eclipticToEquatorial()).rotate(-AstroUtils.getSunLongitude(time.getTime()) - 180, 0, 1, 0);
            attitude = GaiaAttitudeServer.instance.getAttitude(time.getTime());
        }
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

}
