package gaia.cu9.ari.gaiaorbit.util.comp;

import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;

import java.util.Comparator;

/**
 * Compares quads. Further quads go first, nearer quads go last.
 * @author Toni Sagrista
 *
 * @param <IRenderable>
 */
public class QuadComparator<T> implements Comparator<T> {

    @Override
    public int compare(T o1, T o2) {
	return -Float.compare(((AbstractPositionEntity) o1).distToCamera, ((AbstractPositionEntity) o2).distToCamera);
    }

}
