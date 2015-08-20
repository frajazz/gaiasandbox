package gaia.cu9.ari.gaiaorbit.desktop.util;

import gaia.cu9.ari.gaiaorbit.util.DataFilesFactory;
import gaia.cu9.ari.gaiaorbit.util.IDataFiles;

public class DesktopDataFilesFactory extends DataFilesFactory {

    @Override
    protected IDataFiles getDataFilesConcrete() {
        return new DesktopDataFilesLite();
    }

}
