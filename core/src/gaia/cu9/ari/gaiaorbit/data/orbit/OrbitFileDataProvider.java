package gaia.cu9.ari.gaiaorbit.data.orbit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitDataLoader.OrbitDataLoaderParameter;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.Logger;

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
            FileHandle f = Gdx.files.internal(file);
            data = odl.load(f.read());
            EventManager.instance.post(Events.ORBIT_DATA_LOADED, data, file);
        } catch (Exception e) {
            Logger.error(e, OrbitFileDataProvider.class.getName());
        }
    }

    public OrbitData getData() {
        return data;
    }

}
