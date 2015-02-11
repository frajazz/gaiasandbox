package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class DebugInterface extends Table implements IObserver {
    private OwnLabel debug1, debug2, debug3, fps;
    /** Lock object for synchronization **/
    private Object lock;

    public DebugInterface(Skin skin, Object lock) {
	super(skin);
	debug1 = new OwnLabel("", skin, "hud");
	add(debug1).right();
	row();

	debug2 = new OwnLabel("", skin, "hud");
	add(debug2).right();
	row();

	debug3 = new OwnLabel("", skin, "hud");
	add(debug3).right();
	row();

	fps = new OwnLabel("", skin, "hud");
	add(fps).right();
	row();
	this.setVisible(GlobalConf.inst.SHOW_DEBUG_INFO);
	this.lock = lock;
	EventManager.getInstance().subscribe(this, Events.DEBUG1, Events.DEBUG2, Events.DEBUG3, Events.FPS_INFO, Events.SHOW_DEBUG_CMD);
    }

    @Override
    public void notify(Events event, Object... data) {
	synchronized (lock) {
	    switch (event) {
	    case DEBUG1:
		if (data.length > 0 && data[0] != null)
		    debug1.setText((String) data[0]);
		break;

	    case DEBUG2:
		if (data.length > 0 && data[0] != null)
		    debug2.setText((String) data[0]);
		break;

	    case DEBUG3:
		if (data.length > 0 && data[0] != null)
		    debug3.setText((String) data[0]);
		break;
	    case FPS_INFO:
		if (data.length > 0 && data[0] != null)
		    fps.setText((Integer) data[0] + " FPS");
		break;
	    case SHOW_DEBUG_CMD:
		boolean shw;
		if (data.length >= 1) {
		    shw = (boolean) data[0];
		} else {
		    shw = !this.isVisible();
		}
		GlobalConf.inst.SHOW_DEBUG_INFO = shw;
		this.setVisible(GlobalConf.inst.SHOW_DEBUG_INFO);
		break;
	    }
	}
    }

}
