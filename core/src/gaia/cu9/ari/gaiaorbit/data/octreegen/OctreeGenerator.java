package gaia.cu9.ari.gaiaorbit.data.octreegen;

import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.math.BoundingBoxd;
import gaia.cu9.ari.gaiaorbit.util.math.Longref;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.badlogic.gdx.math.MathUtils;

public class OctreeGenerator {

    /** Is the octree centred at the sun? **/
    private static final boolean SUN_CENTRE = false;
    /** Maximum distance in parsecs **/
    private static final double MAX_DISTANCE_CAP = 3e4;

    IAggregationAlgorithm<Particle> aggregation;
    Longref pageid;

    public OctreeGenerator(IAggregationAlgorithm<Particle> aggregation) {
        this.aggregation = aggregation;
        this.pageid = new Longref(0l);
    }

    public OctreeNode<Particle> generateOctree(List<Particle> catalog) {

        double maxdist = Double.MIN_VALUE;
        Iterator<Particle> it = catalog.iterator();
        Particle furthest = null;
        while (it.hasNext()) {
            Particle s = it.next();
            double dist = s.pos.len();
            if (dist * Constants.U_TO_PC > MAX_DISTANCE_CAP) {
                // Remove star
                it.remove();
            } else if (dist > maxdist) {
                furthest = s;
                maxdist = dist;
            }
        }

        OctreeNode<Particle> root = null;
        if (SUN_CENTRE) {
            /** THE CENTRE OF THE OCTREE IS THE SUN **/
            double halfSize = Math.max(Math.max(furthest.pos.x, furthest.pos.y), furthest.pos.z);
            root = new OctreeNode<Particle>(nextPageId(), 0, 0, 0, halfSize, halfSize, halfSize, 0);
        } else {
            /** THE CENTRE OF THE OCTREE MAY BE ANYWHERE **/
            double volume = Double.MIN_VALUE;
            BoundingBoxd aux = new BoundingBoxd();
            BoundingBoxd box = new BoundingBoxd();
            // Lets try to maximize the volume
            for (Particle s : catalog) {
                aux.set(furthest.pos, s.pos);
                double vol = aux.getVolume();
                if (vol > volume) {
                    volume = vol;
                    box.set(aux);
                }
            }
            double halfSize = Math.max(Math.max(box.getDepth(), box.getHeight()), box.getWidth()) / 2d;
            root = new OctreeNode<Particle>(nextPageId(), box.getCenterX(), box.getCenterY(), box.getCenterZ(), halfSize, halfSize, halfSize, 0);
        }

        treatOctant(root, catalog, MathUtils.clamp((float) aggregation.getMaxPart() / (float) catalog.size(), 0f, 1f));
        root.updateNumbers();

        return root;
    }

    private void treatOctant(OctreeNode<Particle> octant, List<Particle> catalog, float percentage) {
        boolean leaf = aggregation.sample(catalog, octant, percentage);

        if (!leaf) {
            // Generate 8 children
            double hsx = octant.size.x / 4d;
            double hsy = octant.size.y / 4d;
            double hsz = octant.size.z / 4d;

            /** CREATE OCTANTS **/
            OctreeNode<Particle>[] nodes = new OctreeNode[8];
            // Front - top - left
            nodes[0] = new OctreeNode<Particle>(nextPageId(), octant.centre.x - hsx, octant.centre.y + hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1);
            // Front - top - right
            nodes[1] = new OctreeNode<Particle>(nextPageId(), octant.centre.x + hsx, octant.centre.y + hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1);
            // Front - bottom - left
            nodes[2] = new OctreeNode<Particle>(nextPageId(), octant.centre.x - hsx, octant.centre.y - hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1);
            // Front - bottom - right
            nodes[3] = new OctreeNode<Particle>(nextPageId(), octant.centre.x + hsx, octant.centre.y - hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1);
            // Back - top - left
            nodes[4] = new OctreeNode<Particle>(nextPageId(), octant.centre.x - hsx, octant.centre.y + hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1);
            // Back - top - right
            nodes[5] = new OctreeNode<Particle>(nextPageId(), octant.centre.x + hsx, octant.centre.y + hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1);
            // Back - bottom - left
            nodes[6] = new OctreeNode<Particle>(nextPageId(), octant.centre.x - hsx, octant.centre.y - hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1);
            // Back - bottom - right
            nodes[7] = new OctreeNode<Particle>(nextPageId(), octant.centre.x + hsx, octant.centre.y - hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1);

            /** INTERSECT CATALOG WITH OCTANTS **/
            int maxSublevelObjs = 0;
            List<Particle>[] lists = new List[8];
            for (int i = 0; i < 8; i++) {
                lists[i] = intersect(catalog, nodes[i]);
                if (lists[i].size() > maxSublevelObjs) {
                    maxSublevelObjs = lists[i].size();
                }
            }

            float sublevelPercentage = MathUtils.clamp((float) aggregation.getMaxPart() / (float) maxSublevelObjs, 0f, 1f);

            /** TREAT OCTANTS **/
            for (int i = 0; i < 8; i++) {
                if (!lists[i].isEmpty()) {
                    treatOctant(nodes[i], lists[i], sublevelPercentage);
                    octant.children[i] = nodes[i];
                    nodes[i].parent = octant;
                }
            }

        }
    }

    /**
     * Returns a new list with all the stars of the incoming list that are inside the box.
     * @param stars
     * @param box
     * @return
     */
    private List<Particle> intersect(List<Particle> stars, OctreeNode<Particle> box) {
        List<Particle> result = new ArrayList<Particle>();
        for (Particle star : stars) {
            if (box.box.contains(star.pos)) {
                result.add(star);
            }
        }
        return result;

    }

    private long nextPageId() {
        return pageid.num++;
    }
}
