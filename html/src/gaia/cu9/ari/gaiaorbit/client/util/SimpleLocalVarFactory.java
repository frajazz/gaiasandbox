package gaia.cu9.ari.gaiaorbit.client.util;

import gaia.cu9.ari.gaiaorbit.util.concurrent.ILocalVar;
import gaia.cu9.ari.gaiaorbit.util.concurrent.LocalVarFactory;

import java.util.concurrent.Callable;

public class SimpleLocalVarFactory<T> extends LocalVarFactory {

    @Override
    public ILocalVar<T> get(final Callable init) {
        ILocalVar<T> var = new SimpleLocalVar<T>() {
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
