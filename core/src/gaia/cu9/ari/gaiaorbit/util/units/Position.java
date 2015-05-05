package gaia.cu9.ari.gaiaorbit.util.units;

import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.units.Quantity.Angle;
import gaia.cu9.ari.gaiaorbit.util.units.Quantity.Angle.AngleUnit;
import gaia.cu9.ari.gaiaorbit.util.units.Quantity.Length;
import gaia.cu9.ari.gaiaorbit.util.units.Quantity.Length.LengthUnit;

/**
 * Helper class that transforms various positional information into the internal position of the application.
 * @author Toni Sagrista
 *
 */
public class Position {

    public enum PositionType {
        RA_DEC_DIST,
        RA_DEC_PLX,
        GLON_GLAT_DIST,
        GLON_GLAT_PLX,
        XYZ_EQUATORIAL,
        XYZ_GALACTIC
    }

    public final Vector3d gsposition;

    /**
     * Works out the cartesian equatorial position in the Gaia Sandbox reference system. The units of the result are parsecs.
     * @param a
     * @param unitA
     * @param b
     * @param unitB
     * @param c
     * @param unitC
     * @param type
     */
    public Position(double a, String unitA, double b, String unitB, double c, String unitC, PositionType type) {

        gsposition = new Vector3d();

        switch (type) {
        case GLON_GLAT_DIST:
            Angle lon = new Angle(a, unitA);
            Angle lat = new Angle(b, unitB);
            Length dist = new Length(c, unitC);

            Coordinates.sphericalToCartesian(lon.get(AngleUnit.RAD), lat.get(AngleUnit.RAD), dist.get(LengthUnit.PC), gsposition);
            gsposition.mul(Coordinates.galacticToEquatorial());

            break;
        case GLON_GLAT_PLX:

            lon = new Angle(a, unitA);
            lat = new Angle(b, unitB);
            dist = new Angle(c, unitC).getParallaxDistance();

            Coordinates.sphericalToCartesian(lon.get(AngleUnit.RAD), lat.get(AngleUnit.RAD), dist.get(LengthUnit.PC), gsposition);
            gsposition.mul(Coordinates.galacticToEquatorial());

            break;
        case RA_DEC_DIST:

            lon = new Angle(a, unitA);
            lat = new Angle(b, unitB);
            dist = new Length(c, unitC);

            Coordinates.sphericalToCartesian(lon.get(AngleUnit.RAD), lat.get(AngleUnit.RAD), dist.get(LengthUnit.PC), gsposition);

            break;
        case RA_DEC_PLX:

            lon = new Angle(a, unitA);
            lat = new Angle(b, unitB);
            dist = new Angle(c, unitC).getParallaxDistance();

            Coordinates.sphericalToCartesian(lon.get(AngleUnit.RAD), lat.get(AngleUnit.RAD), dist.get(LengthUnit.PC), gsposition);

            break;
        case XYZ_EQUATORIAL:

            Length x = new Length(a, unitA);
            Length y = new Length(b, unitB);
            Length z = new Length(c, unitC);

            gsposition.set(x.get(LengthUnit.PC), y.get(LengthUnit.PC), z.get(LengthUnit.PC));

            break;
        case XYZ_GALACTIC:

            x = new Length(a, unitA);
            y = new Length(b, unitB);
            z = new Length(c, unitC);

            gsposition.set(x.get(LengthUnit.PC), y.get(LengthUnit.PC), z.get(LengthUnit.PC));
            gsposition.mul(Coordinates.galacticToEquatorial());

            break;
        default:
            break;
        }

    }

    private void swapCoordinates() {
        // Switch axes
        double aux = gsposition.x;
        gsposition.x = gsposition.y;
        gsposition.y = gsposition.z;
        gsposition.x = aux;
    }
}
