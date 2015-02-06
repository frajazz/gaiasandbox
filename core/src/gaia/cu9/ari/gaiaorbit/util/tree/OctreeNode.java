package gaia.cu9.ari.gaiaorbit.util.tree;

import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeSet;

public class OctreeNode<T extends IPosition> {

    private Octree<T> octree;
    /** Contains the bottom-left-front position of the node **/
    final Vector3d loc;
    /** Contains the top-right-back position of the cube **/
    final Vector3d boundary;
    /** Contains the depth level **/
    final int depth;
    /** Children nodes **/
    OctreeNode<T>[] children = new OctreeNode[8];
    /** List of objects **/
    LinkedList<T> objects = new LinkedList<T>();

    /**
     * Constructs a node with the given depth in the given position.
     * @param octree The parent.
     * @param depth The depth.
     * @param x X coordinate of the bottom-left-front vertex.
     * @param y Y coordinate of the bottom-left-front vertex.
     * @param z Z coordinate of the bottom-left-front vertex.
     */
    public OctreeNode(Octree<T> octree, int depth, double x, double y, double z) {
	this.octree = octree;
	double mySize = this.octree.size / Math.pow(2, depth);

	loc = new Vector3d(x, y, z);
	boundary = new Vector3d(x + mySize, y + mySize, z + mySize);
	this.depth = depth;
	if (this.depth <= octree.depth) {
	    //create subChildren at proper locations
	    children[0] = new OctreeNode<T>(octree, depth + 1, loc.x, loc.y, loc.z);
	    children[1] = new OctreeNode<T>(octree, depth + 1, loc.x + ((boundary.x - loc.x) / 2), loc.y, loc.z);
	    children[2] = new OctreeNode<T>(octree, depth + 1, loc.x, loc.y, loc.z + ((boundary.z - loc.z) / 2));
	    children[3] = new OctreeNode<T>(octree, depth + 1, loc.x + ((boundary.x - loc.x) / 2), loc.y, loc.z + ((boundary.z - loc.z) / 2));
	    children[4] = new OctreeNode<T>(octree, depth + 1, loc.x, loc.y + ((boundary.y - loc.y) / 2), loc.z);
	    children[5] = new OctreeNode<T>(octree, depth + 1, loc.x + ((boundary.x - loc.x) / 2), loc.y + ((boundary.y - loc.y) / 2), loc.z);
	    children[6] = new OctreeNode<T>(octree, depth + 1, loc.x, loc.y + ((boundary.y - loc.y) / 2), loc.z + ((boundary.z - loc.z) / 2));
	    children[7] = new OctreeNode<T>(octree, depth + 1, loc.x + ((boundary.x - loc.x) / 2), loc.y + ((boundary.y - loc.y) / 2), loc.z + ((boundary.z - loc.z) / 2));
	} else {
	    children = null;
	}
    }

    public boolean add(T e) {
	objects.add(e);
	return true;
    }

    public boolean insert(T e, int level) {
	int node = 0;
	if (e.getPosition().y > loc.y + ((boundary.y - loc.y) / 2))
	    node += 4;
	if (e.getPosition().z > loc.z + ((boundary.z - loc.z) / 2))
	    node += 2;
	if (e.getPosition().x > loc.x + ((boundary.x - loc.x) / 2))
	    node += 1;
	if (level == this.depth + 1) {
	    return children[node].add(e);
	} else {
	    return children[node].insert(e, level);
	}
    }

    public void toTree(TreeSet<T> tree) {
	for (T i : objects) {
	    tree.add(i);
	}
	if (children != null) {
	    for (int i = 0; i < 8; i++) {
		children[i].toTree(tree);
	    }
	}
    }

    public void addChildrenToList(ArrayList<OctreeNode<T>> tree) {
	if (children != null) {
	    for (int i = 0; i < 8; i++) {
		tree.add(children[i]);
		children[i].addChildrenToList(tree);
	    }
	}
    }

}
