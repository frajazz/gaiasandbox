package gaia.cu9.ari.gaiaorbit.util;

import java.io.File;

public abstract class ConfInit {

    public static ConfInit instance;

    public static void initialize(ConfInit instance) throws Exception {
        ConfInit.instance = instance;
        instance.initGlobalConf();
    }

    public abstract void initGlobalConf() throws Exception;

    public abstract void persistGlobalConf(File propsFile);

}
