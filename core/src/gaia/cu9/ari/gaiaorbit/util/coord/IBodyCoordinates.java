package gaia.cu9.ari.gaiaorbit.util.coord;

import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.util.Date;

/**
 * Defines the interface to get the coordinates of a body
 * @author Toni Sagrista
 *
 */
public interface IBodyCoordinates {

    /**
     * Initializes the coordinates object
     * @param params
     */
    public void doneLoading(Object... params);

    /**
     * Returns the ecliptic coordinates of the body in the out vector for the given date.
     * @param date The date.
     * @param out The out vector with the ecliptic coordinates in internal units.
     * @return The out vector for chaining.
     */
    public Vector3d getEclipticSphericalCoordinates(Date date, Vector3d out);

    /**
     * Gets equatorial cartesian coordinates for the given date.
     * @param date The date.
     * @param out The out vector where the equatorial cartesian coordinates will be.
     * @return The out vector for chaining, or null if the date is out of range, in case of non elliptical orbits such as Gaia.
     */
    public Vector3d getEquatorialCartesianCoordinates(Date date, Vector3d out);

}
