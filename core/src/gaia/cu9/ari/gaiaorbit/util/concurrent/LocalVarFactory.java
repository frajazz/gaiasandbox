package gaia.cu9.ari.gaiaorbit.util.concurrent;

import java.util.concurrent.Callable;

public abstract class LocalVarFactory<V> {

    public static LocalVarFactory instance;

    public static void initialize(LocalVarFactory instance) {
        LocalVarFactory.instance = instance;
    }

    public abstract ILocalVar<V> get(Callable<V> init);
}
