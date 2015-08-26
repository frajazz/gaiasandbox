package gaia.cu9.ari.gaiaorbit.data.stars;

import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;

import java.io.FileNotFoundException;
import java.util.List;

public interface ISceneGraphLoader {

    public List<? extends SceneGraphNode> loadData() throws FileNotFoundException;

    public void initialize(String[] files) throws RuntimeException;

}
