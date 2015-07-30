package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.scenegraph.Gaia;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.Planet;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;

public class SimplePool<T> extends Pool<T> {

    String typeName;

    public SimplePool(Class<T> type) {
        this(type, 16, Integer.MAX_VALUE);
    }

    public SimplePool(Class<T> type, int initialCapacity) {
        this(type, initialCapacity, Integer.MAX_VALUE);
    }

    public SimplePool(Class<T> type, int initialCapacity, int max) {
        super(initialCapacity, max);
        typeName = type.getSimpleName();
    }

    @Override
    protected T newObject() {
        switch (typeName) {
        case "Vector3":
            return (T) new Vector3();
        case "Vector3d":
            return (T) new Vector3d();
        case "Planet":
            return (T) new Planet();
        case "Star":
            return (T) new Star();
        case "Particle":
            return (T) new Particle();
        case "Gaia":
            return (T) new Gaia();
        case "SceneGraphNode":
            return (T) new SceneGraphNode();
        }

        Logger.warn("Class " + typeName + " is not poolable!");
        return null;
    }

}
