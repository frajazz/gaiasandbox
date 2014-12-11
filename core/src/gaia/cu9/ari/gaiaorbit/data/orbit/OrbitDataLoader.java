package gaia.cu9.ari.gaiaorbit.data.orbit;

import gaia.cu9.ari.gaiaorbit.scenegraph.Orbit.OrbitalParameters;

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
	    provider = parameter.providerClass.newInstance();
	    provider.load(fileName, parameter);
	    data = provider.getData();
	} catch (Exception e) {
	    Gdx.app.error(getClass().getSimpleName(), e.getMessage());
	}

    }

    /**
     * 
     */
    public OrbitData loadSync(AssetManager manager, String fileName, FileHandle file, OrbitDataLoaderParameter parameter) {
	return data;
    }

    static public class OrbitDataLoaderParameter extends AssetLoaderParameters<OrbitData> {

	Class<? extends IOrbitDataProvider> providerClass;
	Date ini;
	boolean forward;
	float orbitalPeriod;
	int numSamples;
	String name;
	OrbitalParameters orbitalParamaters;

	public OrbitDataLoaderParameter(Class<? extends IOrbitDataProvider> providerClass) {
	    this.providerClass = providerClass;
	}

	public OrbitDataLoaderParameter(Class<? extends IOrbitDataProvider> providerClass, OrbitalParameters orbitalParameters) {
	    this(providerClass);
	    this.orbitalParamaters = orbitalParameters;

	}

	public OrbitDataLoaderParameter(Class<? extends IOrbitDataProvider> providerClass, String name, Date ini, boolean forward, float orbitalPeriod, int numSamples) {
	    this(providerClass);
	    this.name = name;
	    this.ini = ini;
	    this.forward = forward;
	    this.orbitalPeriod = orbitalPeriod;
	    this.numSamples = numSamples;
	}

	public OrbitDataLoaderParameter(Class<? extends IOrbitDataProvider> providerClass, String name, Date ini, boolean forward, float orbitalPeriod) {
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
