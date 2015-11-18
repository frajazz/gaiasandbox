package gaia.cu9.ari.gaiaorbit.data.octreegen;

import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BrightestStars implements IAggregationAlgorithm<Particle> {
    private static final int MAX_DEPTH = 9;
    // Maximum number of objects in the densest node of this level
    private static final int MAX_PART = 80000;
    // Minimum number of objects under which we do not need to break the octree further
    private static final int MIN_PART = 40000;
    Comparator<Particle> comp;
    int starId;

    public BrightestStars() {
	comp = new StarBrightnessComparator();
	starId = (int) System.currentTimeMillis();
    }

    @Override
    public boolean sample(List<Particle> inputStars, OctreeNode<Particle> octant, float percentage) {
	// Calculate nObjects for this octant based on maxObjs and the MAX_PART
	int nInput = inputStars.size();
	int nObjects = Math.round(nInput * percentage);

	if (nInput < MIN_PART || octant.depth >= MAX_DEPTH) {
	    // Downright use all stars
	    octant.addAll(inputStars);
	    for (Particle s : inputStars) {
		s.page = octant;
		s.pageId = octant.pageId;
	    }
	    return true;
	} else {
	    // Extract sample
	    Collections.sort(inputStars, comp);
	    for (int i = 0; i < nObjects; i++) {
		Particle s = inputStars.get(i);
		Particle virtual = getVirtualCopy(s);
		virtual.type = 92;
		virtual.nparticles = inputStars.size() / nObjects;

		// Add virtual to octant
		octant.add(virtual);
		virtual.page = octant;
		virtual.pageId = octant.pageId;
	    }
	    return false;
	}

    }

    public class StarBrightnessComparator implements Comparator<Particle> {
	@Override
	public int compare(Particle o1, Particle o2) {
	    return Float.compare(o1.absmag, o2.absmag);
	}

    }

    private Particle getVirtualCopy(Particle s) {
	Particle copy = new Particle();
	copy.name = s.name;
	copy.absmag = s.absmag;
	copy.appmag = s.appmag;
	copy.cc = s.cc;
	copy.colorbv = s.colorbv;
	copy.ct = s.ct;
	copy.pos = new Vector3d(s.pos);
	copy.id = starId++;
	return copy;
    }

    public int getMaxPart() {
	return MAX_PART;
    }

}
