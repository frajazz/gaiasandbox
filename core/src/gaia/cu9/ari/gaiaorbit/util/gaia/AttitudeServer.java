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
        NSL, EPSL;

    }

    static Map<AttitudeType, AttitudeCache> attitudeMap;

    private static class AttitudeCache {
        public BaseAttitudeDataServer attitude;
        public Map<Long, Attitude> cache;
        public long hits = 0, misses = 0;

        public AttitudeCache(Class<? extends BaseAttitudeDataServer> clazz) {
            try {
                attitude = clazz.newInstance();
            } catch (Exception e) {
                Logger.error(e);
            }
            cache = Collections.synchronizedMap(new LruCache<Long, Attitude>(10));
        }

    }

    static {
        attitudeMap = new HashMap<AttitudeType, AttitudeCache>();

        attitudeMap.put(AttitudeType.NSL, new AttitudeCache(Nsl37.class));
        attitudeMap.put(AttitudeType.EPSL, new AttitudeCache(Epsl.class));
    }

    /**
     * Returns the NSL37 attitude for the given date.
     * @param date
     * @return
     */
    public synchronized static Attitude getAttitude(Date date) {
        AttitudeCache cache = attitudeMap.get(AttitudeType.EPSL);
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
