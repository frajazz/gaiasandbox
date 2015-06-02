package gaia.cu9.ari.gaiaorbit.util.gaia;

import gaia.cu9.ari.gaiaorbit.util.LruCache;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * A bean that holds the attitude and its activation time.
 * It also has a cache.
 * Created by tsagrista on 02/06/15.
 */
public class AttitudeIntervalBean implements Comparable<AttitudeIntervalBean> {
    public String name;
    public Date activationTime;
    public BaseAttitudeDataServer attitude;

    public Map<Long, Attitude> cache;
    public long hits = 0, misses = 0;


    public AttitudeIntervalBean(String name, Date activationTime, BaseAttitudeDataServer attitude){
        this.name = name;
        this.activationTime = activationTime;
        this.attitude = attitude;

        cache = Collections.synchronizedMap(new LruCache<Long, Attitude>(10));
    }

    public Attitude get(Date date){
        Long time = date.getTime();
        if (!cache.containsKey(time)) {
            Attitude att = attitude.getAttitude(date);
            cache.put(time, att);
            misses++;
            return att;
        } else {
            hits++;
        }
        return (Attitude) cache.get(time);
    }

    @Override
    public int compareTo(AttitudeIntervalBean o) {
        return this.activationTime.compareTo(o.activationTime);
    }

    @Override
    public String toString() {
        return name;
    }
}
