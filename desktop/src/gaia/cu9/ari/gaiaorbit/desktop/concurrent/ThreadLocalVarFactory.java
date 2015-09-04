package gaia.cu9.ari.gaiaorbit.desktop.concurrent;

import gaia.cu9.ari.gaiaorbit.util.concurrent.ILocalVar;
import gaia.cu9.ari.gaiaorbit.util.concurrent.LocalVarFactory;

import java.util.concurrent.Callable;

public class ThreadLocalVarFactory<T> extends LocalVarFactory {

    @Override
    public ILocalVar<T> get(final Callable init) {
        ILocalVar<T> var = new ThreadLocalVar<T>() {
            @Override
            public T initialVal() {
                try {
                    return (T) init.call();
                } catch (Exception e) {
                    return null;
                }
            }
        };
        return var;
    }

}
