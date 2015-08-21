package gaia.cu9.ari.gaiaorbit.client.util;

import gaia.cu9.ari.gaiaorbit.util.IDataFiles;

public class WebGLDataFilesLite implements IDataFiles {

    @Override
    public String getJsonFiles() {
        return "data/planets.json data/satellites.json data/orbits.json data/extra.json";
    }

    @Override
    public String getCatalogFiles() {
        return "data/daycatalogs/hygxyz-20150821.bin";
    }

    @Override
    public String getConstellationFiles() {
        return null;
    }

    @Override
    public String getBoundaryFiles() {
        return null;
    }

}
