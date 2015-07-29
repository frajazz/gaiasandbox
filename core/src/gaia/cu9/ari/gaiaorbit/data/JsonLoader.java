package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Gaia;
import gaia.cu9.ari.gaiaorbit.scenegraph.Grid;
import gaia.cu9.ari.gaiaorbit.scenegraph.Loc;
import gaia.cu9.ari.gaiaorbit.scenegraph.MilkyWay;
import gaia.cu9.ari.gaiaorbit.scenegraph.ModelBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Mw;
import gaia.cu9.ari.gaiaorbit.scenegraph.Orbit;
import gaia.cu9.ari.gaiaorbit.scenegraph.Planet;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.AtmosphereComponent;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.OrbitComponent;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.RotationComponent;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.TextureComponent;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.GaiaCoordinates;
import gaia.cu9.ari.gaiaorbit.util.coord.IBodyCoordinates;
import gaia.cu9.ari.gaiaorbit.util.coord.MoonAACoordinates;
import gaia.cu9.ari.gaiaorbit.util.coord.OrbitLintCoordinates;
import gaia.cu9.ari.gaiaorbit.util.coord.StaticCoordinates;
import gaia.cu9.ari.gaiaorbit.util.coord.vsop87.EarthVSOP87;
import gaia.cu9.ari.gaiaorbit.util.coord.vsop87.JupiterVSOP87;
import gaia.cu9.ari.gaiaorbit.util.coord.vsop87.MarsVSOP87;
import gaia.cu9.ari.gaiaorbit.util.coord.vsop87.MercuryVSOP87;
import gaia.cu9.ari.gaiaorbit.util.coord.vsop87.NeptuneVSOP87;
import gaia.cu9.ari.gaiaorbit.util.coord.vsop87.SaturnVSOP87;
import gaia.cu9.ari.gaiaorbit.util.coord.vsop87.UranusVSOP87;
import gaia.cu9.ari.gaiaorbit.util.coord.vsop87.VenusVSOP87;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;

/**
 * Implements the loading of scene graph nodes using libgdx's json library.
 * It loads entities in the JSON format described in <a href="https://github.com/ari-zah/gaiasandbox/wiki/Non-particle-data-loading">this link</a>.
 * @author Toni Sagrista
 *
 * @param <T>
 */
public class JsonLoader<T extends SceneGraphNode> implements ISceneGraphNodeProvider {
    private static final String COMPONENTS_PACKAGE = "gaia.cu9.ari.gaiaorbit.scenegraph.component.";
    /** Params to skip in the normal processing **/
    private static final List<String> PARAM_SKIP = Arrays.asList("args", "impl", "comment", "comments");

    /** Contains all the files to be loaded by this loader **/
    private String[] filePaths;

    public void initialize(String... files) {
        filePaths = files;
    }

    @Override
    public List<? extends SceneGraphNode> loadObjects() {
        List<T> bodies = new ArrayList<T>();

        try {
            JsonReader json = new JsonReader();
            for (String filePath : filePaths) {
                JsonValue model = json.parse(Gdx.files.internal(filePath).read());
                JsonValue child = model.get("objects").child;
                int size = 0;
                while (child != null) {
                    size++;
                    String clazzName = child.getString("impl");

                    // Convert to object and add to list
                    T object = (T) convertJsonToObject(child, clazzName);

                    // Only load and add if it display is activated
                    object.initialize();

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

    private Object getInstance(String clazzName) {
        switch (clazzName) {
        case "gaia.cu9.ari.gaiaorbit.scenegraph.Orbit":
            return new Orbit();
        case "gaia.cu9.ari.gaiaorbit.scenegraph.Grid":
            return new Grid();
        case "gaia.cu9.ari.gaiaorbit.scenegraph.MilkyWay":
            return new MilkyWay();
        case "gaia.cu9.ari.gaiaorbit.scenegraph.Mw":
            return new Mw();
        case "gaia.cu9.ari.gaiaorbit.scenegraph.Planet":
            return new Planet();
        case "gaia.cu9.ari.gaiaorbit.scenegraph.Loc":
            return new Loc();
        case "gaia.cu9.ari.gaiaorbit.scenegraph.Gaia":
            return new Gaia();
        case "gaia.cu9.ari.gaiaorbit.scenegraph.component.TextureComponent":
            return new TextureComponent();
        case "gaia.cu9.ari.gaiaorbit.scenegraph.component.AtmosphereComponent":
            return new AtmosphereComponent();
        case "gaia.cu9.ari.gaiaorbit.scenegraph.component.RotationComponent":
            return new RotationComponent();
        case "gaia.cu9.ari.gaiaorbit.scenegraph.component.OrbitComponent":
            return new OrbitComponent();
        case "gaia.cu9.ari.gaiaorbit.util.coord.StaticCoordinates":
            return new StaticCoordinates();
        case "gaia.cu9.ari.gaiaorbit.util.coord.OrbitLintCoordinates":
            return new OrbitLintCoordinates();
        case "gaia.cu9.ari.gaiaorbit.util.coord.MoonAACoordinates":
            return new MoonAACoordinates();
        case "gaia.cu9.ari.gaiaorbit.util.coord.GaiaCoordinates":
            return new GaiaCoordinates();
        case "gaia.cu9.ari.gaiaorbit.util.coord.vsop87.MercuryVSOP87":
            return new MercuryVSOP87();
        case "gaia.cu9.ari.gaiaorbit.util.coord.vsop87.VenusVSOP87":
            return new VenusVSOP87();
        case "gaia.cu9.ari.gaiaorbit.util.coord.vsop87.EarthVSOP87":
            return new EarthVSOP87();
        case "gaia.cu9.ari.gaiaorbit.util.coord.vsop87.MarsVSOP87":
            return new MarsVSOP87();
        case "gaia.cu9.ari.gaiaorbit.util.coord.vsop87.JupiterVSOP87":
            return new JupiterVSOP87();
        case "gaia.cu9.ari.gaiaorbit.util.coord.vsop87.SaturnVSOP87":
            return new SaturnVSOP87();
        case "gaia.cu9.ari.gaiaorbit.util.coord.vsop87.UranusVSOP87":
            return new UranusVSOP87();
        case "gaia.cu9.ari.gaiaorbit.util.coord.vsop87.NeptuneVSOP87":
            return new NeptuneVSOP87();
        case "Map":
            return new HashMap<String, Object>();
        }
        return null;
    }

    private Object getParamInstance(String className, Boolean argument) {
        switch (className) {
        case "gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent":
            if (argument != null) {
                return new ModelComponent(argument);
            } else {
                return new ModelComponent();
            }
        }
        return null;
    }

    /**
     * Converts the given {@link JsonValue} to a java object of the given {@link Class}.
     * @param json The {@link JsonValue} for the object to convert.
     * @param clazz The class of the object.
     * @return The java object of the given class.
     * @throws SecurityException 
     * @throws NoSuchMethodException 
     * @throws InvocationTargetException 
     * @throws IllegalArgumentException 
     * @throws IllegalAccessException 
     * @throws ClassNotFoundException 
     * @throws InstantiationException 
     */
    private Object convertJsonToObject(JsonValue json, String clazzName) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, ClassNotFoundException {
        Object instance;
        try {
            if (json.has("args")) {
                //Creator arguments
                JsonValue args = json.get("args");
                JsonValue arg = args.get(0);
                Boolean argument = arg.asBoolean();
                instance = getParamInstance(clazzName, argument);
            } else {
                instance = getInstance(clazzName);
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
                    String objClazzName = attribute.has("impl") ? attribute.getString("impl") : attribute.name.equals("params") ? "Map" : COMPONENTS_PACKAGE + GlobalResources.capitalise(attribute.name) + "Component";
                    value = convertJsonToObject(attribute, objClazzName);
                    if (value == null) {
                        // Class not found, probably a component
                        try {
                            value = convertJsonToObject(attribute, objClazzName);
                        } catch (ClassNotFoundException e2) {
                            // We use a map
                            valueClass = Map.class;
                            value = convertJsonToMap(attribute);
                        }
                    }

                }
                String methodName = GlobalResources.propertyToMethodName(attribute.name);
                invokeMethod(instance, methodName, value);

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

    private void invokeMethod(Object instance, String methodName, Object param) {
        if (instance instanceof SceneGraphNode) {
            SceneGraphNode obj = (SceneGraphNode) instance;
            switch (methodName) {
            case "Name":
                obj.setName((String) param);
                return;
            case "Ct":
                obj.setCt((String) param);
                return;
            case "Parent":
                obj.setParent((String) param);
                return;
            }
        }
        if (instance instanceof AbstractPositionEntity) {
            AbstractPositionEntity obj = (AbstractPositionEntity) instance;
            switch (methodName) {
            case "Color":
                obj.setColor((double[]) param);
                return;
            case "Size":
                if (param instanceof Long)
                    obj.setSize((Long) param);
                else
                    obj.setSize((Double) param);
                return;
            case "Coordinates":
                obj.setCoordinates((IBodyCoordinates) param);
                return;
            }
        }
        if (instance instanceof CelestialBody) {
            CelestialBody obj = (CelestialBody) instance;
            switch (methodName) {
            case "Rotation":
                obj.setRotation((RotationComponent) param);
                return;
            }
        }
        if (instance instanceof ModelBody) {
            ModelBody obj = (ModelBody) instance;
            switch (methodName) {
            case "Model":
                obj.setModel((ModelComponent) param);
                return;
            }
        }
        if (instance instanceof Planet) {
            Planet obj = (Planet) instance;
            switch (methodName) {
            case "Atmosphere":
                obj.setAtmosphere((AtmosphereComponent) param);
                return;
            }
        }
        if (instance instanceof Orbit) {
            Orbit obj = (Orbit) instance;
            switch (methodName) {
            case "Provider":
                obj.setProvider((String) param);
                return;
            case "Orbit":
                obj.setOrbit((OrbitComponent) param);
                return;
            }
        }
        if (instance instanceof Grid) {
            Grid obj = (Grid) instance;
            switch (methodName) {
            case "TransformName":
                obj.setTransformName((String) param);
                return;
            }
        }
        if (instance instanceof MilkyWay) {
            MilkyWay obj = (MilkyWay) instance;
            switch (methodName) {
            case "Labelcolor":
                obj.setLabelcolor((double[]) param);
                return;
            case "TransformName":
                obj.setTransformName((String) param);
                return;
            case "Model":
                if (param instanceof String) {
                    obj.setModel((String) param);
                } else {
                    obj.setModel((ModelComponent) param);
                }
                return;
            }
        }
        if (instance instanceof Mw) {
            Mw obj = (Mw) instance;
            switch (methodName) {
            case "TransformName":
                obj.setTransformName((String) param);
                return;
            case "Model":
                obj.setModel((ModelComponent) param);
                return;
            }
        }
        if (instance instanceof Loc) {
            Loc obj = (Loc) instance;
            switch (methodName) {
            case "Location":
                obj.setLocation((double[]) param);
                return;
            case "DistFactor":
                obj.setDistFactor((Double) param);
                return;
            }
        }
        if (instance instanceof StaticCoordinates) {
            StaticCoordinates obj = (StaticCoordinates) instance;
            switch (methodName) {
            case "Position":
                obj.setPosition((double[]) param);
                return;
            }
        }
        if (instance instanceof OrbitLintCoordinates) {
            OrbitLintCoordinates obj = (OrbitLintCoordinates) instance;
            switch (methodName) {
            case "Orbitname":
                obj.setOrbitname((String) param);
                return;
            }
        }
        if (instance instanceof ModelComponent) {
            ModelComponent obj = (ModelComponent) instance;
            switch (methodName) {
            case "Type":
                obj.setType((String) param);
                return;
            case "Model":
                obj.setModel((String) param);
                return;
            case "Params":
                obj.setParams((Map<String, Object>) param);
                return;
            case "Texture":
                obj.setTexture((TextureComponent) param);
                return;
            }
        }
        if (instance instanceof TextureComponent) {
            TextureComponent obj = (TextureComponent) instance;
            switch (methodName) {
            case "Base":
                obj.setBase((String) param);
                return;
            case "Ring":
                obj.setRing((String) param);
                return;
            }
        }
        if (instance instanceof RotationComponent) {
            RotationComponent obj = (RotationComponent) instance;
            switch (methodName) {
            case "Period":
                obj.setPeriod((Double) param);
                return;
            case "Axialtilt":
                obj.setAxialtilt((Double) param);
                return;
            case "Inclination":
                if (param instanceof Double)
                    obj.setInclination((Double) param);
                else
                    obj.setInclination((Long) param);
                return;
            case "Ascendingnode":
                obj.setAscendingnode((Double) param);
                return;
            case "Meridianangle":
                obj.setMeridianangle((Double) param);
                return;
            case "Angle":
                obj.setAngle((Double) param);
                return;
            }
        }
        if (instance instanceof OrbitComponent) {
            OrbitComponent obj = (OrbitComponent) instance;
            switch (methodName) {
            case "Source":
                obj.setSource((String) param);
                return;
            case "Period":
                obj.setPeriod((Double) param);
                return;
            case "Epoch":
                if (param instanceof Double)
                    obj.setEpoch((Double) param);
                else
                    obj.setEpoch((Long) param);
                return;
            case "Semimajoraxis":
                obj.setSemimajoraxis((Double) param);
                return;
            case "Eccentricity":
                obj.setEccentricity((Double) param);
                return;
            case "Inclination":
                obj.setInclination((Double) param);
                return;
            case "Ascendingnode":
                obj.setAscendingnode((Double) param);
                return;
            case "Argofpericenter":
                obj.setArgofpericenter((Double) param);
                return;
            case "Meananomaly":
                obj.setMeananomaly((Double) param);
                return;
            }
        }
        if (instance instanceof AtmosphereComponent) {
            AtmosphereComponent obj = (AtmosphereComponent) instance;
            switch (methodName) {
            case "Size":
                obj.setSize((Double) param);
                return;
            case "Params":
                obj.setParams((Map<String, Object>) param);
                return;
            case "Wavelengths":
                obj.setWavelengths((double[]) param);
                return;
            case "M_Kr":
                obj.setM_Kr((Double) param);
                return;
            case "M_Km":
                obj.setM_Km((Double) param);
                return;
            }
        }
        if (instance instanceof Map) {
            Map<String, Object> obj = (Map<String, Object>) instance;
            obj.put(methodName.toLowerCase(), param);
            return;
        }

        int a = 435;
        return;
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
