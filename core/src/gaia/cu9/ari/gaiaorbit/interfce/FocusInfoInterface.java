package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import java.text.DecimalFormat;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

public class FocusInfoInterface extends Table implements IObserver {

    protected OwnLabel focusName, focusRA, focusDEC, focusAngle, focusDist, focusAppMag, focusAbsMag, focusRadius;
    DecimalFormat format, sformat, format8;

    Vector3d pos;

    public FocusInfoInterface(Skin skin, DecimalFormat format, DecimalFormat sformat) {
	super(skin);
	this.format = format;
	this.sformat = sformat;
	this.format8 = new DecimalFormat("#####0.0#######");
	focusName = new OwnLabel("", skin, "header");
	focusRA = new OwnLabel("", skin, "hud");
	focusDEC = new OwnLabel("", skin, "hud");
	focusAppMag = new OwnLabel("", skin, "hud");
	focusAbsMag = new OwnLabel("", skin, "hud");
	focusAngle = new OwnLabel("", skin, "hud");
	focusDist = new OwnLabel("", skin, "hud");
	focusRadius = new OwnLabel("", skin, "hud");

	float w = 100;
	focusRA.setWidth(w);
	focusDEC.setWidth(w);
	focusAngle.setWidth(w);
	focusDist.setWidth(w);

	add(focusName).left().colspan(2);
	row();
	add(new OwnLabel("α", skin, "hud-big")).left();
	add(focusRA).left().padLeft(10);
	row();
	add(new OwnLabel("δ", skin, "hud-big")).left();
	add(focusDEC).left().padLeft(10);
	row();
	add(new OwnLabel("App mag", skin, "hud-big")).left();
	add(focusAppMag).left().padLeft(10);
	row();
	add(new OwnLabel("Abs mag", skin, "hud-big")).left();
	add(focusAbsMag).left().padLeft(10);
	row();
	add(new OwnLabel("Angle", skin, "hud-big")).left();
	add(focusAngle).left().padLeft(10);
	row();
	add(new OwnLabel("Dist", skin, "hud-big")).left();
	add(focusDist).left().padLeft(10);
	row();
	add(new OwnLabel("Radius", skin, "hud-big")).left();
	add(focusRadius).left().padLeft(10);
	pack();

	pos = new Vector3d();
	EventManager.getInstance().subscribe(this, Events.FOCUS_CHANGED, Events.FOCUS_INFO_UPDATED);
    }

    @Override
    public void notify(Events event, Object... data) {
	switch (event) {
	case FOCUS_CHANGED:
	    CelestialBody cb = null;
	    if (data[0] instanceof String) {
		cb = (CelestialBody) GaiaSandbox.getInstance().sg.getNode((String) data[0]);
	    } else {
		cb = (CelestialBody) data[0];
	    }
	    // Update focus information
	    String objectName = cb.name;
	    cb.getPosition(pos);

	    focusName.setText(objectName);
	    if (pos != null) {
		focusRA.setText(format.format(pos.x % 360) + "°");
		focusDEC.setText(format.format(pos.y % 360) + "°");
	    } else {
		focusRA.setText("");
		focusDEC.setText("");
	    }

	    Float appmag = cb.appmag;

	    if (appmag != null) {
		focusAppMag.setText(format.format(appmag));
	    } else {
		focusAppMag.setText("-");
	    }
	    Float absmag = cb.absmag;

	    if (absmag != null) {
		focusAbsMag.setText(format.format(absmag));
	    } else {
		focusAbsMag.setText("-");
	    }
	    focusRadius.setText(sformat.format(cb.getRadius() * Constants.U_TO_KM) + " Km");

	    break;
	case FOCUS_INFO_UPDATED:
	    focusAngle.setText(format8.format(Math.toDegrees((float) data[1]) % 360) + "°");
	    Object[] dist = GlobalResources.floatToDistanceString((float) data[0]);
	    focusDist.setText(sformat.format((float) Math.max(0d, (float) dist[0])) + " " + dist[1]);
	    break;
	}
    }

}
