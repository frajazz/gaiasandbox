package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.data.bean.JsonBean;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class JsonDataLoader extends AsynchronousAssetLoader<JsonBean, JsonDataLoader.JsonParameter> {

    JsonBean bean;

    public JsonDataLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, JsonParameter parameter) {
        JsonLoader loader = new JsonLoader();
        loader.initialize(fileName.split("\\s+"));
        bean = new JsonBean();
        bean.list = loader.loadObjects();

    }

    @Override
    public JsonBean loadSync(AssetManager manager, String fileName, FileHandle file, JsonParameter parameter) {
        return bean;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, JsonParameter parameter) {
        return null;
    }

    static public class JsonParameter extends AssetLoaderParameters<JsonBean> {

    }

}
