package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.data.stars.ISceneGraphLoader;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.PrefixedProperties;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/**
 * Class that loads scene graph nodes.
 * @author Toni Sagrista
 * @deprecated Using {@link gaia.cu9.ari.gaiaorbit.data.JsonLoader} instead.
 * @param <T>
 */
public class NodeLoader<T extends SceneGraphNode> implements ISceneGraphLoader {
    private String[] filePaths;

    public NodeLoader() {
        super();
    }

    public List<T> loadData() {
        List<T> bodies = new ArrayList<T>();

        Properties p = new Properties();
        try {
            for (String filePath : filePaths) {
                p.load(FileLocator.getStream(filePath));
                String[] pnames = p.getProperty("entities.all").split("\\s+");

                for (String pname : pnames) {
                    PrefixedProperties pp = new PrefixedProperties(p, pname + ".");
                    List<String> keys = new ArrayList<String>();
                    keys.addAll(pp.stringPropertyNames());
                    Collections.sort(keys);
                    String clazzName = pp.getProperty("impl");

                    @SuppressWarnings("unchecked")
                    Class<T> clazz = (Class<T>) ClassReflection.forName(clazzName);
                    T instance = ClassReflection.newInstance(clazz);

                    for (String key : keys) {
                        if (!key.equals("impl")) {
                            String value = pp.getProperty(key);
                            if (!value.isEmpty() && value != null) {
                                boolean isVector = key.contains(".vec.");
                                boolean isFloat = true;
                                try {
                                    Parser.parseFloat(value);
                                } catch (NumberFormatException e) {
                                    isFloat = false;
                                }

                                Object val = null;
                                Class<?> valueClass = null;
                                if (isVector) {
                                    String[] vector = value.split("\\s+");
                                    val = new Vector3(Float.parseFloat(vector[0]), Float.parseFloat(vector[1]), Float.parseFloat(vector[2]));
                                    valueClass = Vector3.class;
                                } else if (isFloat) {
                                    val = Float.parseFloat(value);
                                    valueClass = Float.class;
                                } else if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
                                    // Boolean
                                    val = Boolean.parseBoolean(value);
                                    valueClass = Boolean.class;
                                } else {
                                    // String
                                    val = value;
                                    valueClass = String.class;
                                }

                                try {
                                    Method m = ClassReflection.getMethod(clazz, "set" + GlobalResources.propertyToMethodName(key), valueClass);
                                    m.invoke(instance, val);
                                } catch (ReflectionException e) {
                                    Logger.error(e);
                                }
                            }
                        }

                    }

                    // Only load and add if it display is activated
                    Method m = ClassReflection.getMethod(clazz, "initialize");
                    m.invoke(instance);

                    bodies.add(instance);

                }

                Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", pnames.length, filePath));

            }
        } catch (Exception e) {
            Gdx.app.error(NodeLoader.class.getSimpleName(), e.getLocalizedMessage());
        }

        return bodies;
    }

    @Override
    public void initialize(String[] files) {
        filePaths = files;
    }
}
