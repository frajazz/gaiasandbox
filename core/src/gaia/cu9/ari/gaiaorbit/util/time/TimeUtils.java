package gaia.cu9.ari.gaiaorbit.util.time;

public class TimeUtils {

    public static long startTime;

    static {
        startTime = System.currentTimeMillis();
    }

    public static float getRunningTimeSecs() {
        return (System.currentTimeMillis() - startTime) / 1000f;
    }

}
