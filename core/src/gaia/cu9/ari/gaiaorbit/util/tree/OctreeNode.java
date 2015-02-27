package gaia.cu9.ari.gaiaorbit.util.tree;

import gaia.cu9.ari.gaiaorbit.data.ObjectServerLoader;
import gaia.cu9.ari.gaiaorbit.render.ILineRenderable;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.Transform;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Pair;
import gaia.cu9.ari.gaiaorbit.util.math.BoundingBoxd;
import gaia.cu9.ari.gaiaorbit.util.math.Intersectord;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Rayd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Frustum;
import com.badlogic.gdx.math.Vector3;
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
    /** Angle threshold below which we stay with the current level. Lower limit of overlap **/
    public static final double ANGLE_THRESHOLD_1 = Math.toRadians(6d);
    /** Angle threshold above which we break the Octree. Upper limit of overlap **/
    public static final double ANGLE_THRESHOLD_2 = Math.toRadians(16d);
    /** Is dynamic loading active? **/
    public static boolean LOAD_ACTIVE;

    /** Since OctreeNode is not to be parallelized, this can be static **/
    private static BoundingBoxd boxcopy = new BoundingBoxd(new Vector3d(), new Vector3d());
    private static Matrix4d boxtransf = new Matrix4d();
    private static Vector3d auxD1 = new Vector3d(), auxD2 = new Vector3d(), auxD3 = new Vector3d(), auxD4 = new Vector3d();
    private static Vector3 auxF1 = new Vector3(), auxF2 = new Vector3();
    private static Rayd ray = new Rayd(new Vector3d(), new Vector3d());

    /** The load status of this node **/
    private LoadStatus status;
    /** The unique page identifier **/
    public final long pageId;
    /** Contains the bottom-left-front position of the octant **/
    public final Vector3d blf;
    /** Contains the top-right-back position of the octant **/
    public final Vector3d trb;
    /** The centre of this octant **/
    public final Vector3d centre;
    /** The bounding box **/
    public final BoundingBoxd box;
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
    public List<T> objects;

    private double radius;
    /** If observed, the view angle in radians of this octant **/
    public double viewAngle;
    /** The distance to the camera in units of the center of this octant **/
    public double distToCamera;
    /** Is this octant observed in this frame? **/
    public boolean observed;
    /** Camera transform to render **/
    Vector3d transform;
    /** The opacity of this node **/
    public float opacity;

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
	this.blf = new Vector3d(x - hsx, y - hsy, z - hsz);
	this.trb = new Vector3d(x + hsx, y + hsy, z + hsz);
	this.centre = new Vector3d(x, y, z);
	this.size = new Vector3d(hsx * 2, hsy * 2, hsz * 2);
	this.box = new BoundingBoxd(blf, trb);
	this.childrenCount = childrenCount;
	this.nObjects = nObjects;
	this.ownObjects = ownObjects;
	this.depth = depth;
	this.transform = new Vector3d();
	this.observed = false;
	this.status = LoadStatus.NOT_LOADED;

	this.radius = Math.sqrt(hsx * hsx + hsy * hsy + hsz * hsz);
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
	if (objects == null)
	    objects = new ArrayList<T>(100);
	objects.add(e);
	return true;
    }

    public void setObjects(List<T> l) {
	this.objects = l;
    }

    public boolean insert(T e, int level) {
	int node = 0;
	if (e.getPosition().y > blf.y + ((trb.y - blf.y) / 2))
	    node += 4;
	if (e.getPosition().z > blf.z + ((trb.z - blf.z) / 2))
	    node += 2;
	if (e.getPosition().x > blf.x + ((trb.x - blf.x) / 2))
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
     * Returns the deepest octant that contains the position.
     * @param position
     * @return
     */
    public OctreeNode<T> getBestOctant(Vector3d position) {
	if (!this.box.contains(position)) {
	    return null;
	} else {
	    OctreeNode<T> candidate = null;
	    for (int i = 0; i < 8; i++) {
		OctreeNode<T> child = children[i];
		if (child != null) {
		    candidate = child.getBestOctant(position);
		    if (candidate != null) {
			return candidate;
		    }
		}
	    }
	    // We could not found a candidate in our children, we use this node.
	    return this;
	}
    }

    private LoadStatus getLevelStatus(Integer level) {
	if (ObjectServerLoader.lodStatus[level] == null) {
	    return LoadStatus.NOT_LOADED;
	} else {
	    return ObjectServerLoader.lodStatus[level];
	}
    }

    /**
     * Computes the observed value and the transform of each observed node.
     * @param parentTransform The parent transform.
     * @param cam The current camera.
     * @param roulette List where the nodes to be processed are to be added.
     * @param opacity The opacity to set.
     */
    public void update(Transform parentTransform, ICamera cam, List<T> roulette, float opacity) {
	parentTransform.getTranslation(transform);
	this.opacity = opacity;

	// Is this octant observed??
	computeObserved2(parentTransform, cam);

	if (observed) {

	    /**
	     * Load individual pages
	     */
	    //	    if (status == LoadStatus.NOT_LOADED && LOAD_ACTIVE) {
	    //		// Add to load all the level
	    //		ObjectServerLoader.addToQueue(this.parent.children);
	    //	    }

	    /**
	     * Load whole levels of detail
	     */
	    if (getLevelStatus(depth) == LoadStatus.NOT_LOADED && LOAD_ACTIVE) {
		// Add current level of detail to load
		ObjectServerLoader.addToQueue(depth);
	    }

	    synchronized (this) {
		// Compute distance and view angle
		distToCamera = auxD1.set(centre).add(cam.getInversePos()).len();
		viewAngle = Math.atan(radius / distToCamera) / cam.getFovFactor();

		if (viewAngle < ANGLE_THRESHOLD_1) {
		    // Stay in current level
		    addObjectsTo(roulette);
		    setChildrenObserved(false);
		} else if (viewAngle > ANGLE_THRESHOLD_2) {
		    // Break down tree
		    if (childrenCount == 0) {
			// We are a leaf, add objects anyway
			addObjectsTo(roulette);
			setChildrenObserved(false);
		    } else {
			// Update children
			for (int i = 0; i < 8; i++) {
			    OctreeNode<T> child = children[i];
			    if (child != null) {
				child.update(parentTransform, cam, roulette, opacity);
			    }
			}
		    }
		} else {
		    // View angle between th1 and th2
		    addObjectsTo(roulette);
		    if (childrenCount > 0) {
			// Opacity = this?  1 - alpha : children? alpha
			double alpha = MathUtilsd.lint(viewAngle, ANGLE_THRESHOLD_1, ANGLE_THRESHOLD_2, 0d, 1d);
			// Update children
			this.opacity = 1f - (float) alpha;
			for (int i = 0; i < 8; i++) {
			    OctreeNode<T> child = children[i];
			    if (child != null) {
				child.update(parentTransform, cam, roulette, (float) alpha);
			    }
			}
		    }

		}
	    }
	}
    }

    private void addObjectsTo(List<T> roulette) {
	if (objects != null) {
	    roulette.addAll(objects);
	}
    }

    private void setChildrenObserved(boolean observed) {
	for (int i = 0; i < 8; i++) {
	    OctreeNode<T> child = children[i];
	    if (child != null) {
		child.observed = observed;
	    }
	}
    }

    public boolean isObserved() {
	return observed && (parent == null ? true : parent.isObserved());
    }

    /**
     * Uses the camera frustum element to check the octant.
     * @param parentTransform
     * @param cam
     */
    private void computeObserved1(Transform parentTransform, ICamera cam) {
	// Is this octant observed??
	Frustum frustum = cam.getCamera().frustum;
	boxcopy.set(box);
	boxcopy.mul(boxtransf.idt().translate(parentTransform.getTranslation()));
	observed = frustum.boundsInFrustum(boxcopy.getCenter(auxD1).setVector3(auxF1), size.setVector3(auxF2));
    }

    /**
     * The octant is observed if at least one of its vertices is in the view or the
     * camera itself is in the view.
     * @param parentTransform
     * @param cam
     */
    private void computeObserved2(Transform parentTransform, ICamera cam) {
	float angle = cam.getAngleEdge();
	Vector3d dir = cam.getDirection();
	Vector3d up = cam.getUp();

	boxcopy.set(box);
	boxcopy.mul(boxtransf.idt().translate(parentTransform.getTranslation()));
	observed = GlobalResources.isInView(boxcopy.getCenter(auxD1), angle, dir) ||
		GlobalResources.isInView(boxcopy.getCorner000(auxD1), angle, dir) ||
		GlobalResources.isInView(boxcopy.getCorner001(auxD1), angle, dir) ||
		GlobalResources.isInView(boxcopy.getCorner010(auxD1), angle, dir) ||
		GlobalResources.isInView(boxcopy.getCorner011(auxD1), angle, dir) ||
		GlobalResources.isInView(boxcopy.getCorner100(auxD1), angle, dir) ||
		GlobalResources.isInView(boxcopy.getCorner101(auxD1), angle, dir) ||
		GlobalResources.isInView(boxcopy.getCorner110(auxD1), angle, dir) ||
		GlobalResources.isInView(boxcopy.getCorner111(auxD1), angle, dir) ||
		box.contains(cam.getPos());

	// Rays
	if (!observed) {
	    // Rays in direction-up plane (vertical plane)
	    auxD2.set(dir).crs(up);
	    ray.direction.set(auxD1.set(dir).rotate(auxD2, angle));
	    observed = observed || Intersectord.intersectRayBoundsFast(ray, boxcopy.getCenter(auxD3), boxcopy.getDimensions(auxD4));
	    ray.direction.set(auxD1.set(dir).rotate(auxD2, -angle));
	    observed = observed || Intersectord.intersectRayBoundsFast(ray, boxcopy.getCenter(auxD3), boxcopy.getDimensions(auxD4));

	    // Rays in direction-crs(direction,up) plane (horizontal plane)
	    ray.direction.set(auxD1.set(dir).rotate(up, angle));
	    observed = observed || Intersectord.intersectRayBoundsFast(ray, boxcopy.getCenter(auxD3), boxcopy.getDimensions(auxD4));
	    ray.direction.set(auxD1.set(dir).rotate(up, -angle));
	    observed = observed || Intersectord.intersectRayBoundsFast(ray, boxcopy.getCenter(auxD3), boxcopy.getDimensions(auxD4));
	}

    }

    public LoadStatus getStatus() {
	return status;
    }

    public void setStatus(LoadStatus status) {
	synchronized (status) {
	    this.status = status;
	}
    }

    /**
     * Sets the status to this node and its descendants recursively
     * to the given depth level.
     * @param status The new status.
     * @param depth The depth.
     */
    public void setStatus(LoadStatus status, int depth) {
	if (depth >= this.depth) {
	    setStatus(status);
	    for (int i = 0; i < 8; i++) {
		OctreeNode<T> child = children[i];
		if (child != null) {
		    child.setStatus(status, depth);
		}
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
	loc.set(this.blf).add(transform);

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
