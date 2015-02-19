package gaia.cu9.ari.gaiaorbit.util.tree;

import gaia.cu9.ari.gaiaorbit.render.ILineRenderable;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.util.Pair;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Pools;

/**
 * Octree node implementation which contains a list of {@link IPosition} objects
 * and possibly 8 subnodes.
 * @author Toni Sagrista
 *
 * @param <T> The type of object that the octree holds.
 */
public class OctreeNode<T extends IPosition> implements ILineRenderable {
    /** Max depth of the structure this node belongs to **/
    public static int maxDepth;

    /** The unique page identifier **/
    public final long pageId;
    /** Contains the bottom-left-front position of the node **/
    public final Vector3d loc;
    /** Contains the top-right-back position of the cube **/
    public final Vector3d boundary;
    /** Octant size in x, y and z **/
    public final Vector3d size;
    /** Contains the depth level **/
    public final int depth;
    /** Number of objects contained in this node and its descendants **/
    public final int nObjects;
    /** Number of objects contained in this node **/
    public final int ownObjects;
    /** Number of children nodes of this node **/
    public final int childrenCount;
    /** The parent, if any **/
    public OctreeNode<T> parent;
    /** Children nodes **/
    @SuppressWarnings("unchecked")
    public OctreeNode<T>[] children = new OctreeNode[8];
    /** List of objects **/
    public LinkedList<T> objects = new LinkedList<T>();
    /** Camera transform to render **/
    Vector3d transform;

    /**
     * Constructs an octree node.
     * @param pageId The page id.
     * @param x The x coordinate of the center.
     * @param y The y coordinate of the center.
     * @param z The z coordinate of the center.
     * @param hsx The half-size in x.
     * @param hsy The half-size in y.
     * @param hsz The half-size in z.
     * @param childrenCount Number of children nodes. Same as non null positions in children vector.
     * @param nObjects Number of objects contained in this node and its descendants.
     * @param ownObjects Number of objects contained in this node. Same as objects.size().
     */
    public OctreeNode(long pageId, double x, double y, double z, double hsx, double hsy, double hsz, int childrenCount, int nObjects, int ownObjects, int depth) {
	this.pageId = pageId;
	this.loc = new Vector3d(x - hsx, y - hsy, z - hsz);
	this.boundary = new Vector3d(x + hsx, y + hsy, z + hsz);
	this.size = new Vector3d(hsx * 2, hsy * 2, hsz * 2);
	this.childrenCount = childrenCount;
	this.nObjects = nObjects;
	this.ownObjects = ownObjects;
	this.depth = depth;
	this.transform = new Vector3d();
    }

    /** 
     * Resolves and adds the children of this node using the map. It runs recursively
     * once the children have been added.
     * @param map
     */
    public void resolveChildren(Map<Long, Pair<OctreeNode<T>, long[]>> map) {
	Pair<OctreeNode<T>, long[]> me = map.get(pageId);
	if (me == null) {
	    throw new RuntimeException("OctreeNode with page ID " + pageId + " not found in map");
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
		if (children[i] != null) {
		    tree.add(children[i]);
		    children[i].addChildrenToList(tree);
		}
	    }
	}
    }

    public String toString() {
	StringBuffer str = new StringBuffer(depth);
	for (int i = 0; i < depth; i++) {
	    str.append("    ");
	}
	str.append(pageId).append("(").append(depth).append(")");
	if (parent != null) {
	    str.append(" [i: ").append(Arrays.asList(parent.children).indexOf(this)).append(", ownobj: ");
	} else {
	    str.append("[ownobj: ");
	}
	str.append(objects.size()).append("/").append(ownObjects).append(", recobj: ").append(nObjects).append(", nchld: ").append(childrenCount).append("]\n");
	if (childrenCount > 0) {
	    for (OctreeNode<T> child : children) {
		if (child != null) {
		    str.append(child.toString());
		}
	    }
	}
	return str.toString();
    }

    @Override
    public void render(Object... params) {
	render((ShapeRenderer) params[0], (Float) params[1]);
    }

    @Override
    public ComponentType getComponentType() {
	return ComponentType.Others;
    }

    @Override
    public float getDistToCamera() {
	return 0;
    }

    /**
     * Adds nodes to render lists.
     */
    public void update(ICamera cam) {
	transform.set(cam.getInversePos());
	for (OctreeNode<T> child : children) {
	    if (child != null) {
		child.update(cam);
	    }
	}
    }

    @Override
    public void render(ShapeRenderer sr, float alpha) {
	float maxDepth = OctreeNode.maxDepth * 2;
	// Color depends on depth
	Color col = new Color(Color.HSBtoRGB((float) depth / (float) maxDepth, 1f, 0.5f));

	alpha *= MathUtilsd.lint(depth, 0, maxDepth, 1.0, 0.5);
	sr.setColor(col.getRed() * alpha, col.getGreen() * alpha, col.getBlue() * alpha, alpha);

	// Camera correction
	Vector3d loc = Pools.get(Vector3d.class).obtain();
	loc.set(this.loc).add(transform);

	/*
	 *       .·------·
	 *     .' |    .'|
	 *    +---+--·'  |
	 *    |   |  |   |
	 *    |  ,+--+---·
	 *    |.'    | .'
	 *    +------+' 
	 */
	line(sr, loc.x, loc.y, loc.z, loc.x + size.x, loc.y, loc.z);
	line(sr, loc.x, loc.y, loc.z, loc.x, loc.y + size.y, loc.z);
	line(sr, loc.x, loc.y, loc.z, loc.x, loc.y, loc.z + size.z);

	/*
	 *       .·------·
	 *     .' |    .'|
	 *    ·---+--+'  |
	 *    |   |  |   |
	 *    |  ,·--+---+
	 *    |.'    | .'
	 *    ·------+' 
	 */
	line(sr, loc.x + size.x, loc.y, loc.z, loc.x + size.x, loc.y + size.y, loc.z);
	line(sr, loc.x + size.x, loc.y, loc.z, loc.x + size.x, loc.y, loc.z + size.z);

	/*
	 *       .·------+
	 *     .' |    .'|
	 *    ·---+--·'  |
	 *    |   |  |   |
	 *    |  ,+--+---+
	 *    |.'    | .'
	 *    ·------·' 
	 */
	line(sr, loc.x + size.x, loc.y, loc.z + size.z, loc.x, loc.y, loc.z + size.z);
	line(sr, loc.x + size.x, loc.y, loc.z + size.z, loc.x + size.x, loc.y + size.y, loc.z + size.z);

	/*
	 *       .+------·
	 *     .' |    .'|
	 *    ·---+--·'  |
	 *    |   |  |   |
	 *    |  ,+--+---·
	 *    |.'    | .'
	 *    ·------·' 
	 */
	line(sr, loc.x, loc.y, loc.z + size.z, loc.x, loc.y + size.y, loc.z + size.z);

	/*
	 *       .+------+
	 *     .' |    .'|
	 *    +---+--+'  |
	 *    |   |  |   |
	 *    |  ,·--+---·
	 *    |.'    | .'
	 *    ·------·' 
	 */
	line(sr, loc.x, loc.y + size.y, loc.z, loc.x + size.x, loc.y + size.y, loc.z);
	line(sr, loc.x, loc.y + size.y, loc.z, loc.x, loc.y + size.y, loc.z + size.z);
	line(sr, loc.x, loc.y + size.y, loc.z + size.z, loc.x + size.x, loc.y + size.y, loc.z + size.z);
	line(sr, loc.x + size.x, loc.y + size.y, loc.z, loc.x + size.x, loc.y + size.y, loc.z + size.z);

	Pools.get(Vector3d.class).free(loc);
    }

    /**
     * Draws a line.
     */
    private void line(ShapeRenderer sr, double x, double y, double z, double x1, double y1, double z1) {
	sr.line((float) x, (float) y, (float) z, (float) x1, (float) y1, (float) z1);
    }
}
