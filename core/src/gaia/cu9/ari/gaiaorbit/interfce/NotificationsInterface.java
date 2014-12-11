package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;

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
	this.df = DateFormat.getDateInstance(DateFormat.SHORT);
	EventManager.getInstance().subscribe(this, Events.POST_NOTIFICATION, Events.FOCUS_CHANGED, Events.SIMU_TIME_TOGGLED_INFO, Events.TOGGLE_VISIBILITY_CMD, Events.CAMERA_MODE_CMD, Events.PACE_CHANGED_INFO, Events.FOCUS_LOCK_CMD, Events.TOGGLE_AMBIENT_LIGHT, Events.FOV_CHANGE_NOTIFICATION, Events.JAVA_EXCEPTION, Events.ORBIT_DATA_LOADED, Events.SCREENSHOT_INFO, Events.COMPUTE_GAIA_SCAN_CMD, Events.ONLY_OBSERVED_STARS_CMD, Events.TRANSIT_COLOUR_CMD, Events.LIMIT_MAG_CMD);
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
		    addMessage("Camera focus changed to " + sgn.name);
		}
		break;
	    case SIMU_TIME_TOGGLED_INFO:
		addMessage("Simulation " + ((Boolean) data[0] ? "resumed" : "paused"));
		break;
	    case TOGGLE_VISIBILITY_CMD:
		if (data.length == 2)
		    addMessage("Visibility of " + (String) data[0] + " changed to " + (((Boolean) data[1]) ? "on" : "off"));
		else
		    addMessage("Visibility of " + (String) data[0] + " toggled");
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
		    addMessage("Camera mode changed to " + (CameraMode) data[0]);
		break;
	    case PACE_CHANGED_INFO:
		addMessage("Time pace changed to " + data[0] + " simulation hours/second");
		break;
	    case LIMIT_MAG_CMD:
		addMessage("Limit magnitude set to " + GlobalResources.oneDecimalFormat.format((float) data[0]));
		break;
	    case FOV_CHANGE_NOTIFICATION:
		//addMessage("Field of view changed to " + (float) data[0]);
		break;
	    case JAVA_EXCEPTION:
		addMessage("Error: " + ((Exception) data[0]));
		break;
	    case ORBIT_DATA_LOADED:
		addMessage("Orbit data loaded: " + (String) data[1] + "(" + ((OrbitData) data[0]).getNumPoints() + " points)");
		break;
	    case SCREENSHOT_INFO:
		addMessage("Screenshot saved to " + data[0]);
		break;

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
