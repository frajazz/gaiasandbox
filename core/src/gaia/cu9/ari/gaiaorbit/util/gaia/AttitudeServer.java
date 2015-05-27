package gaia.cu9.ari.gaiaorbit.util.gaia;

import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.LruCache;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides caching of the last Nsl37 attitude requested.
 * This allows for calculating the attitude only once in each
 * time step and using it in several points in the processing.
 * @author Toni Sagrista
 *
 */
public class AttitudeServer {

    public enum AttitudeType {
        /** Nominal Scanning Law (NSL), as close a possible to a galactic plane scan **/
        NSL_STAR_GPS,
        /** Ecliptic Pole Scanning Law (EPSL) used preferably until mid-March 2014 **/
        EPSL_STAR_FOLLOW,
        /** EPSL scanning law used preferably after mid-March 2014 and for the first
         30 days of the routine phase **/
        EPSL_STAR_PRECEDE,
        /** Scanning law (after spin rate update) covering EPSL and its transition to NSL
         over South Ecliptic Pole and continuation in NSL **/
        TSL,
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

    static {
        attitudeMap = new HashMap<AttitudeType, AttitudeCache>();

        // Solar aspect angle = 0
        attitudeMap.put(AttitudeType.NSL_STAR_GPS, new AttitudeCache(Nsl37.class, null, null));
        //attitudeMap.get(AttitudeType.NSL_STAR_GPS).attitude.setXiRef(Math.toRadians(10));

        attitudeMap.put(AttitudeType.EPSL_STAR_PRECEDE, new AttitudeCache(Epsl.class, new Class[]{Epsl.Mode.class}, new Object[]{ Epsl.Mode.PRECEDING}));



    }

    /**
     * Returns the NSL37 attitude for the given date.
     * @param date
     * @return
     */
    public synchronized static Attitude getAttitude(Date date) {
        AttitudeCache cache = attitudeMap.get(AttitudeType.NSL_STAR_GPS);
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
