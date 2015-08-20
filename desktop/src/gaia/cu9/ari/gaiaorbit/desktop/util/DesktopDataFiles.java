package gaia.cu9.ari.gaiaorbit.desktop.util;

import gaia.cu9.ari.gaiaorbit.util.IDataFiles;

public class DesktopDataFiles implements IDataFiles {

    @Override
    public String getJsonFiles() {
        return "data/planets.json data/moons.json data/satellites.json data/asteroids.json data/orbits.json data/extra.json data/locations.json data/earth_locations.json data/moon_locations.json";
    }

    @Override
    public String getCatalogFiles() {
        return "data/hygxyz.bin";
    }

    @Override
    public String getConstellationFiles() {
        return "data/constel.csv";
    }

    @Override
    public String getBoundaryFiles() {
        return "data/boundaries.csv";
    }

}
