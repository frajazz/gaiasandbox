package gaia.cu9.ari.gaiaorbit.data.octreegen;

import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.List;

/**
 * Interface to be implemented by all algorithms that create a group of virtual particles
 * for an octant.
 * @author Toni Sagrista
 *
 */
public interface IAggregationAlgorithm<T extends SceneGraphNode> {

    /**
     * Creates the sample of virtual stars from the given input stars. All these stars should be in the box
     * defined by the center and the sizes.
     * @param inputStars The actual stars that are inside the octant.
     * @param octant The octant that characterizes the box with its center, size and depth well set.
     * @param maxLevelObjects The number of objects of the octant with the most objects in this level.
     * @return True if we are in a leaf.
     */
    public boolean sample(List<T> inputStars, OctreeNode<T> octant, int maxLevelObjects);

}