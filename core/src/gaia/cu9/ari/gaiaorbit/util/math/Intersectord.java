package gaia.cu9.ari.gaiaorbit.util.math;

import com.badlogic.gdx.math.collision.Ray;

public class Intersectord {

    /** Quick check whether the given {@link Ray} and {@link BoundingBoxd} intersect.
     * 
     * @param ray The ray
     * @param center The center of the bounding box
     * @param dimensions The dimensions (width, height and depth) of the bounding box
     * @return Whether the ray and the bounding box intersect. */
    static public boolean intersectRayBoundsFast(Rayd ray, Vector3d center, Vector3d dimensions) {
        final double divX = 1f / ray.direction.x;
        final double divY = 1f / ray.direction.y;
        final double divZ = 1f / ray.direction.z;

        double minx = ((center.x - dimensions.x * .5f) - ray.origin.x) * divX;
        double maxx = ((center.x + dimensions.x * .5f) - ray.origin.x) * divX;
        if (minx > maxx) {
            final double t = minx;
            minx = maxx;
            maxx = t;
        }

        double miny = ((center.y - dimensions.y * .5f) - ray.origin.y) * divY;
        double maxy = ((center.y + dimensions.y * .5f) - ray.origin.y) * divY;
        if (miny > maxy) {
            final double t = miny;
            miny = maxy;
            maxy = t;
        }

        double minz = ((center.z - dimensions.z * .5f) - ray.origin.z) * divZ;
        double maxz = ((center.z + dimensions.z * .5f) - ray.origin.z) * divZ;
        if (minz > maxz) {
            final double t = minz;
            minz = maxz;
            maxz = t;
        }

        double min = Math.max(Math.max(minx, miny), minz);
        double max = Math.min(Math.min(maxx, maxy), maxz);

        return max >= 0 && max >= min;
    }

}
