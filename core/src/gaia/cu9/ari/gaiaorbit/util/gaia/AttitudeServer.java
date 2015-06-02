package gaia.cu9.ari.gaiaorbit.util.gaia;

import com.badlogic.gdx.Gdx;
import gaia.cu9.ari.gaiaorbit.data.FileLocator;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.BinarySearchTree;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.LruCache;
import gaia.cu9.ari.gaiaorbit.util.Pair;
import gaia.cu9.ari.gaiaorbit.util.gaia.time.Duration;
import gaia.cu9.ari.gaiaorbit.util.gaia.time.Mins;
import gaia.cu9.ari.gaiaorbit.util.gaia.time.Secs;
import gaia.cu9.ari.gaiaorbit.util.math.Quaterniond;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

    static {
        attitudes = AttitudeXmlParser.parseFolder(FileLocator.internal("data/attitudexml/"));
        initialDate = ((AttitudeIntervalBean)attitudes.findMin()).activationTime;
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
        if(date.before(initialDate)){
            return dummyAttitude;
        }else{
            current.activationTime = date;
            AttitudeIntervalBean att = (AttitudeIntervalBean) attitudes.find(current);

            if(prevAttitude != null && !att.name.equals(prevAttitude.name)){
                // Change!
                EventManager.instance.post(Events.POST_NOTIFICATION, "Gaia attitude changed to " + att.toString() + " at time " + att.activationTime);
            }

            // Get actual attitude
            return att.get(date);
        }


    }

}
