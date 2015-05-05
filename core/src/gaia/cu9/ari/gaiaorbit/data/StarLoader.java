package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.data.stars.ICatalogLoader;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.PrefixedProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * {@link ISceneGraphNodeProvider} that loads stars. It gets the stars from an implementation
 * of {@link ICatalogLoader}.
 * @author Toni Sagrista
 *
 */
public class StarLoader implements ISceneGraphNodeProvider {
    private ICatalogLoader[] loaders;

    /**
     * Creates and initialized the catalog
     */
    public StarLoader() {
        super();
    }

    @Override
    public void initialize(Properties properties) {
        try {
            String[] classes = properties.getProperty("loader").split("\\s+");
            loaders = new ICatalogLoader[classes.length];
            for (int i = 0; i < classes.length; i++) {
                loaders[i] = (ICatalogLoader) Class.forName(classes[i]).newInstance();
                PrefixedProperties props = new PrefixedProperties(properties, loaders[i].getClass().getSimpleName() + ".");
                loaders[i].initialize(props);
            }

        } catch (Exception e) {
            Logger.error(e, this.getClass().getSimpleName());
        }
    }

    @Override
    public List<? extends SceneGraphNode> loadObjects() {
        List<SceneGraphNode> nodes = new ArrayList<SceneGraphNode>();
        try {
            for (ICatalogLoader loader : loaders) {
                nodes.addAll(loader.loadCatalog());
            }
            for (SceneGraphNode s : nodes) {
                s.initialize();
            }

        } catch (Exception e) {
            Logger.error(e, this.getClass().getSimpleName());
        }
        return nodes;
    }

}
