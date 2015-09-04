package gaia.cu9.ari.gaiaorbit.desktop.concurrent;

import gaia.cu9.ari.gaiaorbit.util.concurrent.ILocalVar;

/**
 * This is just a ThreadLocal wrapper.
 * @author tsagrista
 *
 * @param <T>
 */
public class SimpleLocalVar<T> implements ILocalVar<T> {
    T t;

    public SimpleLocalVar() {
        t = initialVal();
    }

    @Override
    public T get() {
        return t;
    }

    @Override
    public T initialVal() {
        return null;
    }

}
