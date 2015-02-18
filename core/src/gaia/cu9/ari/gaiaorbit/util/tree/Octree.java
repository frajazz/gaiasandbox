package gaia.cu9.ari.gaiaorbit.util.tree;

import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Static octree holder. Holds a static octree node which is the root of the
 * tree, the 0 depth node.
 * @author Toni Sagrista
 *
 */
public class Octree implements Iterable<OctreeNode<AbstractPositionEntity>> {

    public static OctreeNode<AbstractPositionEntity> root = null;

    public ArrayList<OctreeNode<AbstractPositionEntity>> toList() {
	ArrayList<OctreeNode<AbstractPositionEntity>> tree = new ArrayList<OctreeNode<AbstractPositionEntity>>();
	tree.add(root);
	root.addChildrenToList(tree);
	return tree;
    }

    @Override
    public Iterator<OctreeNode<AbstractPositionEntity>> iterator() {
	return this.toList().iterator();
    }

}
