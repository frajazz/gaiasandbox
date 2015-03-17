package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;

public class CamRecorder implements IObserver {

    public static CamRecorder instance;
    public boolean recording;

    public static void initialize() {
	instance = new CamRecorder();
    }

    public CamRecorder() {
	this.recording = false;
	EventManager.instance.subscribe(this, Events.RECORD_CAMERA_CMD);
    }

    @Override
    public void notify(Events event, Object... data) {
	switch (event) {
	case RECORD_CAMERA_CMD:

	    break;
	}

    }

}
