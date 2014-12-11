package gaia.cu9.ari.gaiaorbit.data.orbit;

import gaia.cu9.ari.gaiaorbit.data.FileLocator;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitDataLoader.OrbitDataLoaderParameter;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;

import com.badlogic.gdx.Gdx;

/**
 * Reads an orbit file into an OrbitData object.
 * @author Toni Sagrista
 *
 */
public class OrbitFileDataProvider implements IOrbitDataProvider {
    OrbitData data;

    @Override
    public void load(String file, OrbitDataLoaderParameter parameter) {
	FileDataLoader odl = new FileDataLoader();
	try {
	    data = odl.load(FileLocator.getStream(file));
	    EventManager.getInstance().post(Events.ORBIT_DATA_LOADED, data, file);
	} catch (Exception e) {
	    Gdx.app.error(OrbitFileDataProvider.class.getName(), e.getMessage());
	}
    }

    public OrbitData getData() {
	return data;
    }

}
