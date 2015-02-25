package gaia.cu9.ari.gaiaorbit.util.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPoolManager {

    /** The executor service containing the pool **/
    public static ThreadPoolExecutor pool;

    public static void initialize(int numThreads) {
	pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(numThreads, new GaiaSandboxThreadFactory());
    }

}
