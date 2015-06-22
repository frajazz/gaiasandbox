package gaia.cu9.ari.gaiaorbit.util.gaia;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.XmlReader;
import gaia.cu9.ari.gaiaorbit.util.BinarySearchTree;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.gaia.time.Days;
import gaia.cu9.ari.gaiaorbit.util.gaia.time.Duration;
import gaia.cu9.ari.gaiaorbit.util.gaia.time.Hours;
import gaia.cu9.ari.gaiaorbit.util.units.Quantity;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Parses the XML files with the attitudes and their activaton times into a binary search tree.
 * @author Toni Sagrista
 * @date 01/06/15.
 */
public class AttitudeXmlParser {

    private static Date endOfMission;
    private static DateFormat format;

    static {
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        endOfMission = getDate("2019-06-20 06:13:26");
    }

    public static BinarySearchTree parseFolder(FileHandle folder) {
        final FileHandle[] list = folder.list(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.canRead() && pathname.getName().matches("OPS_RSLS_[\\d+][\\w|\\W]*.xml");
            }
        });

        BinarySearchTree bst = new BinarySearchTree();

        // GENERATE LIST OF DURATIONS
        SortedMap<Date, FileHandle> datesMap = new TreeMap<Date, FileHandle>();
        for (FileHandle fh : list) {
            try {
                Date date = parseActivationTime(fh);
                datesMap.put(date, fh);
            } catch (IOException e) {
                Logger.error(e, I18n.bundle.format("error.file.parse", fh.name()));
            }
        }
        Map<FileHandle, Duration> durationMap = new HashMap<FileHandle, Duration>();
        Set<Date> dates = datesMap.keySet();
        FileHandle lastFH = null;
        Date lastDate = null;
        for (Date date : dates) {
            if(lastDate != null && lastFH != null){
                long elapsed = date.getTime() - lastDate.getTime();
                Duration d = new Days(elapsed * Constants.MS_TO_H);
                durationMap.put(lastFH, d);
            }
            lastDate = date;
            lastFH = datesMap.get(date);
        }
        // Last element
        long elapsed = endOfMission.getTime() - lastDate.getTime();
        Duration d = new Hours(elapsed * Constants.MS_TO_H);
        durationMap.put(lastFH, d);


        // PARSE ATTITUDES
        for (FileHandle fh : list) {
            Logger.info(I18n.bundle.format("notif.attitude.loadingfile", fh.name()));
            try {
                AttitudeIntervalBean att = parseFile(fh, durationMap.get(fh));
                bst.insert(att);
            } catch (IOException e) {
                Logger.error(e, I18n.bundle.format("error.file.parse", fh.name()));
            } catch (Exception e) {
                Logger.error(e, I18n.bundle.format("notif.error", e.getMessage()));
            }
        }

        Logger.info(I18n.bundle.format("notif.attitude.initialized", list.length));
        return bst;
    }

    private static Date parseActivationTime(FileHandle fh) throws IOException {
        BaseAttitudeDataServer result = null;

        XmlReader reader = new XmlReader();
        XmlReader.Element element = reader.parse(fh);
        XmlReader.Element model = element.getChildByName("model");

        /** MODEL ELEMENT **/
        String name = model.get("name");
        String className = model.get("classname");
        String activTime = model.get("starttime");
        return getDate(activTime);
    }

    private static AttitudeIntervalBean parseFile(FileHandle fh, Duration duration) throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        BaseAttitudeDataServer result = null;

        XmlReader reader = new XmlReader();
        XmlReader.Element element = reader.parse(fh);
        XmlReader.Element model = element.getChildByName("model");

        /** MODEL ELEMENT **/
        String name = model.get("name");
        String className = model.get("classname");
        String activTime = model.get("starttime");
        Date activationTime = getDate(activTime);
        double startTimeNsSince2010 = (AstroUtils.getJulianDate(activationTime) - AstroUtils.JD_J2010) * AstroUtils.DAY_TO_NS;

        Class clazz = Class.forName(className);

        /** SCAN LAW ELEMENT **/
        XmlReader.Element scanlaw = model.getChildByName("scanlaw");
        String epochRef = scanlaw.getAttribute("epochref");
        Date refEpochDate = getDate(epochRef);
        double refEpoch = AstroUtils.getJulianDate(refEpochDate) * AstroUtils.DAY_TO_NS;
        double refEpochJ2010 = refEpoch - AstroUtils.JD_J2010 * AstroUtils.DAY_TO_NS;

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
        Quantity.Angle scanRate = new Quantity.Angle(getDouble(scanrate, "value"), scanrate.get("unit").split("_")[0]);

        // Solar aspect angle
        XmlReader.Element saa = scanlaw.getChildByName("solaraspectangle");
        Quantity.Angle solarAspectAngle = new Quantity.Angle(getDouble(saa, "value"), saa.get("unit"));

        if (className.contains("MslAttitudeDataServer")) {
            // We need to pass the startTime, duration and MSL to the constructor

            ModifiedScanningLaw msl = new ModifiedScanningLaw((long) startTimeNsSince2010);
            msl.setRefEpoch((long) refEpochJ2010);
            msl.setRefOmega(spinPhase.get(Quantity.Angle.AngleUnit.RAD));
            msl.setRefNu(precessionPhase.get(Quantity.Angle.AngleUnit.RAD));
            msl.setPrecRate(precessionRate);
            msl.setScanRate(scanRate.get(Quantity.Angle.AngleUnit.ARCSEC));
            msl.setRefXi(solarAspectAngle.get(Quantity.Angle.AngleUnit.RAD));
            msl.initialize();

            MslAttitudeDataServer mslDatServ = (MslAttitudeDataServer) clazz.getConstructor(new Class[] { long.class, Duration.class, ModifiedScanningLaw.class }).newInstance(new Object[] { (long) startTimeNsSince2010, duration, msl });
            mslDatServ.initialize();
            result = mslDatServ;

            //            Nsl37 nsl = new Nsl37();
            //            nsl.setRefTime((long) refEpochJ2010);
            //            nsl.setNuRef(precessionPhase.get(Quantity.Angle.AngleUnit.RAD));
            //            nsl.setOmegaRef(spinPhase.get(Quantity.Angle.AngleUnit.RAD));
            //            nsl.setXiRef(solarAspectAngle.get(Quantity.Angle.AngleUnit.RAD));
            //            nsl.setTargetScanRate(scanRate.get(Quantity.Angle.AngleUnit.ARCSEC));
            //            nsl.setTargetPrecessionRate(precessionRate);
            //
            //            result = nsl;

        } else if (className.contains("Epsl")) {

            Epsl.Mode mode = name.equals("EPSL_F") ? Epsl.Mode.FOLLOWING : Epsl.Mode.PRECEDING;
            Epsl epsl = (Epsl) clazz.getConstructor(Epsl.Mode.class).newInstance(mode);

            epsl.setRefTime((long) refEpochJ2010);
            epsl.setNuRef(precessionPhase.get(Quantity.Angle.AngleUnit.RAD));
            epsl.setOmegaRef(spinPhase.get(Quantity.Angle.AngleUnit.RAD));
            epsl.setXiRef(solarAspectAngle.get(Quantity.Angle.AngleUnit.RAD));
            epsl.setTargetScanRate(scanRate.get(Quantity.Angle.AngleUnit.ARCSEC));
            epsl.setTargetPrecessionRate(precessionRate);

            result = epsl;
        }

        return new AttitudeIntervalBean(name, activationTime, result);
    }

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
        return Double.parseDouble(e.get(property));
    }
}
