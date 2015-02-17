package gaia.cu9.ari.gaiaorbit.util.tree;

import gaia.cu9.ari.gaiaorbit.util.Pair;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

/**
 * Octree node implementation which contains a list of {@link IPosition} objects
 * and possibly 8 subnodes.
 * @author Toni Sagrista
 *
 * @param <T> The type of object that the octree holds.
 */
public class OctreeNode<T extends IPosition> {

    /** The unique page identifier **/
    final long pageid;
    /** Contains the bottom-left-front position of the node **/
    final Vector3d loc;
    /** Contains the top-right-back position of the cube **/
    final Vector3d boundary;
    /** Contains the depth level **/
    final int depth;
    /** Number of children of this node **/
    final int childrenCount;
    /** The parent, if any **/
    OctreeNode<T> parent;
    /** Children nodes **/
    @SuppressWarnings("unchecked")
    OctreeNode<T>[] children = new OctreeNode[8];
    /** List of objects **/
    LinkedList<T> objects = new LinkedList<T>();

    /**
     * Constructs an octree node.
     * @param pageid The page id.
     * @param x The x coordinate of the center.
     * @param y The y coordinate of the center.
     * @param z The z coordinate of the center.
     * @param hsx The half-size in x.
     * @param hsy The half-size in y.
     * @param hsz The half-size in z.
     */
    public OctreeNode(long pageid, double x, double y, double z, double hsx, double hsy, double hsz, int childrenCount, int depth) {
	this.pageid = pageid;
	this.loc = new Vector3d(x - hsx, y - hsy, z - hsz);
	this.boundary = new Vector3d(x + hsx, y + hsy, z + hsz);
	this.childrenCount = childrenCount;
	this.depth = depth;
    }

    /** 
     * Resolves and adds the children of this node using the map. It runs recursively
     * once the children have been added.
     * @param map
     */
    public void resolveChildren(Map<Long, Pair<OctreeNode<T>, long[]>> map) {
	Pair<OctreeNode<T>, long[]> me = map.get(pageid);
	if (me == null) {
	    throw new RuntimeException("OctreeNode with page ID " + pageid + " not found in map");
	}

	long[] childrenIds = me.getSecond();
	int i = 0;
	for (long childId : childrenIds) {
	    if (childId >= 0) {
		// Child exists
		OctreeNode<T> child = map.get(childId).getFirst();
		children[i] = child;
		child.parent = this;
	    } else {
		// No node in this position
	    }
	    i++;
	}

	// Recursive running
	for (int j = 0; j < children.length; j++) {
	    OctreeNode<T> child = children[j];
	    if (child != null) {
		child.resolveChildren(map);
	    }
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

    public String toString() {
	StringBuffer str = new StringBuffer(depth);
	for (int i = 0; i < depth; i++) {
	    str.append("    ");
	}
	str.append(pageid).append("(").append(depth).append(")");
	if (parent != null) {
	    str.append(" [i: ").append(Arrays.asList(parent.children).indexOf(this)).append(", obj: ");
	} else {
	    str.append("[obj: ");
	}
	str.append(objects.size()).append(", nchld: ").append(childrenCount).append("]\n");
	if (childrenCount > 0) {
	    for (OctreeNode<T> child : children) {
		if (child != null) {
		    str.append(child.toString());
		}
	    }
	}
	return str.toString();
    }
}
