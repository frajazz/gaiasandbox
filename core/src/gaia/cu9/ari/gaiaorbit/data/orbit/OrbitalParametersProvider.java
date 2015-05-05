package gaia.cu9.ari.gaiaorbit.data.orbit;

import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitDataLoader.OrbitDataLoaderParameter;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.OrbitComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.util.Date;

/**
 * Reads an orbit file into an OrbitData object.
 * @author Toni Sagrista
 *
 */
public class OrbitalParametersProvider implements IOrbitDataProvider {
    OrbitData data;

    @Override
    public void load(String file, OrbitDataLoaderParameter parameter) {
        OrbitComponent params = parameter.orbitalParamaters;
        try {
            // Parameters of the ellipse
            double a = params.semimajoraxis;
            double f = params.e * params.semimajoraxis;
            double b = Math.sqrt(Math.pow(a, 2) - Math.pow(f, 2));

            int nsamples = Math.min(Math.max(180, (int) (a * 0.01)), 500);
            double step = 360d / nsamples;
            Vector3d[] samples = new Vector3d[nsamples + 1];
            int i = 0;
            for (double angledeg = 0; angledeg < 360; angledeg += step) {
                double angleRad = Math.toRadians(angledeg);
                Vector3d point = new Vector3d(b * Math.sin(angleRad), 0d, a * Math.cos(angleRad));
                samples[i] = point;
                i++;
            }
            // Last, to close the orbit.
            samples[i] = samples[0].cpy();

            Matrix4d transform = new Matrix4d();
            transform.scl(Constants.KM_TO_U);
            data = new OrbitData();
            for (Vector3d point : samples) {
                point.mul(transform);
                data.x.add(point.x);
                data.y.add(point.y);
                data.z.add(point.z);
                data.time.add(new Date());
            }
            EventManager.instance.post(Events.ORBIT_DATA_LOADED, data, parameter.name);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public OrbitData getData() {
        return data;
    }

}
