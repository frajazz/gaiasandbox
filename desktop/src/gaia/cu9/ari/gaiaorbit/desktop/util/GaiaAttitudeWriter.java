package gaia.cu9.ari.gaiaorbit.desktop.util;

import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.gaia.Attitude;
import gaia.cu9.ari.gaiaorbit.util.gaia.GaiaAttitudeServer;
import gaia.cu9.ari.gaiaorbit.util.math.Quaterniond;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Writes Gaia attitude to a file. Assumes attitude has been initialized.
 * @author tsagrista
 *
 */
public class GaiaAttitudeWriter {
    public static void attitudeZAxisToFile() {
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
            Writer writerEcl = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fecl), "utf-8"));
            Writer writerEq = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(feq), "utf-8"));

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
