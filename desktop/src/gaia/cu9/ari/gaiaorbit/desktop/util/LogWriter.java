package gaia.cu9.ari.gaiaorbit.desktop.util;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;

import java.util.Date;

public class LogWriter implements IObserver {
    private static final String TAG_SEPARATOR = " - ";

    public LogWriter() {
        EventManager.instance.subscribe(this, Events.JAVA_EXCEPTION, Events.POST_NOTIFICATION);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case JAVA_EXCEPTION:
            if (data.length == 1) {
                System.out.println(new Date().toString() + TAG_SEPARATOR + ((Throwable) data[0]).getLocalizedMessage());
            } else {
                System.out.println(new Date().toString() + TAG_SEPARATOR + (String) data[1] + TAG_SEPARATOR + ((Throwable) data[0]).getLocalizedMessage());

            }
            break;
        case POST_NOTIFICATION:
            String message = "";
            for (int i = 0; i < data.length; i++) {
                message += (String) data[i];
                if (i < data.length - 1 && !(i == data.length - 2 && data[data.length - 1] instanceof Boolean)) {
                    message += TAG_SEPARATOR;
                }

            }
            System.out.println(new Date().toString() + TAG_SEPARATOR + message);
            break;
        }

    }
}
