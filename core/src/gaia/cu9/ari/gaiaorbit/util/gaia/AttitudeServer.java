package gaia.cu9.ari.gaiaorbit.util.gaia;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
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

    public enum AttitudeType {
        /** Ecliptic Pole Scanning Law (EPSL) used preferably until mid-March 2014 **/
        EPSL_STAR_FOLLOW,
        /** EPSL scanning law used preferably after mid-March 2014 and for the first
         30 days of the routine phase **/
        EPSL_STAR_PRECEDE,

        EPSL_FOLLOW,
        /** Scanning law (after spin rate update) covering EPSL and its transition to NSL
         over South Ecliptic Pole and continuation in NSL **/
        TSL,
        /** Nominal Scanning Law (NSL), as close a possible to a galactic plane scan **/
        NSL_STAR_GPS,
        /** Nominal Scanning Law after spin rate update, incl. a galactic plane scan **/
        NSL_GPS,
        /** Continuous transition from EPSL into NSL **/
        NSL_CONT,
        /** NSL with GAREQ-optimized precession phase **/
        NSL_GAREQ0,
        /** NSL with GAREQ-optimized spin phase **/
        NSL_GAREQ1,
        /** NSL with updated GAREQ-optimized spin phase **/
        NSL_GAREQn,
        /** NSL with a solar aspect angle of 0 **/
        SAA0,
        /** NSL with a reduced solar aspect angle **/
        SAA42;

    }

    // Map that holds all the attitudes
    static Map<AttitudeType, AttitudeCache> attitudeMap;

    private static class AttitudeCache {
        public AnalyticalAttitudeDataServer attitude;
        public Map<Long, Attitude> cache;
        public long hits = 0, misses = 0;

        public AttitudeCache(Class<? extends AnalyticalAttitudeDataServer> clazz, Class<?>[] paramTypes, Object[] paramValues) {
            try {
                attitude = clazz.getConstructor(paramTypes).newInstance(paramValues);
            } catch (Exception e) {
                Logger.error(e);
            }
            cache = Collections.synchronizedMap(new LruCache<Long, Attitude>(10));
        }

    }

    // List
    static List<Pair<Date, AttitudeType>> timeSlots;
    static final int N_SLOTS = 13;

    // Dummy attitude for launch sequence
    static Attitude dummyAttitude;

    static {
        attitudeMap = new HashMap<AttitudeType, AttitudeCache>();

        // EPSL* FOLLOWING
        attitudeMap.put(AttitudeType.EPSL_STAR_FOLLOW, new AttitudeCache(Epsl.class, new Class[] { Epsl.Mode.class }, new Object[] { Epsl.Mode.FOLLOWING }));
        // EPSL* PRECEDE
        attitudeMap.put(AttitudeType.EPSL_STAR_PRECEDE, new AttitudeCache(Epsl.class, new Class[] { Epsl.Mode.class }, new Object[] { Epsl.Mode.PRECEDING }));
        // EPSL FOLLOWING
        attitudeMap.put(AttitudeType.EPSL_FOLLOW, new AttitudeCache(Epsl.class, new Class[] { Epsl.Mode.class }, new Object[] { Epsl.Mode.FOLLOWING }));
        // TSL 1 day, 2 hours, 6 min, 21 sec (26.1 hours or 1566.3 min or 93981 sec) duration
        attitudeMap.put(AttitudeType.TSL, new AttitudeCache(TransitionScanningLaw.class, new Class[] { Duration.class }, new Object[] { new Secs(93981) }));
        // NSL* GPS
        attitudeMap.put(AttitudeType.NSL_STAR_GPS, new AttitudeCache(Nsl37.class, null, null));
        // NSL GPS
        attitudeMap.put(AttitudeType.NSL_GPS, new AttitudeCache(Nsl37.class, null, null));
        // NSL CONT
        attitudeMap.put(AttitudeType.NSL_CONT, new AttitudeCache(Nsl37.class, null, null));
        // NSL GAREQ0
        attitudeMap.put(AttitudeType.NSL_GAREQ0, new AttitudeCache(Nsl37.class, null, null));
        // NSL GAREQ1
        attitudeMap.put(AttitudeType.NSL_GAREQ1, new AttitudeCache(Nsl37.class, null, null));
        // NSL GAREQn
        attitudeMap.put(AttitudeType.NSL_GAREQn, new AttitudeCache(Nsl37.class, null, null));
        // SAA0
        attitudeMap.put(AttitudeType.SAA0, new AttitudeCache(Nsl37.class, null, null));
        attitudeMap.get(AttitudeType.SAA0).attitude.setXiRef(0);
        // SAA42
        attitudeMap.put(AttitudeType.SAA42, new AttitudeCache(Nsl37.class, null, null));
        attitudeMap.get(AttitudeType.SAA42).attitude.setXiRef(Math.toRadians(42));

        // Prepare time slots list
        timeSlots = new ArrayList<Pair<Date, AttitudeType>>(N_SLOTS);
        timeSlots.add(new Pair(getDate("2014-01-15 15:43:04"), AttitudeType.EPSL_STAR_FOLLOW));
        timeSlots.add(new Pair(getDate("2014-02-17 18:03:28"), AttitudeType.SAA42));
        timeSlots.add(new Pair(getDate("2014-02-27 14:13:04"), AttitudeType.SAA0));
        timeSlots.add(new Pair(getDate("2014-02-28 17:20:16"), AttitudeType.EPSL_STAR_PRECEDE));
        timeSlots.add(new Pair(getDate("2014-05-02 00:00:00"), AttitudeType.NSL_STAR_GPS));
        timeSlots.add(new Pair(getDate("2014-05-09 21:33:00"), AttitudeType.EPSL_STAR_PRECEDE));
        timeSlots.add(new Pair(getDate("2014-05-24 10:55:00"), AttitudeType.TSL));
        timeSlots.add(new Pair(getDate("2014-05-25 13:01:21"), AttitudeType.NSL_GPS));
        timeSlots.add(new Pair(getDate("2014-06-02 08:52:57"), AttitudeType.EPSL_STAR_PRECEDE));
        timeSlots.add(new Pair(getDate("2014-06-06 15:00:09"), AttitudeType.EPSL_STAR_FOLLOW));
        timeSlots.add(new Pair(getDate("2014-07-03 08:48:33"), AttitudeType.EPSL_STAR_FOLLOW));
        timeSlots.add(new Pair(getDate("2014-08-22 04:02:21"), AttitudeType.EPSL_FOLLOW));
        timeSlots.add(new Pair(getDate("2014-09-25 11:35:17"), AttitudeType.NSL_GAREQ1));

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

    private static AttitudeType lastAttitudeType = null;

    /**
     * Returns the NSL37 attitude for the given date.
     * @param date
     * @return
     */
    public synchronized static Attitude getAttitude(Date date) {
        // Find AttitudeType in timeSlots
        Pair<Date, AttitudeType> current;
        AttitudeType at = null;

        if (date.after(timeSlots.get(N_SLOTS - 1).getFirst())) {
            current = timeSlots.get(N_SLOTS - 1);
            at = current.getSecond();
        } else {

            int i = 0;
            current = timeSlots.get(i);
            if (date.before(current.getFirst())) {
                // We are in launch sequence!
                return dummyAttitude;
            }

            while (++i < N_SLOTS) {
                current = timeSlots.get(i);
                if (date.before(current.getFirst())) {
                    // Found it!
                    current = timeSlots.get(i - 1);
                    break;
                }
            }
        }
        at = current.getSecond();

        if (lastAttitudeType != null && !at.equals(lastAttitudeType)) {
            // Change!
            EventManager.instance.post(Events.POST_NOTIFICATION, "Gaia attitude changed to " + at.toString() + " at time " + current.getFirst());
        }
        lastAttitudeType = at;

        // Get actual attitude
        AttitudeCache cache = attitudeMap.get(at);
        Long time = date.getTime();
        if (!cache.cache.containsKey(time)) {
            Attitude att = cache.attitude.getAttitude(date);
            cache.cache.put(time, att);
            cache.misses++;
            return att;
        } else {
            cache.hits++;
        }
        return (Attitude) cache.cache.get(time);
    }

}
