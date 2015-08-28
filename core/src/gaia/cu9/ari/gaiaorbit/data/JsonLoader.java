package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.data.stars.ISceneGraphLoader;
import gaia.cu9.ari.gaiaorbit.interfce.TextUtils;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Constructor;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/**
 * Implements the loading of scene graph nodes using libgdx's json library.
 * It loads entities in the JSON format described in <a href="https://github.com/ari-zah/gaiasandbox/wiki/Non-particle-data-loading">this link</a>.
 * @author Toni Sagrista
 *
 * @param <T>
 */
public class JsonLoader<T extends SceneGraphNode> implements ISceneGraphLoader {
    private static final String COMPONENTS_PACKAGE = "gaia.cu9.ari.gaiaorbit.scenegraph.component.";
    /** Params to skip in the normal processing **/
    private static final List<String> PARAM_SKIP = Arrays.asList("args", "impl", "comment", "comments");

    /** Contains all the files to be loaded by this loader **/
    private String[] filePaths;

    @Override
    public void initialize(String[] files) {
        filePaths = files;
    }

    @Override
    public List<? extends SceneGraphNode> loadData() {
        List<T> bodies = new ArrayList<T>();

        try {
            JsonReader json = new JsonReader();
            for (String filePath : filePaths) {
                FileHandle file = Gdx.files.internal(filePath);
                JsonValue model = json.parse(file.read());
                JsonValue child = model.get("objects").child;
                int size = 0;
                while (child != null) {
                    size++;
                    String clazzName = child.getString("impl");

                    @SuppressWarnings("unchecked")
                    Class<Object> clazz = (Class<Object>) ClassReflection.forName(clazzName);

                    // Convert to object and add to list
                    T object = (T) convertJsonToObject(child, clazz);

                    // Only load and add if it display is activated
                    Method m = ClassReflection.getMethod(clazz, "initialize");
                    m.invoke(object);

                    bodies.add(object);

                    child = child.next;
                }
                Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", size, filePath));
            }

        } catch (Exception e) {
            Logger.error(e);
        }

        return bodies;
    }

    /**
     * Converts the given {@link JsonValue} to a java object of the given {@link Class}.
     * @param json The {@link JsonValue} for the object to convert.
     * @param clazz The class of the object.
     * @return The java object of the given class.
     * @throws ReflectionException 
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     * @throws ClassNotFoundException 
     * @throws InstantiationException 
     */
    private Object convertJsonToObject(JsonValue json, Class<?> clazz) throws ReflectionException {
        Object instance;
        try {
            if (json.has("args")) {
                //Creator arguments
                JsonValue args = json.get("args");
                Class<?>[] argumentTypes = new Class<?>[args.size];
                Object[] arguments = new Object[args.size];
                for (int i = 0; i < args.size; i++) {
                    JsonValue arg = args.get(i);
                    argumentTypes[i] = getValueClass(arg);
                    arguments[i] = getValue(arg);
                }
                Constructor constructor = ClassReflection.getConstructor(clazz, argumentTypes);
                instance = constructor.newInstance(arguments);
            } else {
                instance = ClassReflection.newInstance(clazz);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate class: " + e.getMessage());
        }
        JsonValue attribute = json.child;
        while (attribute != null) {
            // We skip some param names
            if (!PARAM_SKIP.contains(attribute.name)) {
                ValueType type = attribute.type();
                Class<?> valueClass = null;
                Object value = null;
                if (attribute.isValue()) {
                    valueClass = getValueClass(attribute);
                    value = getValue(attribute);
                } else if (attribute.isArray()) {
                    // We suppose are childs are of the same type
                    switch (attribute.child.type()) {
                    case stringValue:
                        valueClass = String[].class;
                        value = attribute.asStringArray();
                        break;
                    case doubleValue:
                        valueClass = double[].class;
                        value = attribute.asDoubleArray();
                        break;
                    case booleanValue:
                        valueClass = boolean[].class;
                        value = attribute.asBooleanArray();
                        break;
                    case longValue:
                        valueClass = int[].class;
                        value = attribute.asIntArray();
                        break;
                    }

                } else if (attribute.isObject()) {
                    String clazzName = attribute.has("impl") ? attribute.getString("impl") : TextUtils.capitalise(attribute.name) + "Component";
                    try {
                        valueClass = ClassReflection.forName(clazzName);
                        value = convertJsonToObject(attribute, valueClass);
                    } catch (ReflectionException e1) {
                        // Class not found, probably a component
                        try {
                            valueClass = ClassReflection.forName(COMPONENTS_PACKAGE + clazzName);
                            value = convertJsonToObject(attribute, valueClass);
                        } catch (ReflectionException e2) {
                            // We use a map
                            valueClass = Map.class;
                            value = convertJsonToMap(attribute);
                        }
                    }

                }
                String methodName = "set" + GlobalResources.propertyToMethodName(attribute.name);
                Method m = searchMethod(methodName, valueClass, clazz);
                if (m != null)
                    m.invoke(instance, value);
                else
                    throw new ReflectionException("No method " + methodName + " in class " + valueClass.toString() + " or its interfaces or superclass.");
            }
            attribute = attribute.next;
        }
        return instance;
    }

    public Map<String, Object> convertJsonToMap(JsonValue json) {
        Map<String, Object> map = new TreeMap<String, Object>();

        JsonValue child = json.child;
        while (child != null) {
            Object val = getValue(child);
            if (val != null) {
                map.put(child.name, val);
            }
            child = child.next;
        }

        return map;
    }

    /**
     * Searches for the given method with the given class. If none is found, it looks for fitting methods
     * with the classe's interfaces and superclasses recursively.
     * @param methodName
     * @param clazz
     * @return
     */
    private Method searchMethod(String methodName, Class<?> clazz, Class<?> source) {
        Method m = null;
        try {
            m = ClassReflection.getMethod(source, methodName, clazz);
        } catch (ReflectionException e) {
            if (clazz == null) {
                return null;
            }
            // Let's see if we find a method that fits one of the implementing interfaces
            Class<?>[] interfaces = clazz.getInterfaces();
            boolean found = false;
            int i = 0;
            while (!found && i < interfaces.length) {
                Class<?> current = interfaces[i];
                try {
                    m = ClassReflection.getMethod(source, methodName, current);
                    found = true;
                } catch (ReflectionException e1) {
                    // Not lucky
                }
                i++;
            }
            // Let's try recursively with the superclass
            if (!found) {
                return searchMethod(methodName, clazz.getSuperclass(), source);
            }
        }
        return m;
    }

    private Object getValue(JsonValue val) {
        Object value = null;
        switch (val.type()) {
        case stringValue:
            value = val.asString();
            break;
        case doubleValue:
            value = val.asDouble();
            break;
        case booleanValue:
            value = val.asBoolean();
            break;
        case longValue:
            value = val.asLong();
            break;
        }
        return value;
    }

    private Class<?> getValueClass(JsonValue val) {
        Class<?> valueClass = null;
        switch (val.type()) {
        case stringValue:
            valueClass = String.class;
            break;
        case doubleValue:
            valueClass = Double.class;
            break;
        case booleanValue:
            valueClass = Boolean.class;
            break;
        case longValue:
            valueClass = Long.class;
            break;
        }
        return valueClass;
    }
}
