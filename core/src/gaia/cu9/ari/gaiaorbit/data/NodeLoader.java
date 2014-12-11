package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.PrefixedProperties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;

/**
 * Class that loads scene graph nodes.
 * @author Toni Sagrista
 *
 * @param <T>
 */
public class NodeLoader<T extends SceneGraphNode> implements ISceneGraphNodeProvider {
    private String[] filePaths;

    public NodeLoader() {
	super();
    }

    public List<T> loadObjects() {
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
		    Class<T> clazz = (Class<T>) Class.forName(clazzName);
		    T instance = clazz.newInstance();

		    for (String key : keys) {
			if (!key.equals("impl")) {
			    String value = pp.getProperty(key);
			    if (!value.isEmpty() && value != null) {
				boolean isVector = key.contains(".vec.");
				boolean isFloat = true;
				try {
				    Float.parseFloat(value);
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
				    Method m = clazz.getMethod("set" + GlobalResources.propertyToMethodName(key), valueClass);
				    m.invoke(instance, val);
				} catch (NoSuchMethodException e) {
				    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
				}
			    }
			}

		    }

		    // Only load and add if it display is activated
		    Method m = clazz.getMethod("initialize");
		    m.invoke(instance);

		    bodies.add(instance);

		}

		EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), pnames.length + " objects loaded from file " + filePath);

	    }
	} catch (InvocationTargetException e) {
	    Gdx.app.error(NodeLoader.class.getSimpleName(), e.getTargetException().getLocalizedMessage());
	} catch (Exception e) {
	    Gdx.app.error(NodeLoader.class.getSimpleName(), e.getLocalizedMessage());
	}

	return bodies;
    }

    @Override
    public void initialize(Properties properties) {
	filePaths = properties.getProperty("files").split("\\s+");
    }
}
