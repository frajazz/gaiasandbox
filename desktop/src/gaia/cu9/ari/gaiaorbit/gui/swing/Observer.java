package gaia.cu9.ari.gaiaorbit.gui.swing;

import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.gui.swing.jsplash.JSplash;
import gaia.cu9.ari.gaiaorbit.util.I18n;

public class Observer implements IObserver {
    JSplash splash;
    int progress = 0;
    int line = 0;

    public Observer(JSplash splash) {
	this.splash = splash;
	EventManager.instance.subscribe(this, Events.POST_NOTIFICATION, Events.ORBIT_DATA_LOADED);
    }

    @Override
    public void notify(Events event, Object... data) {
	progress += 4;
	String message;
	switch (event) {
	case POST_NOTIFICATION:
	    if (data.length > 1)
		message = (String) data[1];
	    else
		message = (String) data[0];
	    splash.setProgress(progress, message);

	    message = "";
	    for (int i = 0; i < data.length; i++) {
		message += (String) data[i];
		if (i < data.length - 1) {
		    message += " - ";
		}
	    }

	    break;
	case ORBIT_DATA_LOADED:
	    message = I18n.bundle.format("notif.orbitdata.loaded", data[1], ((OrbitData) data[0]).getNumPoints());
	    splash.setProgress(progress, message);
	    break;
	}
    }

}
