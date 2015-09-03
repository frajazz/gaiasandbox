package gaia.cu9.ari.gaiaorbit.util.gaia;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.BinarySearchTree;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.IDateFormat;
import gaia.cu9.ari.gaiaorbit.util.math.Quaterniond;

import java.util.Calendar;
import java.util.Date;

/**
 * Provides caching of the last Nsl37 attitude requested.
 * This allows for calculating the attitude only once in each
 * time step and using it in several points in the processing.
 * @author Toni Sagrista
 *
 */
public class GaiaAttitudeServer {

    public static GaiaAttitudeServer instance;

    // List of attitudes in a BST sorted by activation date
    private BinarySearchTree attitudes;
    // Dummy attitude for launch sequence
    Attitude dummyAttitude;
    Nsl37 nsl;

    // The previous attitude
    AttitudeIntervalBean prevAttitude = null, current;

    // The first activation date
    Date initialDate;

    public GaiaAttitudeServer(String folder, String... files) {
        if (GlobalConf.data.REAL_GAIA_ATTITUDE) {
            attitudes = AttitudeXmlParser.parseFolder(folder, GlobalConf.runtime.STRIPPED_FOV_MODE, files);
            initialDate = ((AttitudeIntervalBean) attitudes.findMin()).activationTime;
            current = new AttitudeIntervalBean("current", null, null, "dummy");
            // Dummy attitude
            dummyAttitude = new ConcreteAttitude(0, new Quaterniond(), false);
        } else {
            // Use NSL as approximation
            nsl = new Nsl37();
        }
    }

    private Date getDate(String date) {
        String fmt = "yyyy-MM-dd HH:mm:ss";
        IDateFormat format = DateFormatFactory.getFormatter(fmt);
        Date d = format.parse(date);
        return d;
    }

    private Date getDate(int day, int month, int year, int hour, int min, int sec) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(year, month, day, hour, min, sec);
        return cal.getTime();
    }

    /**
     * Returns the NSL37 attitude for the given date.
     * @param date
     * @return
     */
    public synchronized Attitude getAttitude(Date date) {
        Attitude result;
        if (GlobalConf.data.REAL_GAIA_ATTITUDE) {
            // Find AttitudeType in timeSlots
            if (date.before(initialDate)) {
                result = dummyAttitude;
            } else {
                current.activationTime = date;
                AttitudeIntervalBean att = (AttitudeIntervalBean) attitudes.findIntervalStart(current);

                if (prevAttitude != null && !att.equals(prevAttitude)) {
                    // Change!
                    EventManager.instance.post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.attitude.changed", att.toString(), att.activationTime));
                }

                prevAttitude = att;

                // Get actual attitude
                result = att.get(date);
            }
        } else {
            result = nsl.getAttitude(date);
        }

        return result;

    }

    public synchronized String getCurrentAttitudeName() {
        if (prevAttitude != null) {
            return prevAttitude.file;
        }
        return null;
    }

}
