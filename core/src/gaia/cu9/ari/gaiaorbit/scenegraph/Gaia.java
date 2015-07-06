package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.gaia.Attitude;
import gaia.cu9.ari.gaiaorbit.util.gaia.GaiaAttitudeServer;
import gaia.cu9.ari.gaiaorbit.util.math.Quaterniond;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;

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
            attitude = GaiaAttitudeServer.instance.getAttitude(time.getTime());
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

    private void attitudeZAxisToFile() {
        Calendar iniCal = GregorianCalendar.getInstance();
        // 2014-01-15T15:43:04
        iniCal.set(2014, 01, 15, 15, 43, 04);
        Date ini = iniCal.getTime();
        // 2019-06-20T06:20:05
        iniCal.set(2019, 06, 20, 05, 40, 00);
        Date end = iniCal.getTime();

        long tini = ini.getTime();
        long tend = end.getTime();
        Vector3d v = new Vector3d();
        Vector3d sph = new Vector3d();

        String filenameEcl = "Gaia-ecliptic-Z-" + System.currentTimeMillis() + ".csv";
        String filenameEq = "Gaia-equatorial-Z-" + System.currentTimeMillis() + ".csv";
        File fecl = new File(System.getProperty("java.io.tmpdir") + File.separator + filenameEcl);
        File feq = new File(System.getProperty("java.io.tmpdir") + File.separator + filenameEq);

        try {
            Writer writerEcl = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(fecl), "utf-8"));
            Writer writerEq = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(feq), "utf-8"));

            writerEcl.append("#EclLong[deg], EclLat[deg], t[ms-January_1_1970_00:00:00_GMT], current[date], attitudeFile\n");
            writerEq.append("#alpha[deg], delta[deg], t[ms-January_1_1970_00:00:00_GMT], current[date], attitudeFile\n");
            for (long t = tini; t <= tend; t += Constants.H_TO_MS) {
                Date current = new Date(t);

                try {
                    Attitude att = GaiaAttitudeServer.instance.getAttitude(current);
                    Quaterniond quat = att.getQuaternion();

                    v.set(0, 1, 0);

                    // Equatorial
                    v.rotateVectorByQuaternion(quat);
                    Coordinates.cartesianToSpherical(v, sph);
                    writerEq.append(Math.toDegrees(sph.x) + ", " + Math.toDegrees(sph.y) + ", " + t + ", " + current + ", " + GaiaAttitudeServer.instance.getCurrentAttitudeName() + "\n");

                    //Ecliptic
                    v.mul(Coordinates.eclipticToEquatorial());
                    Coordinates.cartesianToSpherical(v, sph);
                    writerEcl.append(Math.toDegrees(sph.x) + ", " + Math.toDegrees(sph.y) + ", " + t + ", " + current + ", " + GaiaAttitudeServer.instance.getCurrentAttitudeName() + "\n");

                } catch (Exception e) {
                    System.out.println("Error: t=" + current);
                }

            }

            writerEcl.close();
            writerEq.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

