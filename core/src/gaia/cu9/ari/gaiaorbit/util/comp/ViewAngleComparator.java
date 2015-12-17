package gaia.cu9.ari.gaiaorbit.util.comp;

import java.util.Comparator;

import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;

/**
 * Compares entities. Further entities go first, nearer entities go last.
 * @author Toni Sagrista
 *
 * @param <IRenderable>
 */
public class ViewAngleComparator<T> implements Comparator<T> {

    @Override
    public int compare(T o1, T o2) {

        return Float.compare(((AbstractPositionEntity) o1).viewAngleApparent, ((AbstractPositionEntity) o2).viewAngleApparent);

    }

}
