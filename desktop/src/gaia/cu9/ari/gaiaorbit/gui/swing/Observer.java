package gaia.cu9.ari.gaiaorbit.gui.swing;

import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.gui.swing.jsplash.JSplash;

public class Observer implements IObserver {
    JSplash splash;
    int progress = 0;
    int line = 0;

    public Observer(JSplash splash) {
	this.splash = splash;
	EventManager.getInstance().subscribe(this, Events.POST_NOTIFICATION, Events.ORBIT_DATA_LOADED);
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
	    message = "Orbit data loaded: " + (String) data[1] + "(" + ((OrbitData) data[0]).getNumPoints() + " points)";
	    splash.setProgress(progress, message);
	    break;
	}
    }

}
