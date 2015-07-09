package gaia.cu9.ari.gaiaorbit.data.orbit;

import gaia.cu9.ari.gaiaorbit.scenegraph.component.OrbitComponent;

import java.util.Date;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

/**
 * Abstract data loader to rule them all.
 * @author Toni Sagrista
 *
 */
public class OrbitDataLoader extends AsynchronousAssetLoader<OrbitData, OrbitDataLoader.OrbitDataLoaderParameter> {

    OrbitData data;

    public OrbitDataLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, OrbitDataLoaderParameter parameter) {
        return null;
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, OrbitDataLoaderParameter parameter) {
        IOrbitDataProvider provider;
        try {
            provider = getProvider(parameter.providerClass);
            provider.load(fileName, parameter);
            data = provider.getData();
        } catch (Exception e) {
            Gdx.app.error(getClass().getSimpleName(), e.getMessage());
        }

    }

    private IOrbitDataProvider getProvider(String className) {
        switch (className) {
        case "gaia.cu9.ari.gaiaorbit.data.orbit.OrbitalParametersProvider":
            return new OrbitalParametersProvider();
        case "gaia.cu9.ari.gaiaorbit.data.orbit.OrbitFileDataProvider":
            return new OrbitFileDataProvider();
        case "gaia.cu9.ari.gaiaorbit.data.orbit.OrbitSamplerDataProvider":
            return new OrbitSamplerDataProvider();
        }
        return null;
    }

    /**
     * 
     */
    public OrbitData loadSync(AssetManager manager, String fileName, FileHandle file, OrbitDataLoaderParameter parameter) {
        return data;
    }

    static public class OrbitDataLoaderParameter extends AssetLoaderParameters<OrbitData> {

        String providerClass;
        Date ini;
        boolean forward;
        float orbitalPeriod;
        int numSamples;
        String name;
        OrbitComponent orbitalParamaters;

        public OrbitDataLoaderParameter(String providerClass) {
            this.providerClass = providerClass;
        }

        public OrbitDataLoaderParameter(String name, String providerClass, OrbitComponent orbitalParameters) {
            this(providerClass);
            this.name = name;
            this.orbitalParamaters = orbitalParameters;
        }

        public OrbitDataLoaderParameter(String providerClass, String name, Date ini, boolean forward, float orbitalPeriod, int numSamples) {
            this(providerClass);
            this.name = name;
            this.ini = ini;
            this.forward = forward;
            this.orbitalPeriod = orbitalPeriod;
            this.numSamples = numSamples;
        }

        public OrbitDataLoaderParameter(String providerClass, String name, Date ini, boolean forward, float orbitalPeriod) {
            this(providerClass, name, ini, forward, orbitalPeriod, -1);
        }

        public void setIni(Date date) {
            this.ini = date;
        }

        public void setForward(boolean fwd) {
            this.forward = fwd;
        }

        public void setOrbitalPeriod(float period) {
            this.orbitalPeriod = period;
        }
    }
}
