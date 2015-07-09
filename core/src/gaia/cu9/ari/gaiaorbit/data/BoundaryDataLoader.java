package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.data.bean.BoundariesBean;
import gaia.cu9.ari.gaiaorbit.data.constel.ConstelBoundariesLoader;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class BoundaryDataLoader extends AsynchronousAssetLoader<BoundariesBean, BoundaryDataLoader.BoundariesParameter> {

    BoundariesBean bean;

    public BoundaryDataLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, BoundariesParameter parameter) {
        ConstelBoundariesLoader loader = new ConstelBoundariesLoader();
        loader.initialize(file);
        bean = new BoundariesBean();
        bean.list.addAll(loader.loadObjects());
    }

    @Override
    public BoundariesBean loadSync(AssetManager manager, String fileName, FileHandle file, BoundariesParameter parameter) {
        return bean;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, BoundariesParameter parameter) {
        return null;
    }

    static public class BoundariesParameter extends AssetLoaderParameters<BoundariesBean> {

    }

}
