package gaia.cu9.ari.gaiaorbit.util.concurrent;


/**
 * Single thread indexer. All indexes are 0.
 * @author Toni Sagrista
 *
 */
public class SingleThreadIndexer extends ThreadIndexer {

    @Override
    public int idx() {
        return 0;
    }

}
