package gaia.cu9.ari.gaiaorbit.util;

public abstract class ConfInit {

    private static ConfInit initializer;

    public abstract void initGlobalConf() throws Exception;

    public void initConf() throws Exception {
        initializer.initGlobalConf();
    }
}
