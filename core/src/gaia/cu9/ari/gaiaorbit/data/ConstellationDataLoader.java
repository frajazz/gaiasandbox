package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.data.bean.ConstellationsBean;
import gaia.cu9.ari.gaiaorbit.data.constel.ConstellationsLoader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class ConstellationDataLoader extends AsynchronousAssetLoader<ConstellationsBean, ConstellationDataLoader.ConstellationsParameter> {

    ConstellationsBean bean;

    public ConstellationDataLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, ConstellationsParameter parameter) {
        ConstellationsLoader loader = new ConstellationsLoader();
        loader.initialize(file);
        bean = new ConstellationsBean();
        bean.addAll(loader.loadObjects());
    }

    @Override
    public ConstellationsBean loadSync(AssetManager manager, String fileName, FileHandle file, ConstellationsParameter parameter) {
        return bean;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, ConstellationsParameter parameter) {
        return null;
    }

    static public class ConstellationsParameter extends AssetLoaderParameters<ConstellationsBean> {

    }

}
