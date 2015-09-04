package gaia.cu9.ari.gaiaorbit.util;

import java.io.File;

public abstract class ConfInit {

    public static ConfInit instance;
    /** Used to emulate webgl in desktop. Should be set true by the WebGL ConfInits  **/
    public boolean webgl = false;

    public static void initialize(ConfInit instance) throws Exception {
        ConfInit.instance = instance;
        instance.initGlobalConf();
    }

    public abstract void initGlobalConf() throws Exception;

    public abstract void persistGlobalConf(File propsFile);

}
