package gaia.cu9.ari.gaiaorbit.desktop.concurrent;

import gaia.cu9.ari.gaiaorbit.util.concurrent.ILocalVar;

/**
 * This is just a ThreadLocal wrapper.
 * @author tsagrista
 *
 * @param <T>
 */
public class ThreadLocalVar<T> implements ILocalVar<T> {
    ThreadLocal<T> tl;

    public ThreadLocalVar() {
        tl = new ThreadLocal<T>() {
            @Override
            public T initialValue() {
                return initialVal();
            }
        };
    }

    @Override
    public T get() {
        return tl.get();
    }

    @Override
    public T initialVal() {
        return null;
    }

}
