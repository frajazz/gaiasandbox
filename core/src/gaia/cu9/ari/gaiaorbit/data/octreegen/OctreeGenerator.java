package gaia.cu9.ari.gaiaorbit.data.octreegen;

import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.util.math.BoundingBoxd;
import gaia.cu9.ari.gaiaorbit.util.math.Longref;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.ArrayList;
import java.util.List;

public class OctreeGenerator {

    /** Is the octree centred at the sun? **/
    private static final boolean SUN_CENTRE = true;

    IAggregationAlgorithm<CelestialBody> aggregation;
    Longref pageid;

    public OctreeGenerator(Class<? extends IAggregationAlgorithm> clazz) {
	try {
	    this.aggregation = clazz.newInstance();
	} catch (InstantiationException | IllegalAccessException e) {
	    e.printStackTrace();
	}
	this.pageid = new Longref(0l);
    }

    public OctreeNode<CelestialBody> generateOctree(List<CelestialBody> catalog) {

	double maxdist = Double.MIN_VALUE;
	CelestialBody furthest = null;
	for (CelestialBody s : catalog) {
	    double dist = s.pos.len();
	    if (dist > maxdist) {
		furthest = s;
		maxdist = dist;
	    }
	}

	OctreeNode<CelestialBody> root = null;
	if (SUN_CENTRE) {
	    /** THE CENTRE OF THE OCTREE IS THE SUN **/
	    double halfSize = Math.max(Math.max(furthest.pos.x, furthest.pos.y), furthest.pos.z);
	    root = new OctreeNode<CelestialBody>(nextPageId(), 0, 0, 0, halfSize, halfSize, halfSize, 0);
	} else {
	    /** THE CENTRE OF THE OCTREE MAY BE ANYWHERE **/
	    double volume = Double.MIN_VALUE;
	    CelestialBody other = null;
	    BoundingBoxd aux = new BoundingBoxd();
	    BoundingBoxd box = new BoundingBoxd();
	    // Lets try to maximize the volume
	    for (CelestialBody s : catalog) {
		aux.set(furthest.pos, s.pos);
		double vol = aux.getVolume();
		if (vol > volume) {
		    volume = vol;
		    other = s;
		    box.set(aux);
		}
	    }
	    double halfSize = Math.max(Math.max(box.getDepth(), box.getHeight()), box.getWidth()) / 2d;
	    root = new OctreeNode<CelestialBody>(nextPageId(), box.getCenterX(), box.getCenterY(), box.getCenterZ(), halfSize, halfSize, halfSize, 0);
	}

	treatOctant(root, catalog);
	root.updateNumbers();

	return root;
    }

    private void treatOctant(OctreeNode<CelestialBody> octant, List<CelestialBody> catalog) {
	boolean leaf = aggregation.sample(catalog, octant);

	if (!leaf) {
	    // Generate 8 children
	    double hsx = octant.size.x / 4;
	    double hsy = octant.size.y / 4;
	    double hsz = octant.size.z / 4;

	    // Front - top - left
	    OctreeNode<CelestialBody> node000 = new OctreeNode<CelestialBody>(nextPageId(), octant.centre.x - hsx, octant.centre.y + hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1);
	    List<CelestialBody> l = intersect(catalog, node000.box);
	    if (!l.isEmpty()) {
		treatOctant(node000, l);
		octant.children[0] = node000;
	    }

	    // Front - top - right
	    OctreeNode<CelestialBody> node001 = new OctreeNode<CelestialBody>(nextPageId(), octant.centre.x + hsx, octant.centre.y + hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1);
	    l = intersect(catalog, node001.box);
	    if (!l.isEmpty()) {
		treatOctant(node001, l);
		octant.children[1] = node001;
	    }

	    // Front - bottom - left
	    OctreeNode<CelestialBody> node010 = new OctreeNode<CelestialBody>(nextPageId(), octant.centre.x - hsx, octant.centre.y - hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1);
	    l = intersect(catalog, node010.box);
	    if (!l.isEmpty()) {
		treatOctant(node010, l);
		octant.children[2] = node010;
	    }

	    // Front - bottom - right
	    OctreeNode<CelestialBody> node011 = new OctreeNode<CelestialBody>(nextPageId(), octant.centre.x + hsx, octant.centre.y - hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1);
	    l = intersect(catalog, node011.box);
	    if (!l.isEmpty()) {
		treatOctant(node011, l);
		octant.children[3] = node011;
	    }

	    // Back - top - left
	    OctreeNode<CelestialBody> node100 = new OctreeNode<CelestialBody>(nextPageId(), octant.centre.x - hsx, octant.centre.y + hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1);
	    l = intersect(catalog, node100.box);
	    if (!l.isEmpty()) {
		treatOctant(node100, l);
		octant.children[4] = node100;
	    }

	    // Back - top - right
	    OctreeNode<CelestialBody> node101 = new OctreeNode<CelestialBody>(nextPageId(), octant.centre.x + hsx, octant.centre.y + hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1);
	    l = intersect(catalog, node101.box);
	    if (!l.isEmpty()) {
		treatOctant(node101, l);
		octant.children[5] = node101;
	    }
	    // Back - bottom - left
	    OctreeNode<CelestialBody> node110 = new OctreeNode<CelestialBody>(nextPageId(), octant.centre.x - hsx, octant.centre.y - hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1);
	    l = intersect(catalog, node110.box);
	    if (!l.isEmpty()) {
		treatOctant(node110, l);
		octant.children[6] = node110;
	    }
	    // Back - bottom - right
	    OctreeNode<CelestialBody> node111 = new OctreeNode<CelestialBody>(nextPageId(), octant.centre.x + hsx, octant.centre.y - hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1);
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
    private List<CelestialBody> intersect(List<CelestialBody> stars, BoundingBoxd box) {
	List<CelestialBody> result = new ArrayList<CelestialBody>();
	for (CelestialBody star : stars) {
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
