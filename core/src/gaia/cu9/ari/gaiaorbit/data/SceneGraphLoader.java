package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import java.io.InputStream;
import java.util.List;
import java.util.Properties;

public class SceneGraphLoader {
    private static final String PROP_DATA_PROVIDERS = "data.providers";

    public static ISceneGraph loadSceneGraph(InputStream props, ITimeFrameProvider time) {
        Properties p = new Properties();
        ISceneGraph sg = null;
        try {
            p.load(props);

            String[] dataProviders = p.getProperty(PROP_DATA_PROVIDERS).split("\\s+");

            SceneGraphNodeProviderManager sgnpm = new SceneGraphNodeProviderManager();
            sgnpm.addProviders(p, dataProviders);

            List<SceneGraphNode> nodes = sgnpm.loadObjects();

            sg = new SceneGraph();

            sg.initialize(nodes, time);

        } catch (Exception e) {
            Logger.error(e);
        }
        return sg;
    }

}
