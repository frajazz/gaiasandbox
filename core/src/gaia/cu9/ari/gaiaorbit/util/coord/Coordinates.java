package gaia.cu9.ari.gaiaorbit.util.coord;

import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector2d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import com.badlogic.gdx.math.Matrix4;

/**
 * Provides utility coordinate conversions between some astronomical coordinate systems and to 
 * Cartesian coordinates. All angles are in radians.
 * @author Toni Sagrista
 *
 */
public class Coordinates {

    /** Parsecs to Km factor **/
    public static final double PC_TO_KM = 3.08567758e13d;
    /** Obliquity for low precision calculations in degrees and radians. J2000 with T=0 **/
    public static final double OBLIQUITY_DEG_J2000 = 23.4392808;
    public static final double OBLIQUITY_RAD_J2000 = Math.toRadians(OBLIQUITY_DEG_J2000);
    /** Obliquity of ecliptic in J2000 in arcsec **/
    public static final double OBLIQUITY_ARCSEC_J2000 = 84381.41100;

    /** Some constants **/
    /** Node line galactic longitude **/
    private static final float nodes_l = -33f;
    /** Inclination of galactic equator to celestial equator **/
    private static final float incl_gal_cel = 62.9f;
    /** Node line equatorial right ascension **/
    private static final float nodes_ra = 282.25f;

    /** Moon inclination with respect to the ecliptic **/
    private static final float moonincl = 5.1333f;

    private static Matrix4d equatorialToEcliptic, eclipticToEquatorial, equatorialToGalactic, galacticToEquatorial, eclipticToGalactic, galacticToEcliptic;
    private static Matrix4 equatorialToEclipticF, eclipticToEquatorialF, equatorialToGalacticF, galacticToEquatorialF, eclipticToGalacticF, galacticToEclipticF;
    static {
        // Initialize matrices

        // EQ -> ECL
        equatorialToEcliptic = getRotationMatrix(0, OBLIQUITY_DEG_J2000, 0);
        equatorialToEclipticF = new Matrix4(equatorialToEcliptic.valuesf());

        // ECL -> EQ
        eclipticToEquatorial = getRotationMatrix(0, -OBLIQUITY_DEG_J2000, 0);
        eclipticToEquatorialF = new Matrix4(equatorialToEcliptic.valuesf());

        // EQ -> GAL
        equatorialToGalactic = getRotationMatrix(nodes_l, incl_gal_cel, nodes_ra);
        equatorialToGalacticF = new Matrix4(equatorialToGalactic.valuesf());

        // GAL -> EQ
        galacticToEquatorial = getRotationMatrix(-nodes_ra, -incl_gal_cel, -nodes_l);
        galacticToEquatorialF = new Matrix4(galacticToEquatorial.valuesf());

        // ECL -> GAL
        eclipticToGalactic = new Matrix4d(eclipticToEquatorial).mul(equatorialToGalactic);
        eclipticToGalacticF = new Matrix4(eclipticToGalactic.valuesf());

        // GAL -> ECL
        galacticToEcliptic = new Matrix4d(galacticToEquatorial).mul(equatorialToEcliptic);
        galacticToEclipticF = new Matrix4(galacticToEcliptic.valuesf());

    }

    /**
     * Gets the rotation matrix to apply for the given Euler angles &alpha;, &beta; and &gamma;. It applies Ry(&gamma;)*Rz(&beta;)*Ry(&alpha;), so that
     * it rotates the fixed xyz system to make it coincide with the XYZ, where &alpha; is the angle between the axis z and the line of nodes N, &beta;
     * is the angle between the y axis and the Y axis, and &gamma; is the angle between the Z axis and the line of nodes N.<br/>
     * The assumed reference system is as follows:
     * <ul><li>
     * ZX is the fundamental plane.
     * </li><li>
     * Z points to the origin of the reference plane (the line of nodes N).
     * </li><li>
     * Y points upwards.
     * </li></ul>
     * @param alpha The &alpha; angle in degrees, between z and N.
     * @param beta The &beta; angle in degrees, between y and Y.
     * @param gamma The &gamma; angle in degrees, Z and N.
     * @return The rotation matrix.
     */
    public static Matrix4d getRotationMatrix(double alpha, double beta, double gamma) {
        Matrix4d m = new Matrix4d().rotate(0, 1, 0, gamma).rotate(0, 0, 1, beta).rotate(0, 1, 0, alpha);
        return m;
    }

    /**
     * Gets the rotation matrix to transform equatorial to moon orbit plane coordinates. Since the zero point
     * in both systems is the same (the vernal equinox, &gamma;, defined as the intersection between the equator and the ecliptic),
     * &alpha; and &gamma; are zero. &beta;, the angle between the up directions of both systems, is precisely the obliquity of the
     * ecliptic, &epsilon;. So we have the Euler angles &alpha;=0&deg;, &beta;=&epsilon;;, &gamma;=0&deg;.
     * @return The matrix to transform from the equatorial plane to the Moon orbit.
     * @return
     */
    public static Matrix4d equatorialToLunarOrbit() {
        return getRotationMatrix(0, OBLIQUITY_DEG_J2000 + moonincl, 0);
    }

    /**
     * Gets the rotation matrix to transform equatorial to the ecliptic coordinates. Since the zero point
     * in both systems is the same (the vernal equinox, &gamma;, defined as the intersection between the equator and the ecliptic),
     * &alpha; and &gamma; are zero. &beta;, the angle between the up directions of both systems, is precisely the obliquity of the
     * ecliptic, &epsilon;. So we have the Euler angles &alpha;=0&deg;, &beta;=&epsilon;;, &gamma;=0&deg;.
     * @return The matrix to transform from equatorial coordinates to ecliptic coordinates.
     */
    public static Matrix4d equatorialToEcliptic() {
        //return getRotationMatrix(0, obliquity, 0);
        return equatorialToEcliptic;
    }

    public static Matrix4 equatorialToEclipticF() {
        //return getRotationMatrix(0, obliquity, 0);
        return equatorialToEclipticF;
    }

    /**
     * Gets the rotation matrix to transform equatorial to the ecliptic coordinates. Since the zero point
     * in both systems is the same (the vernal equinox, &gamma;, defined as the intersection between the equator and the ecliptic),
     * &alpha; and &gamma; are zero. &beta;, the angle between the up directions of both systems, is precisely the obliquity of the
     * ecliptic, &epsilon;. So we have the Euler angles &alpha;=0&deg;, &beta;=&epsilon;;, &gamma;=0&deg;.
     * @return The matrix to transform from equatorial coordinates to ecliptic coordinates.
     */
    public static Matrix4d equatorialToEcliptic(double julianDate) {
        return getRotationMatrix(0, AstroUtils.obliquity(julianDate), 0);
    }

    /**
     * Gets the rotation matrix to transform from the ecliptic system to the equatorial system. See {@link Coordinates#equatorialToEcliptic()} for
     * more information, for this is the inverse transformation.
     * @return The transformation matrix.
     */
    public static Matrix4d eclipticToEquatorial() {
        //return getRotationMatrix(0, -obliquity, 0);
        return eclipticToEquatorial;
    }

    public static Matrix4 eclipticToEquatorialF() {
        //return getRotationMatrix(0, -obliquity, 0);
        return eclipticToEquatorialF;
    }

    /**
     * Gets the rotation matrix to transform from the ecliptic system to the equatorial system. See {@link Coordinates#equatorialToEcliptic()} for
     * more information, for this is the inverse transformation.
     * @return The transformation matrix.
     */
    public static Matrix4d eclipticToEquatorial(double julianDate) {
        return getRotationMatrix(0, -AstroUtils.obliquity(julianDate), 0);
    }

    /**
     * Gets the rotation matrix to transform equatorial to galactic coordinates. The inclination of the galactic
     * equator to the celestial equator is 62.9&deg;. The intersection, or node line, of the two equators is at RA=282.25&deg; DEC=0&deg; and
     * l=33&deg; b=0&deg;. So we have the Euler angles &alpha;=-33&deg;, &beta;=62.9&deg;, &gamma;=282.25&deg;.
     * @return The transformation matrix.
     */
    public static Matrix4d equatorialToGalactic() {
        //return getRotationMatrix(-33f, 62.9f, 282.25f);
        return equatorialToGalactic;
    }

    public static Matrix4 equatorialToGalacticF() {
        //return getRotationMatrix(-33f, 62.9f, 282.25f);
        return equatorialToGalacticF;
    }

    /**
     * Gets the rotation matrix to transform from the galactic system to the equatorial system. See {@link Coordinates#equatorialToGalactic()} for
     * more information, since this is the inverse transformation. Use this matrix if you need to convert
     * equatorial cartesian coordinates to galactic cartesian coordinates.
     * @return The transformation matrix.
     */
    public static Matrix4d galacticToEquatorial() {
        //	return getRotationMatrix(-282.25f, -62.9f, 33f);
        return galacticToEquatorial;
    }

    public static Matrix4 galacticToEquatorialF() {
        //	return getRotationMatrix(-282.25f, -62.9f, 33f);
        return galacticToEquatorialF;
    }

    /**
     * Transforms from equatorial to ecliptic coordinates
     * @param vec Vector with ra (&alpha;) and dec (&delta;) in radians.
     * @param out The output vector.
     * @return The output vector with ecliptic longitude (&lambda;) and ecliptic latitude (&beta;) in radians, for chaining.
     */
    public static Vector2d equatorialToEcliptic(Vector2d vec, Vector2d out) {
        return equatorialToEcliptic(vec.x, vec.y, out);
    }

    /**
     * Transforms from equatorial to ecliptic coordinates
     * @param alpha Right ascension (&alpha;) in radians.
     * @param delta Declination (&delta;) in radians.
     * @param out The output vector.
     * @return The output vector with ecliptic longitude (&lambda;) and ecliptic latitude (&beta;) in radians, for chaining.
     */
    public static Vector2d equatorialToEcliptic(double alpha, double delta, Vector2d out) {
        double lambda = Math.atan2((Math.sin(alpha) * Math.cos(OBLIQUITY_RAD_J2000) + Math.tan(delta) * Math.sin(OBLIQUITY_RAD_J2000)), Math.cos(alpha));
        if (lambda < 0) {
            lambda += Math.PI * 2;
        }
        double beta = Math.asin(Math.sin(delta) * Math.cos(OBLIQUITY_RAD_J2000) - Math.cos(delta) * Math.sin(OBLIQUITY_RAD_J2000) * Math.sin(alpha));

        return out.set(lambda, beta);
    }

    /**
     * Transforms from equatorial to ecliptic coordinates
     * @param vec Vector with ra (&alpha;) and dec (&delta;) in radians.
     * @return Vector with ecliptic longitude (&lambda;) and ecliptic latitude (&beta;) in radians.
     */
    public static Vector2d equatorialToEcliptic(Vector2d vec, double julianDate) {
        double alpha = vec.x;
        double delta = vec.y;
        double obliquity_rad = Math.toRadians(julianDate);

        double lambda = Math.atan2((Math.sin(alpha) * Math.cos(obliquity_rad) + Math.tan(delta) * Math.sin(obliquity_rad)), Math.cos(alpha));
        if (lambda < 0) {
            lambda += Math.PI * 2;
        }
        double beta = Math.asin(Math.sin(delta) * Math.cos(obliquity_rad) - Math.cos(delta) * Math.sin(obliquity_rad) * Math.sin(alpha));

        return new Vector2d((float) lambda, (float) beta);
    }

    /**
     * Transforms from ecliptic to equatorial coordinates
     * @param vec Vector with ecliptic longitude (&lambda;) and ecliptic latitude (&beta;) in radians.
     * @param out The output vector.
     * @return The output vector with ra (&alpha;) and dec (&delta;) in radians, for chaining.
     */
    public static Vector2d eclipticToEquatorial(Vector2d vec, Vector2d out) {
        return eclipticToEquatorial(vec.x, vec.y, out);
    }

    /**
     * Transforms from ecliptic to equatorial coordinates
     * @param lambda Ecliptic longitude (&lambda;) in radians.
     * @param beta Ecliptic latitude (&beta;) in radians.
     * @param out The output vector.
     * @return The output vector with ra (&alpha;) and dec (&delta;) in radians, for chaining.
     */
    public static Vector2d eclipticToEquatorial(double lambda, double beta, Vector2d out) {

        double alpha = Math.atan2((Math.sin(lambda) * Math.cos(OBLIQUITY_RAD_J2000) - Math.tan(beta) * Math.sin(OBLIQUITY_RAD_J2000)), Math.cos(lambda));
        if (alpha < 0) {
            alpha += Math.PI * 2;
        }
        double delta = Math.asin(Math.sin(beta) * Math.cos(OBLIQUITY_RAD_J2000) + Math.cos(beta) * Math.sin(OBLIQUITY_RAD_J2000) * Math.sin(lambda));

        return out.set(alpha, delta);
    }

    /**
     * Transforms from ecliptic to equatorial coordinates
     * @param vec Vector with ecliptic longitude (&lambda;) and ecliptic latitude (&beta;) in radians.
     * @return Vector with ra (&alpha;) and dec (&delta;) in radians.
     */
    public static Vector2d eclipticToEquatorial(Vector2d vec, double julianDate) {
        double lambda = vec.x;
        double beta = vec.y;
        double obliquity_rad = Math.toRadians(AstroUtils.obliquity(julianDate));

        double alpha = Math.atan2((Math.sin(lambda) * Math.cos(obliquity_rad) - Math.tan(beta) * Math.sin(obliquity_rad)), Math.cos(lambda));
        if (alpha < 0) {
            alpha += Math.PI * 2;
        }
        double delta = Math.asin(Math.sin(beta) * Math.cos(obliquity_rad) + Math.cos(beta) * Math.sin(obliquity_rad) * Math.sin(lambda));

        return new Vector2d((float) alpha, (float) delta);
    }

    public static Matrix4d eclipticToGalactic() {
        return eclipticToGalactic;
    }

    public static Matrix4 eclipticToGalacticF() {
        return eclipticToGalacticF;
    }

    public static Matrix4d galacticToEcliptic() {
        return galacticToEcliptic;
    }

    public static Matrix4 galacticToEclipticF() {
        return galacticToEclipticF;
    }

    /**
     * Transforms from ecliptic to galactic coordinates
     * @param vec Vector with ecliptic longitude (&lambda;) and ecliptic latitude (&beta;) in radians.
     * @return Vector with galactic longitude (l) and galactic latitude (b) in radians.
     */
    public static Vector2d eclipticToGalactic(Vector2d vec) {
        return new Vector2d();
    }

    /**
     * Transforms from galactic to ecliptic coordinates
     * @param vec Vector with galactic longitude (l) and galactic latitude (b) in radians.
     * @return Vector with ecliptic longitude (&lambda;) and ecliptic latitude (&beta;) in radians.
     */
    public static Vector2d galacticToEcliptic(Vector2d vec) {
        return new Vector2d();
    }

    /**
     * Transforms from equatorial to galactic coordinates. TODO!
     * @param vec Vector with ra (&alpha;) and dec (&delta;) in radians.
     * @return Vector with galactic longitude (l) and galactic latitude (b) in radians.
     */
    public static Vector2d equatorialToGalactic(Vector2d vec) {

        return new Vector2d();
    }

    /**
     * Transforms from galactic to equatorial coordinates
     * @param vec Vector with galactic longitude (l) and galactic latitude (b) in radians.
     * @return Vector with ra (&alpha;) and dec (&delta;) in radians.
     */
    public static Vector2d galacticToEquatorial(Vector2d vec) {
        return new Vector2d();
    }

    /**
     * Converts from spherical to Cartesian coordinates, given a longitude (&alpha;), a latitude (&delta;) and the radius. The result is in the XYZ space, where ZX is the fundamental plane, with Z pointing to the
     * the origin of coordinates (equinox) and Y pointing to the north pole.
     * @param vec Vector containing the spherical coordinates. <ol><li>The longitude or right ascension (&alpha;), from the Z direction to the X direction, in radians.</li><li>The latitude or declination (&delta;), in radians.</li><li>The radius or distance to the point.</li></ol>
     * @param out The output vector.
     * @return Output vector in Cartesian coordinates where x and z are on the horizontal plane and y is in the up direction.
     */
    public static Vector3d sphericalToCartesian(Vector3d vec, Vector3d out) {
        return sphericalToCartesian(vec.x, vec.y, vec.z, out);
    }

    /**
     * Converts from spherical to Cartesian coordinates, given a longitude (&alpha;), a latitude (&delta;) and the radius.
     * @param longitude The longitude or right ascension angle, from the z direction to the x direction, in radians.
     * @param latitude The latitude or declination, in radians.
     * @param radius The radius or distance to the point.
     * @param out The output vector.
     * @return Output vector with the Cartesian coordinates[x, y, z] where x and z are on the horizontal plane and y is in the up direction, for chaining.
     */
    public static Vector3d sphericalToCartesian(double longitude, double latitude, double radius, Vector3d out) {
        out.x = radius * Math.cos(latitude) * Math.sin(longitude);
        out.y = radius * Math.sin(latitude);
        out.z = radius * Math.cos(latitude) * Math.cos(longitude);
        return out;
    }

    /**
     * Converts from Cartesian coordinates to spherical coordinates.
     * @param vec Vector with the Cartesian coordinates[x, y, z] where x and z are on the horizontal plane and y is in the up direction.
     * @param out Output vector.
     * @return Output vector containing the spherical coordinates. <ol><li>The longitude or right ascension (&alpha;), from the z direction to the x direction.</li><li>The latitude or declination (&delta;).</li><li>The radius or distance to the point.</li></ol> 
     */
    public static Vector3d cartesianToSpherical(Vector3d vec, Vector3d out) {
        /**
         * 
         *     x, y, z = values[:]
                xsq = x ** 2
                ysq = y ** 2
                zsq = z ** 2
                distance = math.sqrt(xsq + ysq + zsq)
            
                alpha = math.atan2(y, x)
                # Correct the value of alpha depending upon the quadrant.
                if alpha < 0:
                    alpha += 2 * math.pi
            
                if (xsq + ysq) == 0:
                    # In the case of the poles, delta is -90 or +90
                    delta = math.copysign(math.pi / 2, z)
                else:
                    delta = math.atan(z / math.sqrt(xsq + ysq))
         */
        double xsq = vec.x * vec.x;
        double ysq = vec.y * vec.y;
        double zsq = vec.z * vec.z;
        double distance = (float) Math.sqrt(xsq + ysq + zsq);

        double alpha = Math.atan2(vec.x, vec.z);
        if (alpha < 0) {
            alpha += 2 * Math.PI;
        }

        double delta = 0;
        if (zsq + xsq == 0) {
            delta = (vec.y > 0 ? Math.PI / 2 : -Math.PI / 2);
        } else {
            delta = Math.tan(vec.y / Math.sqrt(zsq + xsq));
        }

        out.x = alpha;
        out.y = delta;
        out.z = distance;
        return out;
    }

}
