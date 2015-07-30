package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.data.bean.HYGBean;
import gaia.cu9.ari.gaiaorbit.data.stars.HYGBinaryLoader;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.Logger;

import java.io.FileNotFoundException;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class HYGCatalogLoader extends AsynchronousAssetLoader<HYGBean, HYGCatalogLoader.HYGLoaderParameter> {

    HYGBean bean;

    public HYGCatalogLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    @Override
    public void loadAsync(AssetManager manager, String fileName, FileHandle file, HYGLoaderParameter parameter) {
        HYGBinaryLoader loader = new HYGBinaryLoader();
        loader.initialize(file);
        bean = new HYGBean();
        try {
            bean.addAll(loader.loadCatalog());
        } catch (FileNotFoundException e) {
            Logger.error(e);
        }
        for (SceneGraphNode n : bean.list()) {
            n.initialize();
        }

    }

    @Override
    public HYGBean loadSync(AssetManager manager, String fileName, FileHandle file, HYGLoaderParameter parameter) {
        return bean;
    }

    @Override
    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, HYGLoaderParameter parameter) {
        return null;
    }

    static public class HYGLoaderParameter extends AssetLoaderParameters<HYGBean> {

    }

}
