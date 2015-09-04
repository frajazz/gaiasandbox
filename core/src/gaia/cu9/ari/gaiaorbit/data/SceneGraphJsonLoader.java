package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.AbstractOctreeWrapper;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;

public class SceneGraphJsonLoader {

    public static ISceneGraph loadSceneGraph(InputStream json, ITimeFrameProvider time, boolean multithreading, int maxThreads) {
        ISceneGraph sg = null;
        try {
            List<SceneGraphNode> nodes = new ArrayList<SceneGraphNode>(5000);

            JsonReader jsonReader = new JsonReader();
            JsonValue model = jsonReader.parse(json);

            JsonValue child = model.get("data").child;
            while (child != null) {
                String clazzName = child.getString("loader");
                @SuppressWarnings("unchecked")
                Class<Object> clazz = (Class<Object>) ClassReflection.forName(clazzName);

                JsonValue filesJson = child.get("files");
                if (filesJson != null) {
                    String[] files = filesJson.asStringArray();

                    Constructor c = ClassReflection.getConstructor(clazz);
                    ISceneGraphLoader loader = (ISceneGraphLoader) c.newInstance();

                    // Init loader
                    loader.initialize(files);

                    // Load data
                    nodes.addAll(loader.loadData());
                }

                child = child.next;
            }

            // Initialize nodes and look for octrees
            boolean hasOctree = false;
            for (SceneGraphNode node : nodes) {
                node.initialize();
                if (node instanceof AbstractOctreeWrapper) {
                    hasOctree = true;
                    break;
                }
            }

            sg = SceneGraphImplementationProvider.provider.getImplementation(multithreading, hasOctree, maxThreads);

            sg.initialize(nodes, time);

        } catch (Exception e) {
            Logger.error(e);
        }
        return sg;
    }

}
