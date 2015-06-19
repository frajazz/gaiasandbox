package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;

public class Logger {

    com.badlogic.gdx.utils.Logger gdxlogger = new com.badlogic.gdx.utils.Logger("GaiaSandbox");

    public static void error(Throwable t, String tag) {
        if(EventManager.instance.hasSubscriptors(Events.JAVA_EXCEPTION)) {
            EventManager.instance.post(Events.JAVA_EXCEPTION, t, tag);
        }else{
            System.err.println(tag);
            t.printStackTrace(System.err);
        }
    }

    public static void error(Throwable t) {
        if(EventManager.instance.hasSubscriptors(Events.JAVA_EXCEPTION)) {
            EventManager.instance.post(Events.JAVA_EXCEPTION, t);
        }else{
            t.printStackTrace(System.err);
        }
    }

    public static void warn(Object... messages) {
        EventManager.instance.post(Events.POST_NOTIFICATION, messages);
    }

    public static void info(Object... messages) {
        EventManager.instance.post(Events.POST_NOTIFICATION, messages);
    }

}
