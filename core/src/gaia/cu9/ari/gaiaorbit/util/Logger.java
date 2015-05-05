package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;

public class Logger {

    com.badlogic.gdx.utils.Logger gdxlogger = new com.badlogic.gdx.utils.Logger("GaiaSandbox");

    public static void error(Throwable t, String tag) {
        EventManager.instance.post(Events.JAVA_EXCEPTION, t, tag);
    }

    public static void error(Throwable t) {
        EventManager.instance.post(Events.JAVA_EXCEPTION, t);
    }

    public static void info(Object... messages) {
        Logger.info(messages);
    }

}
