package gaia.cu9.ari.gaiaorbit.util.concurrent;

import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

/**
 * Class that returns the thread inxes.
 * @author Toni Sagrista
 *
 */
public abstract class ThreadIndexer {

    public static ThreadIndexer inst;

    public static void initialize() {
        if (GlobalConf.performance.MULTITHREADING) {
            inst = new MultiThreadIndexer();
        } else {
            inst = new SingleThreadIndexer();
        }
    }

    /**
     * Gets the index of the current thread
     * @return
     */
    public abstract int i();

}
