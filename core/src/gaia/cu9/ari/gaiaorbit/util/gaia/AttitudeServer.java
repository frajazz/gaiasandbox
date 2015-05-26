package gaia.cu9.ari.gaiaorbit.util.gaia;

import gaia.cu9.ari.gaiaorbit.util.LruCache;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * Provides caching of the last Nsl37 attitude requested.
 * This allows for calculating the attitude only once in each
 * time step and using it in several points in the processing.
 * @author Toni Sagrista
 *
 */
public class AttitudeServer {

    static Nsl37 nsl;

    static Map<Long, Attitude> cache;
    static long hits = 0, misses = 0;
    static {
        nsl = new Nsl37();
        cache = Collections.synchronizedMap(new LruCache<Long, Attitude>(10));
    }

    /**
     * Returns the NSL37 attitude for the given date.
     * @param date
     * @return
     */
    public synchronized static Attitude getAttitude(Date date) {
        Long time = date.getTime();
        if (!cache.containsKey(time)) {
            Attitude att = nsl.getAttitudeNative(date);
            cache.put(time, att);
            misses++;
            return att;
        } else {
            hits++;
        }
        return cache.get(time);
    }

}
