package gaia.cu9.ari.gaiaorbit.desktop.util;

import gaia.cu9.ari.gaiaorbit.data.stars.HYGBinaryLoader;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Pair;
import gaia.cu9.ari.gaiaorbit.util.gaia.GaiaAttitudeServer;
import gaia.cu9.ari.gaiaorbit.util.gaia.Satellite;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Quaterniond;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;

/**
 * Loads a catalog and selects the stars that are observed during a certain period of time.
 * @author tsagrista
 *
 */
public class GaiaCatalogFilter {

    List<Particle> catalog;
    long MAX_OVERLAP_TIME;
    double BAM_2;
    double angleEdgeRad;
    Matrix4d trf;

    NumberFormat nf;
    LogWriter lw;

    public GaiaCatalogFilter() {
        super();
    }

    public void initialize() throws Exception {
        // Init Gdx files
        Gdx.files = new LwjglFiles();

        // Init log writer
        lw = new LogWriter();

        // Init global conf
        DesktopConfInit confInit = new DesktopConfInit();
        confInit.initConf();

        // Precompute some math functions
        MathUtilsd.initialize();

        // Initialize i18n
        I18n.initialize();

        // Load attitude
        GaiaAttitudeServer.instance = new GaiaAttitudeServer("data/attitudexml/", "OPS_RSLS_0022916_rsls_nsl_gareq1_afterFirstSpinPhaseOptimization.2.xml");

        // Load catalog
        HYGBinaryLoader loader = new HYGBinaryLoader();
        loader.initialize(Gdx.files.internal("data/hygxyz.bin"));
        catalog = loader.loadCatalog();

        // Format
        nf = new DecimalFormat("##00");

        // Initialize constants and so
        float h = (float) Satellite.FOV_AC_ACTIVE;
        float w = (float) Satellite.FOV_AL;
        angleEdgeRad = (Math.sqrt(h * h + w * w) * Math.PI / 180D);
        MAX_OVERLAP_TIME = (long) (angleEdgeRad / (Satellite.SCANRATE * (Math.PI / (3600D * 180D)))) * 1000;
        BAM_2 = Satellite.BASICANGLE_DEGREE / 2D;

        trf = new Matrix4d();
    }

    /**
     * Produces the filtered catalogs for each day between the selected dates.
     * @param iniY The initial year.
     * @param iniM The initial month. Starts at 1 for January.
     * @param iniD The initial day of the month. Starts at 1.
     * @param endY The end year.
     * @param endM The end month. Starts at 1 for January.
     * @param endD The end day of the month. Starts at 1.
     */
    public void filterCatalog(int iniY, int iniM, int iniD, int endY, int endM, int endD) {
        // Some constants
        final long msDay = 24 * 60 * 60 * 1000;
        // 5 min overlap
        final long overlap = 5 * 60 * 1000;

        GregorianCalendar iniCal = new GregorianCalendar(iniY, iniM - 1, iniD);
        GregorianCalendar endCal = new GregorianCalendar(endY, endM - 1, endD);
        Date ini = iniCal.getTime();
        Date end = endCal.getTime();

        Date current = new Date(ini.getTime());

        Set<Particle> out = new HashSet<Particle>(10000);

        while (current.getTime() < end.getTime()) {
            out.clear();
            long dayStart = current.getTime();
            // Process day
            for (long t = dayStart - overlap; t < dayStart + msDay + overlap * 2; t += MAX_OVERLAP_TIME) {
                Pair<Vector3d, Vector3d> dirs = getDirections(new Date(t));

                for (Particle p : catalog) {
                    double poslen = p.pos.len();
                    boolean observed = MathUtilsd.acos(p.pos.dot(dirs.getFirst()) / poslen) < angleEdgeRad || MathUtilsd.acos(p.pos.dot(dirs.getSecond()) / poslen) < angleEdgeRad;
                    // Sun should always be there because of the scene graph
                    observed = observed || p.name.equalsIgnoreCase("sol") || p.name.equalsIgnoreCase("sun");
                    if (observed) {
                        out.add(p);
                    }
                }
            }

            // Write out to file
            Calendar cal = Calendar.getInstance();
            cal.setTime(current);
            String bin = System.getProperty("java.io.tmpdir") + File.separator + "hygxyz-" + nf.format(cal.get(Calendar.YEAR)) + nf.format(cal.get(Calendar.MONTH) + 1) + nf.format(cal.get(Calendar.DAY_OF_MONTH)) + ".bin";

            try {
                writeToBinary(out, bin);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Advance a day
            current.setTime(current.getTime() + msDay);

        }

    }

    public void writeToBinary(Set<Particle> catalog, String bin) throws IOException {
        File binFile = new File(bin);
        binFile.mkdirs();
        if (binFile.exists()) {
            binFile.delete();
            binFile.createNewFile();
        }
        // Create an output stream to the file.
        FileOutputStream file_output = new FileOutputStream(binFile);
        // Wrap the FileOutputStream with a DataOutputStream
        DataOutputStream data_out = new DataOutputStream(file_output);

        // Size of stars
        data_out.writeInt(catalog.size());
        for (Particle s : catalog) {
            // name_length, name, appmag, absmag, colorbv, ra, dec, dist
            data_out.writeInt(s.name.length());
            data_out.writeChars(s.name);
            data_out.writeFloat(s.appmag);
            data_out.writeFloat(s.absmag);
            data_out.writeFloat(s.colorbv);
            data_out.writeFloat(s.posSph.x);
            data_out.writeFloat(s.posSph.y);
            data_out.writeFloat((float) s.pos.len());
            data_out.writeInt(s.id);
        }
        file_output.close();
        System.out.println(catalog.size() + " particles written to binary file " + bin);
    }

    public Pair<Vector3d, Vector3d> getDirections(Date d) {
        trf.idt();
        Quaterniond quat = GaiaAttitudeServer.instance.getAttitude(d).getQuaternion();
        trf.rotate(quat).rotate(0, 0, 1, 180);
        Vector3d dir1 = new Vector3d().set(0, 0, 1).rotate(BAM_2, 0, 1, 0).mul(trf).nor();
        Vector3d dir2 = new Vector3d().set(0, 0, 1).rotate(-BAM_2, 0, 1, 0).mul(trf).nor();
        return new Pair<Vector3d, Vector3d>(dir1, dir2);
    }

    public static void main(String[] args) throws Exception {
        GaiaCatalogFilter gcf = new GaiaCatalogFilter();
        gcf.initialize();
        gcf.filterCatalog(2015, 8, 20, 2015, 8, 25);
    }
}
