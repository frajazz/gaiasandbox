package gaia.cu9.ari.gaiaorbit.data.octreegen;

import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BrightestStars implements IAggregationAlgorithm<Particle> {
    private static final int MAX_DEPTH = 13;
    // Maximum number of objects in the densest node of this level
    private static final int MAX_PART = 10000;
    // Minimum number of objects under which we do not need to break the octree further
    private static final int MIN_PART = 1000;
    Comparator<Particle> comp;
    int starId;

    int discarded = 0;

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
            if (nInput < MIN_PART) {
                // Downright use all stars that have not been assigned
                for (Particle s : inputStars) {
                    if (s.page == null) {
                        octant.add(s);
                        s.page = octant;
                        s.pageId = octant.pageId;
                    }
                }
            } else {
                // Select sample, discard the rest
                Collections.sort(inputStars, comp);
                for (int i = 0; i < nObjects; i++) {
                    Particle s = inputStars.get(i);
                    if (s.page == null) {
                        // Add star
                        octant.add(s);
                        s.page = octant;
                        s.pageId = octant.pageId;
                        s.type = 92;
                        s.nparticles = inputStars.size() / nObjects;
                    }
                }

                discarded += nInput - nObjects;
            }
            return true;
        } else {
            // Extract sample
            Collections.sort(inputStars, comp);
            for (int i = 0; i < nObjects; i++) {
                Particle s = inputStars.get(i);
                if (s.page == null) {
                    // Add star
                    octant.add(s);
                    s.page = octant;
                    s.pageId = octant.pageId;
                    s.type = 92;
                    s.nparticles = inputStars.size() / nObjects;
                }
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

    public int getDiscarded() {
        return discarded;
    }

}
