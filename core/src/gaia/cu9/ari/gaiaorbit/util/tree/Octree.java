package gaia.cu9.ari.gaiaorbit.util.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

public class Octree<T extends IPosition> implements Iterable<OctreeNode<T>> {

    public double size; //size of the galaxy/tree
    public final int depth;
    public OctreeNode<T> root = null;

    /**
     * Creates an octree according to the given parameters
     * @param depth the number of levels the tree has
     * @param size the size of one side of the cube,
     *        in arbitrary units.
     */
    public Octree(int depth, double size) {
	this.depth = depth;
	this.size = size;
	root = new OctreeNode<T>(this, 0, -size / 2d, -size / 2d, -size / 2d);
    }

    /**
     * Inserts a star into the octree.
     * @param e the star to be inserted
     * @param level how common the star is, rare stars get a lower level. In general, no levels should be skipped.
     * @return returns true if insertion was successful, false otherwise. 
     */
    public boolean insert(T e, int level) {
	if (level > this.depth)
	    return false;
	if (e.getPosition().x > size)
	    return false;
	if (e.getPosition().y > size)
	    return false;
	if (e.getPosition().z > size)
	    return false;

	if (level == 0) {
	    return root.add(e);
	} else {
	    return root.insert(e, level);
	}
    }

    public TreeSet<T> toTreeSet() {
	TreeSet<T> tree = new TreeSet<T>();
	root.toTree(tree);
	return tree;
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
