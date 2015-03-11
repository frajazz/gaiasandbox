package gaia.cu9.ari.gaiaorbit.data.octreegen;

import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BrightestStars implements IAggregationAlgorithm<Star> {
    private static final int MAX_DEPTH = 10;
    private static final int MAX_PART = 16000;
    Comparator<Star> comp;
    long starId;

    public BrightestStars() {
	comp = new StarBrightnessComparator();
	starId = System.currentTimeMillis();
    }

    @Override
    public boolean sample(List<Star> inputStars, OctreeNode<Star> octant) {
	if (inputStars.size() < MAX_PART || octant.depth >= MAX_DEPTH) {
	    // Downright use all stars
	    octant.addAll(inputStars);
	    return true;
	} else {
	    // Extract sample
	    Collections.sort(inputStars, comp);
	    for (int i = 0; i < MAX_PART; i++) {
		Star s = inputStars.get(i);
		Star virtual = getVirtualCopy(s);
		virtual.type = 92;
		virtual.nparticles = inputStars.size() / MAX_PART;
		octant.add(virtual);
		virtual.page = (OctreeNode) octant;
		virtual.pageId = octant.pageId;
	    }
	    return false;
	}

    }

    public class StarBrightnessComparator implements Comparator<Star> {
	@Override
	public int compare(Star o1, Star o2) {
	    return Float.compare(o1.absmag, o2.absmag);
	}

    }

    private Star getVirtualCopy(Star s) {
	Star copy = new Star();
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

}
