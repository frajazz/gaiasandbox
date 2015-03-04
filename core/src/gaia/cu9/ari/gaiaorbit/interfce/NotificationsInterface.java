package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;

import java.text.DateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/**
 * Widget that captures and displays messages in a GUI.
 * @author Toni Sagrista
 *
 */
public class NotificationsInterface extends Table implements IObserver {
    private static final long DEFAULT_TIMEOUT = 4000;
    DateFormat df;
    long msTimeout;
    Label message;
    LinkedList<MessageBean> historical;
    boolean displaying = false;
    boolean consoleLog = true;

    /** Lock object for synchronization **/
    Object lock;

    public NotificationsInterface(Skin skin, Object lock) {
	this(DEFAULT_TIMEOUT, skin);
	this.lock = lock;
    }

    public NotificationsInterface(long msTimeout, Skin skin) {
	super(skin);
	this.msTimeout = msTimeout;
	message = new OwnLabel("", skin, "hud-med");
	this.add(message).left();
	this.historical = new LinkedList<MessageBean>();
	this.df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
	EventManager.getInstance().subscribe(this, Events.POST_NOTIFICATION, Events.FOCUS_CHANGED, Events.TOGGLE_TIME_CMD, Events.TOGGLE_VISIBILITY_CMD, Events.CAMERA_MODE_CMD, Events.PACE_CHANGED_INFO, Events.FOCUS_LOCK_CMD, Events.TOGGLE_AMBIENT_LIGHT, Events.FOV_CHANGE_NOTIFICATION, Events.JAVA_EXCEPTION, Events.ORBIT_DATA_LOADED, Events.SCREENSHOT_INFO, Events.COMPUTE_GAIA_SCAN_CMD, Events.ONLY_OBSERVED_STARS_CMD, Events.TRANSIT_COLOUR_CMD, Events.LIMIT_MAG_CMD, Events.TOGGLE_STEREOSCOPIC, Events.TOGGLE_CLEANMODE, Events.TOGGLE_GLOBALPAUSE);
    }

    private void addMessage(String msg) {
	this.historical.add(new MessageBean(msg));
	this.message.setText(msg);
	displaying = true;
	if (consoleLog) {
	    Gdx.app.log(df.format(new Date()), msg);
	}
    }

    public void update() {
	if (displaying) {
	    if (new Date().getTime() - historical.getLast().date.getTime() > msTimeout) {
		displaying = false;
		message.setText("");
	    }
	}
    }

    @Override
    public void notify(Events event, Object... data) {
	synchronized (lock) {
	    switch (event) {
	    case POST_NOTIFICATION:
		String message = "";
		for (int i = 0; i < data.length; i++) {
		    message += (String) data[i];
		    if (i < data.length - 1) {
			message += " - ";
		    }
		}
		addMessage(message);
		break;
	    case FOCUS_CHANGED:
		if (data[0] != null) {
		    SceneGraphNode sgn = null;
		    if (data[0] instanceof String) {
			sgn = GaiaSandbox.getInstance().sg.getNode((String) data[0]);
		    } else {
			sgn = (SceneGraphNode) data[0];
		    }
		    addMessage(I18n.bundle.format("notif.camerafocus", sgn.name));
		}
		break;
	    case TOGGLE_TIME_CMD:
		Boolean bool = (Boolean) data[0];
		if (bool == null) {
		    addMessage(I18n.bundle.format("notif.toggle", I18n.bundle.format("gui.time")));
		} else {
		    addMessage(I18n.bundle.format("notif.simulation.pause", (bool ? 0 : 1)));
		}
		break;
	    case TOGGLE_VISIBILITY_CMD:
		if (data.length == 2)
		    addMessage(I18n.bundle.format("notif.visibility.onoff", (String) data[0], ((Boolean) data[1]) ? 1 : 0));
		else
		    addMessage(I18n.bundle.format("notif.visibility.toggle", (String) data[0]));
		break;
	    case FOCUS_LOCK_CMD:
	    case TOGGLE_AMBIENT_LIGHT:
	    case COMPUTE_GAIA_SCAN_CMD:
	    case ONLY_OBSERVED_STARS_CMD:
	    case TRANSIT_COLOUR_CMD:
		addMessage(data[0] + (((Boolean) data[1]) ? " on" : " off"));
		break;
	    case CAMERA_MODE_CMD:
		CameraMode cm = (CameraMode) data[0];
		if (cm != CameraMode.Focus)
		    addMessage(I18n.bundle.format("notif.cameramode.change", (CameraMode) data[0]));
		break;
	    case PACE_CHANGED_INFO:
		addMessage(I18n.bundle.format("notif.timepace.change", data[0]));
		break;
	    case LIMIT_MAG_CMD:
		addMessage(I18n.bundle.format("notif.limitmag", data[0]));
		break;
	    case FOV_CHANGE_NOTIFICATION:
		//addMessage("Field of view changed to " + (float) data[0]);
		break;
	    case JAVA_EXCEPTION:
		addMessage(I18n.bundle.format("notif.error", data[0]));
		break;
	    case ORBIT_DATA_LOADED:
		addMessage(I18n.bundle.format("notif.orbitdata.loaded", data[1], ((OrbitData) data[0]).getNumPoints()));
		break;
	    case SCREENSHOT_INFO:
		addMessage(I18n.bundle.format("notif.screenshot", data[0]));
		break;
	    case TOGGLE_STEREOSCOPIC:
	    case TOGGLE_CLEANMODE:
	    case TOGGLE_GLOBALPAUSE:
		addMessage(I18n.bundle.format("notif.toggle", data[0]));
	    }
	}
    }

    public int getNumberMessages() {
	return historical.size();
    }

    public List<MessageBean> getHistorical() {
	return historical;
    }

    public class MessageBean {
	String msg;
	Date date;

	public MessageBean(String msg) {
	    this.msg = msg;
	    this.date = new Date();
	}
    }

}
