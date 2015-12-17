package gaia.cu9.ari.gaiaorbit.util.comp;

import java.util.Comparator;

import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;

/**
 * Compares entities. Further entities go first, nearer entities go last.
 * @author Toni Sagrista
 *
 * @param <IRenderable>
 */
public class ViewAngleComparator<T> implements Comparator<T> {

    @Override
    public int compare(T o1, T o2) {
        boolean obs1 = !(o1 instanceof Particle) || (o1 instanceof Particle && ((Particle) o1).octant != null && ((Particle) o1).octant.observed);
        boolean obs2 = !(o2 instanceof Particle) || (o2 instanceof Particle && ((Particle) o2).octant != null && ((Particle) o2).octant.observed);
        if ((obs1 && obs2) || (!obs1 && !obs2))
            return Float.compare(((AbstractPositionEntity) o1).viewAngleApparent, ((AbstractPositionEntity) o2).viewAngleApparent);
        else if (obs1 && !obs2) {
            return 1;
        } else if (!obs1 && obs2) {
            return -1;
        }
        return 0;
    }

}
