package gaia.cu9.ari.gaiaorbit.util.coord;

import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.scenegraph.HeliotropicOrbit;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.util.Date;

public class GaiaCoordinates implements IBodyCoordinates {
    HeliotropicOrbit orbit;
    OrbitData data;

    @Override
    public void doneLoading(Object... params) {
        orbit = (HeliotropicOrbit) ((ISceneGraph) params[0]).getNode("Gaia orbit");
        data = orbit.orbitData;
    }

    @Override
    public Vector3d getEclipticSphericalCoordinates(Date date, Vector3d out) {
        return null;
    }

    @Override
    public Vector3d getEquatorialCartesianCoordinates(Date date, Vector3d out) {
        boolean inRange = data.loadPoint(out, date);
        // Rotate by solar longitude, and convert to equatorial.
        out.rotate(AstroUtils.getSunLongitude(date) + 180, 0, 1, 0).mul(Coordinates.equatorialToEcliptic());
        return inRange ? out : null;
    }

}
