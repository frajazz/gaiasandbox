package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
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
    private static final long DEFAULT_TIMEOUT = 5000;
    private static final String TAG_SEPARATOR = " - ";
    DateFormat df;
    long msTimeout;
    Label message1;
    Label message2;
    LinkedList<MessageBean> historical;
    boolean displaying = false;
    boolean consoleLog = true;
    boolean permanent = false;
    boolean multiple = false;
    boolean writeDates = true;

    /** Lock object for synchronization **/
    Object lock;

    /**
     * Initializes the notifications interface.
     * @param skin The skin.
     * @param lock The lock object.
     * @param multiple Allow multiple messages?
     * @param writeDates Write dates with messages?
     */
    public NotificationsInterface(Skin skin, Object lock, boolean multiple, boolean writeDates) {
        this(skin, lock, multiple);
        this.writeDates = writeDates;
    }

    /**
     * Initializes the notifications interface.
     * @param skin The skin.
     * @param lock The lock object.
     * @param multiple Allow multiple messages?
     */
    public NotificationsInterface(Skin skin, Object lock, boolean multiple) {
        this(DEFAULT_TIMEOUT, skin, multiple);
        this.lock = lock;

    }

    /**
     * Initializes the notifications interface.
     * @param msTimeout The timeout in ms.
     * @param skin The skin.
     */
    public NotificationsInterface(long msTimeout, Skin skin, boolean multiple) {
        super(skin);
        this.msTimeout = msTimeout;
        this.multiple = multiple;

        // Create second message if necessary
        if (multiple) {
            message2 = new OwnLabel("", skin, "hud-med");
            this.add(message2).left().row();
        }
        //Create message
        message1 = new OwnLabel("", skin, "hud-med");
        this.add(message1).left();
        this.historical = new LinkedList<MessageBean>();
        this.df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
        EventManager.instance.subscribe(this, Events.POST_NOTIFICATION, Events.FOCUS_CHANGED, Events.TOGGLE_TIME_CMD, Events.TOGGLE_VISIBILITY_CMD, Events.CAMERA_MODE_CMD, Events.PACE_CHANGED_INFO, Events.FOCUS_LOCK_CMD, Events.TOGGLE_AMBIENT_LIGHT, Events.FOV_CHANGE_NOTIFICATION, Events.JAVA_EXCEPTION, Events.ORBIT_DATA_LOADED, Events.SCREENSHOT_INFO, Events.COMPUTE_GAIA_SCAN_CMD, Events.ONLY_OBSERVED_STARS_CMD, Events.TRANSIT_COLOUR_CMD, Events.LIMIT_MAG_CMD, Events.TOGGLE_STEREOSCOPIC, Events.TOGGLE_CLEANMODE, Events.FRAME_OUTPUT_CMD, Events.TOGGLE_STEREO_PROFILE);
    }

    private void addMessage(String msg) {
        addMessage(msg, false);
    }

    private void addMessage(String msg, boolean permanent) {
        MessageBean messageBean = new MessageBean(msg);

        if (multiple && !historical.isEmpty() && !historical.getLast().finished(msTimeout)) {
            // Move current up
            this.message2.setText(this.message1.getText());
        }
        // Set 1
        this.message1.setText(formatMessage(messageBean));

        this.historical.add(messageBean);
        this.displaying = true;
        this.permanent = permanent;
        if (consoleLog) {
            Gdx.app.log(df.format(messageBean.date), msg);
        }
    }

    private String formatMessage(MessageBean msgBean) {
        return (writeDates ? df.format(msgBean.date) + TAG_SEPARATOR : "") + msgBean.msg;
    }

    public void update() {
        if (displaying && !permanent) {
            if (multiple && historical.size() > 1 && historical.get(historical.size() - 2).finished(msTimeout)) {
                message2.setText("");
            }

            if (historical.getLast().finished(msTimeout)) {
                displaying = false;
                message1.setText("");
            }

        }
    }

    @Override
    public void notify(Events event, Object... data) {
        synchronized (lock) {
            switch (event) {
            case POST_NOTIFICATION:
                String message = "";
                boolean perm = false;
                for (int i = 0; i < data.length; i++) {
                    if (i == data.length - 1 && data[i] instanceof Boolean) {
                        perm = (Boolean) data[i];
                    } else {
                        message += (String) data[i];
                        if (i < data.length - 1 && !(i == data.length - 2 && data[data.length - 1] instanceof Boolean)) {
                            message += TAG_SEPARATOR;
                        }
                    }
                }
                addMessage(message, perm);
                break;
            case FOCUS_CHANGED:
                if (data[0] != null) {
                    SceneGraphNode sgn = null;
                    if (data[0] instanceof String) {
                        sgn = GaiaSandbox.instance.sg.getNode((String) data[0]);
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
                if (data.length == 1) {
                    addMessage(I18n.bundle.format("notif.error", ((Throwable) data[0]).getLocalizedMessage()));
                } else {
                    addMessage(I18n.bundle.format("notif.error", data[1] + TAG_SEPARATOR + ((Throwable) data[0]).getLocalizedMessage()));
                }
                break;
            case ORBIT_DATA_LOADED:
                addMessage(I18n.bundle.format("notif.orbitdata.loaded", data[1], ((OrbitData) data[0]).getNumPoints()));
                break;
            case SCREENSHOT_INFO:
                addMessage(I18n.bundle.format("notif.screenshot", data[0]));
                break;
            case TOGGLE_STEREOSCOPIC:
            case TOGGLE_CLEANMODE:
                addMessage(I18n.bundle.format("notif.toggle", data[0]));
                break;
            case TOGGLE_STEREO_PROFILE:
                addMessage(I18n.bundle.format("notif.stereoscopic.profile", GlobalConf.program.STEREO_PROFILE.toString()));
                break;
            case FRAME_OUTPUT_CMD:
                if (data.length == 0) {
                    addMessage(I18n.bundle.format("notif.toggle", I18n.bundle.get("element.frameoutput")));
                } else {
                    boolean activated = (Boolean) data[0];
                    if (activated) {
                        addMessage(I18n.bundle.format("notif.activated", I18n.bundle.get("element.frameoutput")));
                    } else {
                        addMessage(I18n.bundle.format("notif.deactivated", I18n.bundle.get("element.frameoutput")));
                    }
                }
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

        /** Has the message finished given the timeout? **/
        public boolean finished(long timeout) {
            return new Date().getTime() - date.getTime() > timeout;
        }

        @Override
        public String toString() {
            return formatMessage(this);
        }
    }

}
