package gaia.cu9.ari.gaiaorbit.data.orbit;

import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitDataLoader.OrbitDataLoaderParameter;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.io.IOException;
import java.util.Date;

/**
 * Samples an orbit for a particular Body.
 * @author Toni Sagrista
 *
 */
public class OrbitSamplerDataProvider implements IOrbitDataProvider, IObserver {
    private static boolean writeData = false;
    private static final String writeDataPath = "/home/tsagrista/Workspaces/workspace-luna/GaiaSandbox-android/assets/data/android/";
    OrbitData data;

    public static void main(String[] args) {
	OrbitSamplerDataProvider.writeData = true;
	OrbitSamplerDataProvider me = new OrbitSamplerDataProvider();
	EventManager.instance.subscribe(me, Events.JAVA_EXCEPTION, Events.POST_NOTIFICATION);

	Date now = new Date();
	String[] bodies = new String[] { "Mercury", "Venus", "Earth", "Mars", "Jupiter", "Saturn", "Uranus", "Neptune", "Moon" };
	float[] periods = new float[] { 87.9691f, 224.701f, 365.256363f, 686.971f, 4332.59f, 10759.22f, 30799.095f, 60190.03f, 27.321682f };
	for (int i = 0; i < bodies.length; i++) {

	    String b = bodies[i];
	    float period = periods[i];
	    OrbitDataLoaderParameter param = new OrbitDataLoaderParameter(me.getClass(), b, now, true, period, 80);
	    me.load(null, param);

	}

    }

    @Override
    public void load(String file, OrbitDataLoaderParameter parameter) {
	// Sample using VSOP
	int numSamples = parameter.numSamples > 0 ? parameter.numSamples : (int) (200 * parameter.orbitalPeriod / 365);
	numSamples = Math.max(50, Math.min(1000, numSamples));
	data = new OrbitData();
	String bodyDesc = parameter.name;
	Date d = new Date(parameter.ini.getTime());
	double last = 0, accum = 0;
	Vector3d ecl = new Vector3d();

	// Milliseconds of this orbit in one revolution
	long orbitalMs = (long) parameter.orbitalPeriod * 24 * 60 * 60 * 1000;
	long stepMs = orbitalMs / numSamples;

	// Load vsop orbit data
	for (int i = 0; i <= numSamples; i++) {
	    AstroUtils.getEclipticCoordinates(bodyDesc, d, ecl);

	    if (last == 0) {
		last = Math.toDegrees(ecl.x);
	    }

	    accum += Math.toDegrees(ecl.x) - last;
	    last = Math.toDegrees(ecl.x);

	    if (accum > 355) {
		break;
	    }

	    Coordinates.sphericalToCartesian(ecl, ecl);
	    ecl.mul(Coordinates.equatorialToEcliptic()).scl(Constants.KM_TO_U);
	    data.x.add(ecl.x);
	    data.y.add(ecl.y);
	    data.z.add(ecl.z);
	    d.setTime(d.getTime() + stepMs);
	    data.time.add(new Date(d.getTime()));
	}

	// Close the circle
	data.x.add(data.x.get(0));
	data.y.add(data.y.get(0));
	data.z.add(data.z.get(0));
	d.setTime(d.getTime() + stepMs);
	data.time.add(new Date(d.getTime()));

	if (writeData) {
	    try {
		OrbitDataWriter.writeOrbitData(writeDataPath + "orb." + bodyDesc.toString() + ".dat", data);
	    } catch (IOException e) {
		EventManager.instance.post(Events.JAVA_EXCEPTION, e);
	    }
	}

	EventManager.instance.post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.orbitdataof.loaded", parameter.name, data.getNumPoints()));

    }

    public OrbitData getData() {
	return data;
    }

    @Override
    public void notify(Events event, Object... data) {
	switch (event) {
	case JAVA_EXCEPTION:
	    System.err.println((Exception) data[0]);
	    break;
	case POST_NOTIFICATION:
	    System.out.println((String) data[0] + " -" + (String) data[1]);
	}

    }

}
