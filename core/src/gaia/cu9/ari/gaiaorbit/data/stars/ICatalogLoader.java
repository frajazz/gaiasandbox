package gaia.cu9.ari.gaiaorbit.data.stars;

import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;

import java.io.FileNotFoundException;
import java.util.List;

public interface ICatalogLoader {

    public List<? extends SceneGraphNode> loadCatalog() throws FileNotFoundException;

}
