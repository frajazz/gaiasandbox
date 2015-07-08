package gaia.cu9.ari.gaiaorbit.util.concurrent;

/**
 * Class that returns the thread indices.
 * @author Toni Sagrista
 *
 */
public abstract class ThreadIndexer {

    private static ThreadIndexer inst;

    public static void setInstance(ThreadIndexer inst) {
        ThreadIndexer.inst = inst;
    }

    public static ThreadIndexer inst() {
        if (inst == null) {
            inst = new SingleThreadIndexer();
        }
        return inst;
    }

    /**
     * Gets the index of the current thread
     * @return
     */
    public abstract int i();

}
