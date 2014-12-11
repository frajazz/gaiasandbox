package gaia.cu9.ari.gaiaorbit.util.coord.vsop87;

import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.coord.IBodyCoordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.util.Date;

public abstract class AbstractVSOP87 implements iVSOP87, IBodyCoordinates {

    @Override
    public void initialize(Object... params) {
    }

    @Override
    public Vector3d getEclipticSphericalCoordinates(Date date, Vector3d out) {
	double tau = AstroUtils.tau(AstroUtils.getJulianDateCache(date));

	double L = (L0(tau) + L1(tau) + L2(tau) + L3(tau) + L4(tau) + L5(tau));
	double B = (B0(tau) + B1(tau) + B2(tau) + B3(tau) + B4(tau) + B5(tau));
	double R = (R0(tau) + R1(tau) + R2(tau) + R3(tau) + R4(tau) + R5(tau));
	R = R * AstroUtils.AU_TO_KM;

	out.set(L, B, R * Constants.KM_TO_U);
	return out;

    }

    @Override
    public Vector3d getEquatorialCartesianCoordinates(Date date, Vector3d out) {
	getEclipticSphericalCoordinates(date, out);
	Coordinates.sphericalToCartesian(out, out);
	out.mul(Coordinates.equatorialToEcliptic());
	return out;
    }

}
