package gaia.cu9.ari.gaiaorbit.data.octreegen;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;

import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

public class BrightestStars implements IAggregationAlgorithm<Particle> {
    private static final int MAX_DEPTH = 20;
    // Maximum number of objects in the densest node of this level
    private static final int MAX_PART = 2000;
    // Minimum number of objects under which we do not need to break the octree further
    private static final int MIN_PART = 200;

    // Whether to discard stars due to density or not
    private static final boolean DISCARD = false;

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
        int nObjects = MathUtils.clamp(Math.round(nInput * percentage), 1, Integer.MAX_VALUE);

        if (nInput <= MIN_PART || octant.depth >= MAX_DEPTH) {
            if (!DISCARD) {
                // Never discard any
                for (Particle s : inputStars) {
                    if (s.octant == null) {
                        octant.add(s);
                        s.octant = octant;
                        s.octantId = octant.pageId;
                    }
                }
            } else if (DISCARD) {
                if (nInput <= MIN_PART) {
                    // Downright use all stars that have not been assigned
                    for (Particle s : inputStars) {
                        if (s.octant == null) {
                            octant.add(s);
                            s.octant = octant;
                            s.octantId = octant.pageId;
                        }
                    }
                } else {
                    // Select sample, discard the rest
                    Collections.sort(inputStars, comp);
                    for (int i = 0; i < nObjects; i++) {
                        Particle s = inputStars.get(i);
                        if (s.octant == null) {
                            // Add star
                            octant.add(s);
                            s.octant = octant;
                            s.octantId = octant.pageId;
                            s.nparticles = inputStars.size() / nObjects;
                        }
                    }

                    discarded += nInput - nObjects;
                }
            }
            return true;
        } else {
            // Extract sample
            Collections.sort(inputStars, comp);
            int added = 0;
            int i = 0;
            while (added < nObjects && i < inputStars.size()) {
                Particle s = inputStars.get(i);
                if (s.octant == null) {
                    // Add star
                    octant.add(s);
                    s.octant = octant;
                    s.octantId = octant.pageId;
                    s.nparticles = inputStars.size() / nObjects;
                    added++;
                }
                i++;
            }
            // It is leaf if we didn't add any star
            return added == 0;
        }

    }

    public class StarBrightnessComparator implements Comparator<Particle> {
        @Override
        public int compare(Particle o1, Particle o2) {
            return Float.compare(o1.absmag, o2.absmag);
        }

    }

    public int getMaxPart() {
        return MAX_PART;
    }

    public int getDiscarded() {
        return discarded;
    }

}
