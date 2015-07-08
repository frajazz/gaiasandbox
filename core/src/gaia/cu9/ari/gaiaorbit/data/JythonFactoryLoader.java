package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.script.JythonFactory;
import gaia.cu9.ari.gaiaorbit.util.Logger;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

/**
 * Loads the JythonFacotry
 * Created by tsagrista on 10/06/15.
 */
public class JythonFactoryLoader extends AsynchronousAssetLoader<JythonFactory, JythonFactoryLoader.JythonFactoryLoaderParameter> {

    JythonFactory factory;

    public JythonFactoryLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, JythonFactoryLoaderParameter parameter) {
        Logger.info(this.getClass().getSimpleName(), "Loading Jython instance");
        JythonFactory.initialize();
        factory = JythonFactory.getInstance();
    }

    @Override
    public JythonFactory loadSync(AssetManager manager, String fileName, FileHandle file, JythonFactoryLoaderParameter parameter) {
        return factory;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, JythonFactoryLoaderParameter parameter) {
        return null;
    }

    static public class JythonFactoryLoaderParameter extends AssetLoaderParameters<JythonFactory> {

        public JythonFactoryLoaderParameter() {

        }
    }
}
