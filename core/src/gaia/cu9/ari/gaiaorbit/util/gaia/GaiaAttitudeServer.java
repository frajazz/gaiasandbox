package gaia.cu9.ari.gaiaorbit.util.gaia;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.BinarySearchTree;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.math.Quaterniond;

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

    public GaiaAttitudeServer(String folder) {
        attitudes = AttitudeXmlParser.parseFolder(folder);
        initialDate = ((AttitudeIntervalBean) attitudes.findMin()).activationTime;
        current = new AttitudeIntervalBean("current", null, null, "dummy");
        // Dummy attitude
        dummyAttitude = new ConcreteAttitude(0, new Quaterniond(), false);
        nsl = new Nsl37();
    }

    /**
     * Returns the NSL37 attitude for the given date.
     * @param date
     * @return
     */
    public synchronized Attitude getAttitude(Date date) {

        //return nsl.getAttitude(date);
        // Find AttitudeType in timeSlots
        if (date.before(initialDate)) {
            return dummyAttitude;
        } else {
            current.activationTime = date;
            AttitudeIntervalBean att = (AttitudeIntervalBean) attitudes.findIntervalStart(current);

            if (prevAttitude != null && !att.equals(prevAttitude)) {
                // Change!
                EventManager.instance.post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.attitude.changed", att.toString(), att.activationTime));
            }

            prevAttitude = att;

            // Get actual attitude
            return att.get(date);
        }

    }

    public synchronized String getCurrentAttitudeName() {
        if (prevAttitude != null) {
            return prevAttitude.file;
        }
        return null;
    }

}
