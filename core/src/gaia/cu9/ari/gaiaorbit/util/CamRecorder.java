package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** 
 * Contains the logic to record the camera state at each frame.
 * @author Toni Sagrista
 *
 */
public class CamRecorder implements IObserver {
    /** Singleton **/
    public static CamRecorder instance;
    private static final String sep = " ";

    public boolean recording;
    private BufferedWriter os;
    private File f;
    private long startMs;

    public static void initialize() {
	instance = new CamRecorder();
    }

    public CamRecorder() {
	this.recording = false;
	EventManager.instance.subscribe(this, Events.RECORD_CAMERA_CMD);
    }

    public void update(Vector3d position, Vector3d direction, Vector3d up) {
	if (recording && os != null) {
	    try {
		os.append(Double.toString(position.x)).append(sep).append(Double.toString(position.y)).append(sep).append(Double.toString(position.z));
		os.append(sep).append(Double.toString(direction.x)).append(sep).append(Double.toString(direction.y)).append(sep).append(Double.toString(direction.z));
		os.append(sep).append(Double.toString(up.x)).append(sep).append(Double.toString(up.y)).append(sep).append(Double.toString(up.z));
		os.append("\n");
	    } catch (IOException e) {
		EventManager.instance.post(Events.JAVA_EXCEPTION, e);
	    }
	}
    }

    @Override
    public void notify(Events event, Object... data) {
	switch (event) {
	case RECORD_CAMERA_CMD:
	    if (data[0] != null) {
		recording = (Boolean) data[0];
	    } else {
		recording = !recording;
	    }

	    if (recording) {
		// We start recording, prepare buffer!
		if (os != null) {
		    throw new RuntimeException("Hey, the buffer is already set up! Can not start recording the camera!");
		}
		f = new File(System.getProperty("java.io.tmpdir"), System.currentTimeMillis() + "_gscamera.dat");
		if (f.exists()) {
		    f.delete();
		}
		try {
		    f.createNewFile();
		    os = new BufferedWriter(new FileWriter(f));
		} catch (IOException e) {
		    EventManager.instance.post(Events.JAVA_EXCEPTION, e);
		}
		startMs = System.currentTimeMillis();
		EventManager.instance.post(Events.POST_NOTIFICATION, I18n.bundle.get("notif.camerarecord.start"));

	    } else {
		// Flush and close
		try {
		    os.close();
		} catch (IOException e) {
		    EventManager.instance.post(Events.JAVA_EXCEPTION, e);
		}
		os = null;
		long elapsed = System.currentTimeMillis() - startMs;
		startMs = 0;
		float secs = elapsed / 1000f;
		EventManager.instance.post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.camerarecord.done", f.getAbsolutePath(), secs));
		f = null;
	    }
	    break;
	}

    }

}
