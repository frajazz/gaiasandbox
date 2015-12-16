package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;

public class Logger {

    public enum LoggerLevel {
        ERROR(0), WARN(1), INFO(2), DEBUG(3);

        public int val;

        LoggerLevel(int val) {
            this.val = val;
        }

        public int getVal() {
            return val;
        }

    }

    public static LoggerLevel level = LoggerLevel.INFO;

    com.badlogic.gdx.utils.Logger gdxlogger = new com.badlogic.gdx.utils.Logger("GaiaSandbox");

    public static void error(Throwable t, String tag) {
        if (inLevel(LoggerLevel.ERROR))
            if (EventManager.instance.hasSubscriptors(Events.JAVA_EXCEPTION)) {
                EventManager.instance.post(Events.JAVA_EXCEPTION, t, tag);
            } else {
                System.err.println(tag);
                t.printStackTrace(System.err);
            }
    }

    public static void error(Throwable t) {
        if (inLevel(LoggerLevel.ERROR))
            if (EventManager.instance.hasSubscriptors(Events.JAVA_EXCEPTION)) {
                EventManager.instance.post(Events.JAVA_EXCEPTION, t);
            } else {
                t.printStackTrace(System.err);
            }
    }

    public static void warn(Object... messages) {
        if (inLevel(LoggerLevel.WARN))
            EventManager.instance.post(Events.POST_NOTIFICATION, messages);
    }

    public static void info(Object... messages) {
        if (inLevel(LoggerLevel.INFO))
            EventManager.instance.post(Events.POST_NOTIFICATION, messages);
    }

    public static void debug(Object... messages) {
        if (inLevel(LoggerLevel.DEBUG))
            EventManager.instance.post(Events.POST_NOTIFICATION, messages);
    }

    private static boolean inLevel(LoggerLevel l) {
        return l.getVal() <= level.getVal();
    }

}
