package gaia.cu9.ari.gaiaorbit.util.concurrent;

import gaia.cu9.ari.gaiaorbit.util.concurrent.GaiaSandboxThreadFactory.GSThread;

/**
 * Thread indexer for a multithread environment.
 * @author Toni Sagrista
 *
 */
public class MultiThreadIndexer extends ThreadIndexer {

    @Override
    public int i() {
	return ((GSThread) Thread.currentThread()).index;
    }

}