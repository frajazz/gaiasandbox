package gaia.cu9.ari.gaiaorbit.util.coord;

import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Orbit;
import gaia.cu9.ari.gaiaorbit.scenegraph.Orbit.OrbitalParameters;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.util.Date;

public class OrbitLintCoordinates implements IBodyCoordinates {
    Orbit orbit;
    OrbitalParameters orbitalParams;
    OrbitData data;
    Matrix4d transf;
    Vector3d aux = new Vector3d();

    @Override
    public void initialize(Object... params) {
	if (params.length == 0) {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, new RuntimeException("OrbitLintCoordinates need the orbit name"));
	} else {
	    transf = new Matrix4d();
	    SceneGraphNode sgn = (SceneGraphNode) params[0];
	    orbit = (Orbit) sgn;
	    orbitalParams = orbit.params;
	    data = orbit.orbitData;
	}
    }

    @Override
    public Vector3d getEclipticSphericalCoordinates(Date date, Vector3d out) {
	getEquatorialCartesianCoordinates(date, out);
	out.mul(Coordinates.eclipticToEquatorial());

	// To spherical
	Coordinates.cartesianToSpherical(out, out);
	return out;
    }

    @Override
    public Vector3d getEquatorialCartesianCoordinates(Date date, Vector3d out) {
	// Find out index

	// Number of periods occurred
	double numPeriods = (AstroUtils.getJulianDateCache(date) - orbitalParams.epoch) / orbitalParams.period;
	// Current angle in degrees
	double angle = (orbitalParams.meanAnomaly + (numPeriods - Math.floor(numPeriods)) * 360d) % 360d;
	// Fraction in [0..numPoints]
	double fraction = (angle / 360d) * data.getNumPoints();

	int basei = (int) Math.floor(fraction);
	int nexti = (basei + 1) % data.getNumPoints();
	double percent = fraction - basei;

	data.loadPoint(out, basei);
	data.loadPoint(aux, nexti);

	double len = aux.sub(out).len();
	aux.nor().scl(percent * len);
	out.add(aux);

	transf.set(((CelestialBody) orbit.parent).orientation);
	transf.rotate(0, 1, 0, orbitalParams.argOfPericenter);
	transf.rotate(0, 0, 1, orbitalParams.i);
	transf.rotate(0, 1, 0, orbitalParams.ascendingNode);

	out.mul(transf);
	return out;
    }

}