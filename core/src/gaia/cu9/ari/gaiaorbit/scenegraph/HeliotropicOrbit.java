package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;

import java.util.Date;

/**
 * Heliotropic orbits must be corrected using the Sun longitude. They are by default
 * in equatorial coordinates.
 * @author Toni Sagrista
 *
 */
public class HeliotropicOrbit extends Orbit {
    double angle;

    public HeliotropicOrbit() {
        super();
    }

    /**
     * Update the local transform with the transform and the rotations/scales necessary.
     * Override if your model contains more than just the position and size.
     */
    protected void updateLocalTransform(Date date) {
        angle = AstroUtils.getSunLongitude(date);
        localTransformD.set(transform.getMatrix()).mul(Coordinates.equatorialToEcliptic()).rotate(0, 1, 0, angle + 180);
    }
}
