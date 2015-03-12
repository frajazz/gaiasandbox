package gaia.cu9.ari.gaiaorbit.data.octreegen;

import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.math.BoundingBoxd;
import gaia.cu9.ari.gaiaorbit.util.math.Longref;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OctreeGenerator {

    /** Is the octree centred at the sun? **/
    private static final boolean SUN_CENTRE = false;
    /** Maximum distance in parsecs **/
    private static final double MAX_DISTANCE_CAP = 5e4;

    IAggregationAlgorithm<Star> aggregation;
    Longref pageid;

    public OctreeGenerator(Class<? extends IAggregationAlgorithm> clazz) {
	try {
	    this.aggregation = clazz.newInstance();
	} catch (InstantiationException | IllegalAccessException e) {
	    e.printStackTrace();
	}
	this.pageid = new Longref(0l);
    }

    public OctreeNode<Star> generateOctree(List<Star> catalog) {

	double maxdist = Double.MIN_VALUE;
	Iterator<Star> it = catalog.iterator();
	Star furthest = null;
	while (it.hasNext()) {
	    Star s = it.next();
	    double dist = s.pos.len();
	    if (dist * Constants.U_TO_PC > MAX_DISTANCE_CAP) {
		// Remove star
		it.remove();
	    } else if (dist > maxdist) {
		furthest = s;
		maxdist = dist;
	    }
	}

	OctreeNode<Star> root = null;
	if (SUN_CENTRE) {
	    /** THE CENTRE OF THE OCTREE IS THE SUN **/
	    double halfSize = Math.max(Math.max(furthest.pos.x, furthest.pos.y), furthest.pos.z);
	    root = new OctreeNode<Star>(nextPageId(), 0, 0, 0, halfSize, halfSize, halfSize, 0);
	} else {
	    /** THE CENTRE OF THE OCTREE MAY BE ANYWHERE **/
	    double volume = Double.MIN_VALUE;
	    Star other = null;
	    BoundingBoxd aux = new BoundingBoxd();
	    BoundingBoxd box = new BoundingBoxd();
	    // Lets try to maximize the volume
	    for (Star s : catalog) {
		aux.set(furthest.pos, s.pos);
		double vol = aux.getVolume();
		if (vol > volume) {
		    volume = vol;
		    other = s;
		    box.set(aux);
		}
	    }
	    double halfSize = Math.max(Math.max(box.getDepth(), box.getHeight()), box.getWidth()) / 2d;
	    root = new OctreeNode<Star>(nextPageId(), box.getCenterX(), box.getCenterY(), box.getCenterZ(), halfSize, halfSize, halfSize, 0);
	}

	treatOctant(root, catalog);
	root.updateNumbers();

	return root;
    }

    private void treatOctant(OctreeNode<Star> octant, List<Star> catalog) {
	boolean leaf = aggregation.sample(catalog, octant);

	if (!leaf) {
	    // Generate 8 children
	    double hsx = octant.size.x / 4d;
	    double hsy = octant.size.y / 4d;
	    double hsz = octant.size.z / 4d;

	    // Front - top - left
	    OctreeNode<Star> node000 = new OctreeNode<Star>(nextPageId(), octant.centre.x - hsx, octant.centre.y + hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1);
	    List<Star> l = intersect(catalog, node000.box);
	    if (!l.isEmpty()) {
		treatOctant(node000, l);
		octant.children[0] = node000;
	    }

	    // Front - top - right
	    OctreeNode<Star> node001 = new OctreeNode<Star>(nextPageId(), octant.centre.x + hsx, octant.centre.y + hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1);
	    l = intersect(catalog, node001.box);
	    if (!l.isEmpty()) {
		treatOctant(node001, l);
		octant.children[1] = node001;
	    }

	    // Front - bottom - left
	    OctreeNode<Star> node010 = new OctreeNode<Star>(nextPageId(), octant.centre.x - hsx, octant.centre.y - hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1);
	    l = intersect(catalog, node010.box);
	    if (!l.isEmpty()) {
		treatOctant(node010, l);
		octant.children[2] = node010;
	    }

	    // Front - bottom - right
	    OctreeNode<Star> node011 = new OctreeNode<Star>(nextPageId(), octant.centre.x + hsx, octant.centre.y - hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1);
	    l = intersect(catalog, node011.box);
	    if (!l.isEmpty()) {
		treatOctant(node011, l);
		octant.children[3] = node011;
	    }

	    // Back - top - left
	    OctreeNode<Star> node100 = new OctreeNode<Star>(nextPageId(), octant.centre.x - hsx, octant.centre.y + hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1);
	    l = intersect(catalog, node100.box);
	    if (!l.isEmpty()) {
		treatOctant(node100, l);
		octant.children[4] = node100;
	    }

	    // Back - top - right
	    OctreeNode<Star> node101 = new OctreeNode<Star>(nextPageId(), octant.centre.x + hsx, octant.centre.y + hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1);
	    l = intersect(catalog, node101.box);
	    if (!l.isEmpty()) {
		treatOctant(node101, l);
		octant.children[5] = node101;
	    }
	    // Back - bottom - left
	    OctreeNode<Star> node110 = new OctreeNode<Star>(nextPageId(), octant.centre.x - hsx, octant.centre.y - hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1);
	    l = intersect(catalog, node110.box);
	    if (!l.isEmpty()) {
		treatOctant(node110, l);
		octant.children[6] = node110;
	    }
	    // Back - bottom - right
	    OctreeNode<Star> node111 = new OctreeNode<Star>(nextPageId(), octant.centre.x + hsx, octant.centre.y - hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1);
	    l = intersect(catalog, node111.box);
	    if (!l.isEmpty()) {
		treatOctant(node111, l);
		octant.children[7] = node111;
	    }
	}
    }

    /**
     * Returns a new list with all the stars of the incoming list that are inside the box.
     * @param stars
     * @param box
     * @return
     */
    private List<Star> intersect(List<Star> stars, BoundingBoxd box) {
	List<Star> result = new ArrayList<Star>();
	for (Star star : stars) {
	    if (box.contains(star.pos)) {
		result.add(star);
	    }
	}
	return result;

    }

    private long nextPageId() {
	return pageid.num++;
    }
}
