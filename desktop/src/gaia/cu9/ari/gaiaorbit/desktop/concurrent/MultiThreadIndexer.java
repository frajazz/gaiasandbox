package gaia.cu9.ari.gaiaorbit.desktop.concurrent;

import gaia.cu9.ari.gaiaorbit.util.concurrent.GaiaSandboxThreadFactory.GSThread;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadIndexer;

/**
 * Thread indexer for a multithread environment.
 * @author Toni Sagrista
 *
 */
public class MultiThreadIndexer extends ThreadIndexer {

    @Override
    public int i() {
        return Thread.currentThread() instanceof GSThread ? ((GSThread) Thread.currentThread()).index : 0;
    }

}
