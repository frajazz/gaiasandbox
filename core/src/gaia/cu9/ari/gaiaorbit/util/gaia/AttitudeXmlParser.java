package gaia.cu9.ari.gaiaorbit.util.gaia;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlReader;
import gaia.cu9.ari.gaiaorbit.util.BinarySearchTree;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.gaia.time.Days;
import gaia.cu9.ari.gaiaorbit.util.gaia.time.Duration;
import gaia.cu9.ari.gaiaorbit.util.units.Quantity;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Parses the XML files with the attitudes and their activaton times into a binary search tree.
 * Created by tsagrista on 01/06/15.
 */
public class AttitudeXmlParser {

    public static BinarySearchTree parseFolder(FileHandle folder) {
        final FileHandle[] list = folder.list(new FileFilter() {

            @Override public boolean accept(File pathname) {
                return pathname.isFile() && pathname.canRead() && pathname.getName().matches("OPS_RSLS_[\\d+][\\w|\\W]*.xml");
            }
        });

        BinarySearchTree bst = new BinarySearchTree();

        for (FileHandle fh : list) {
            try {
                AttitudeIntervalBean att = parseFile(fh);
                bst.insert(att);
            } catch (IOException e) {
                Logger.error(e, I18n.bundle.format("error.file.parse", fh.name()));
            } catch (Exception e) {
                Logger.error(e, I18n.bundle.format("notif.error", e.getMessage()));
            }
        }
        return bst;
    }

    private static AttitudeIntervalBean parseFile(FileHandle fh) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        BaseAttitudeDataServer result = null;

        XmlReader reader = new XmlReader();
        XmlReader.Element element = reader.parse(fh);
        XmlReader.Element model = element.getChildByName("model");

        /** MODEL ELEMENT **/
        String name = model.get("name");
        String className = model.get("classname");
        String activTime = model.get("starttime");
        Date activationTime = getDate(activTime);

        double startTimeJD = AstroUtils.getJulianDate(activationTime) - AstroUtils.JD_J2010;
        Class clazz = Class.forName(className);

        /** SCAN LAW ELEMENT **/
        XmlReader.Element scanlaw = model.getChildByName("scanlaw");
        String epochRef = scanlaw.getAttribute("epochref");
        Date refEpochDate = getDate(epochRef);
        double refEpoch = AstroUtils.getJulianDate(refEpochDate) - AstroUtils.JD_J2010;

        // Spin phase
        XmlReader.Element spinphase = scanlaw.getChildByName("spinphase");
        Quantity.Angle spinPhase = new Quantity.Angle(getDouble(spinphase, "value"), spinphase.get("unit"));

        // Precession pahse
        XmlReader.Element precessphase = scanlaw.getChildByName("precessphase");
        Quantity.Angle precessionPhase = new Quantity.Angle(getDouble(precessphase, "value"), precessphase.get("unit"));

        // Precession rate - always in rev/yr
        XmlReader.Element precessrate = scanlaw.getChildByName("precessrate");
        Double precessionRate = getDouble(precessrate, "value");

        // Scan rate
        XmlReader.Element scanrate = scanlaw.getChildByName("scanrate");
        Quantity.Angle scanRate = new Quantity.Angle(getDouble(scanlaw, "value"), scanlaw.get("unit").split("_")[0]);

        // Solar aspect angle
        XmlReader.Element saa = scanlaw.getChildByName("solaraspectangle");
        Quantity.Angle solarAspectAngle = new Quantity.Angle(getDouble(saa, "value"), saa.get("unit"));

        if (className.contains("MslAttitudeDataServer")) {
            // We need to pass the startTime, duration and MSL to the constructor

            Duration duration = new Days(80);
            ModifiedScanningLaw msl = new ModifiedScanningLaw((long) startTimeJD);
            msl.setRefEpoch((long) refEpoch);
            msl.setRefOmega(spinPhase.get(Quantity.Angle.AngleUnit.RAD));
            msl.setRefNu(precessionPhase.get(Quantity.Angle.AngleUnit.RAD));
            msl.setPrecRate(precessionRate);
            msl.setScanRate(scanRate.get(Quantity.Angle.AngleUnit.ARCSEC));
            msl.setRefXi(solarAspectAngle.get(Quantity.Angle.AngleUnit.RAD));

            MslAttitudeDataServer mslDatServ = (MslAttitudeDataServer) clazz.getConstructor(new Class[] { long.class, Duration.class, ModifiedScanningLaw.class }).newInstance(new Object[] { startTimeJD, duration, msl });
            result = mslDatServ;

        } else if (className.contains("Epsl")) {

            Epsl.Mode mode = name.equals("EPSL_F") ? Epsl.Mode.FOLLOWING : Epsl.Mode.PRECEDING;
            Epsl epsl = (Epsl) clazz.getConstructor(Epsl.Mode.class).newInstance(mode);

            epsl.setRefTime((long) refEpoch);
            epsl.setNuRef(precessionPhase.get(Quantity.Angle.AngleUnit.RAD));
            epsl.setOmegaRef(spinPhase.get(Quantity.Angle.AngleUnit.RAD));
            epsl.setXiRef(solarAspectAngle.get(Quantity.Angle.AngleUnit.RAD));
            epsl.setTargetScanRate(scanRate.get(Quantity.Angle.AngleUnit.ARCSEC));
            epsl.setTargetPrecessionRate(precessionRate);
            //            epsl.setTargetScanPeriod();

            result = epsl;
        }

        return new AttitudeIntervalBean(name, new Date(), result);
    }

    static DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static Date getDate(String date) {
        try {
            Date d = format.parse(date);
            return d;
        } catch (ParseException e) {
            Logger.error(e);
        }
        return null;
    }

    private static Double getDouble(XmlReader.Element e, String property) {
        return Double.parseDouble(e.get("property"));
    }
}
