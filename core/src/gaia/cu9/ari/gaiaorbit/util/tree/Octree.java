package gaia.cu9.ari.gaiaorbit.util.tree;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Static generic octree implementation. Nodes are not divided upon insertion.
 * The structure of the octree is supposed to be worked out elsewhere.
 * @author Toni Sagrista
 *
 * @param <T> The type of object that the octree holds.
 */
public class Octree<T extends IPosition> implements Iterable<OctreeNode<T>> {

    public double size; //size of the galaxy/tree
    public OctreeNode<T> root = null;

    public Octree(int depth, double size, OctreeNode<T> root) {
	this.size = size;
	this.root = root;
    }

    public ArrayList<OctreeNode<T>> toList() {
	ArrayList<OctreeNode<T>> tree = new ArrayList<OctreeNode<T>>();
	tree.add(root);
	root.addChildrenToList(tree);
	return tree;
    }

    @Override
    public Iterator<OctreeNode<T>> iterator() {
	return this.toList().iterator();
    }

}
