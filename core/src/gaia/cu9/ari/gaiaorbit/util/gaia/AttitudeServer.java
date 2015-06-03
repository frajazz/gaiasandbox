package gaia.cu9.ari.gaiaorbit.util.gaia;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.BinarySearchTree;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.Quaterniond;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.badlogic.gdx.files.FileHandle;

/**
 * Provides caching of the last Nsl37 attitude requested.
 * This allows for calculating the attitude only once in each
 * time step and using it in several points in the processing.
 * @author Toni Sagrista
 *
 */
public class AttitudeServer {

    // List of attitudes in a BST sorted by activation date
    private static BinarySearchTree attitudes;
    // Dummy attitude for launch sequence
    static Attitude dummyAttitude;

    // The previous attitude
    static AttitudeIntervalBean prevAttitude = null, current;

    // The first activation date
    static Date initialDate;

    public static void initialize(FileHandle xmlFolder) {
        attitudes = AttitudeXmlParser.parseFolder(xmlFolder);
        initialDate = ((AttitudeIntervalBean) attitudes.findMin()).activationTime;
        current = new AttitudeIntervalBean(null, null, null);
        // Dummy attitude
        dummyAttitude = new ConcreteAttitude(0, new Quaterniond(), false);

    }

    private static Date getDate(String date) {
        String fmt = "yyyy-MM-dd HH:mm:ss";
        DateFormat format = new SimpleDateFormat(fmt);
        try {
            Date d = format.parse(date);
            return d;
        } catch (ParseException e) {
            Logger.error(e);
        }
        return null;
    }

    private static Date getDate(int day, int month, int year, int hour, int min, int sec) {
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
    public synchronized static Attitude getAttitude(Date date) {
        // Find AttitudeType in timeSlots
        if (date.before(initialDate)) {
            return dummyAttitude;
        } else {
            current.activationTime = date;
            AttitudeIntervalBean att = (AttitudeIntervalBean) attitudes.findIntervalStart(current);

            if (prevAttitude != null && !att.equals(prevAttitude)) {
                // Change!
                EventManager.instance.post(Events.POST_NOTIFICATION, "Gaia attitude changed to " + att.toString() + " at time " + att.activationTime);
            }

            prevAttitude = att;

            // Get actual attitude
            return att.get(date);
        }

    }

}
