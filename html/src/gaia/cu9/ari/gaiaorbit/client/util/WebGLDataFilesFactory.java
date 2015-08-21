package gaia.cu9.ari.gaiaorbit.client.util;

import gaia.cu9.ari.gaiaorbit.util.DataFilesFactory;
import gaia.cu9.ari.gaiaorbit.util.IDataFiles;

public class WebGLDataFilesFactory extends DataFilesFactory {

    @Override
    protected IDataFiles getDataFilesConcrete() {
        return new WebGLDataFilesLite();
    }

}
