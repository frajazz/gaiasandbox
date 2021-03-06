package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.format.INumberFormat;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;

public class FocusInfoInterface extends Table implements IObserver {

    protected OwnLabel focusName, focusId, focusRA, focusDEC, focusAngle, focusDist, focusAppMag, focusAbsMag, focusRadius;
    protected OwnLabel camName, camVel, camPos;

    private Table focusInfo, cameraInfo;

    Vector3d pos;

    INumberFormat nf, sf;

    public FocusInfoInterface(Skin skin) {
        super(skin);
        this.setBackground("table-bg");

        nf = NumberFormatFactory.getFormatter("#0.###");
        sf = NumberFormatFactory.getFormatter("##0.###E0");

        focusInfo = new Table();
        focusInfo.pad(5);
        cameraInfo = new Table();
        cameraInfo.pad(5);

        focusName = new OwnLabel("", skin, "hud-header");
        focusId = new OwnLabel("", skin, "hud");
        focusRA = new OwnLabel("", skin, "hud");
        focusDEC = new OwnLabel("", skin, "hud");
        focusAppMag = new OwnLabel("", skin, "hud");
        focusAbsMag = new OwnLabel("", skin, "hud");
        focusAngle = new OwnLabel("", skin, "hud");
        focusDist = new OwnLabel("", skin, "hud");
        focusRadius = new OwnLabel("", skin, "hud");

        camName = new OwnLabel(I18n.bundle.get("gui.camera"), skin, "hud-header");
        camVel = new OwnLabel("", skin, "hud");
        camPos = new OwnLabel("", skin, "hud");

        float w = 100;
        focusId.setWidth(w);
        focusRA.setWidth(w);
        focusDEC.setWidth(w);
        focusAngle.setWidth(w);
        focusDist.setWidth(w);
        camVel.setWidth(w);

        focusInfo.add(focusName).left().colspan(2);
        focusInfo.row();
        focusInfo.add(new OwnLabel("ID", skin, "hud-big")).left();
        focusInfo.add(focusId).left().padLeft(10);
        focusInfo.row();
        focusInfo.add(new OwnLabel(txt("gui.focusinfo.alpha"), skin, "hud-big")).left();
        focusInfo.add(focusRA).left().padLeft(10);
        focusInfo.row();
        focusInfo.add(new OwnLabel(txt("gui.focusinfo.delta"), skin, "hud-big")).left();
        focusInfo.add(focusDEC).left().padLeft(10);
        focusInfo.row();
        focusInfo.add(new OwnLabel(txt("gui.focusinfo.appmag"), skin, "hud-big")).left();
        focusInfo.add(focusAppMag).left().padLeft(10);
        focusInfo.row();
        focusInfo.add(new OwnLabel(txt("gui.focusinfo.absmag"), skin, "hud-big")).left();
        focusInfo.add(focusAbsMag).left().padLeft(10);
        focusInfo.row();
        focusInfo.add(new OwnLabel(txt("gui.focusinfo.angle"), skin, "hud-big")).left();
        focusInfo.add(focusAngle).left().padLeft(10);
        focusInfo.row();
        focusInfo.add(new OwnLabel(txt("gui.focusinfo.distance"), skin, "hud-big")).left();
        focusInfo.add(focusDist).left().padLeft(10);
        focusInfo.row();
        focusInfo.add(new OwnLabel(txt("gui.focusinfo.radius"), skin, "hud-big")).left();
        focusInfo.add(focusRadius).left().padLeft(10).padBottom(5);

        cameraInfo.add(camName).left().colspan(2);
        cameraInfo.row();
        cameraInfo.add(new OwnLabel(txt("gui.camera.vel"), skin, "hud-big")).left();
        cameraInfo.add(camVel).left().padLeft(10);
        cameraInfo.row();
        cameraInfo.add(camPos).left().colspan(2);

        add(focusInfo);
        row();
        add(cameraInfo);
        pack();

        pos = new Vector3d();
        EventManager.instance.subscribe(this, Events.FOCUS_CHANGED, Events.FOCUS_INFO_UPDATED, Events.CAMERA_MOTION_UPDATED, Events.CAMERA_MODE_CMD);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case FOCUS_CHANGED:
            CelestialBody cb = null;
            if (data[0] instanceof String) {
                cb = (CelestialBody) GaiaSandbox.instance.sg.getNode((String) data[0]);
            } else {
                cb = (CelestialBody) data[0];
            }

            String id = "";
            if (cb instanceof Star) {
                Star s = (Star) cb;
                if (s.hip >= 0) {
                    id = "HIP " + s.hip;
                } else if (s.tycho >= 0) {
                    id = "TYC " + s.tycho;
                } else {
                    id = "" + s.id;
                }
            } else {
                id = "-";
            }
            focusId.setText(id);

            // Update focus information
            String objectName = cb.name;

            focusName.setText(objectName);
            if (cb.posSph != null && cb.posSph.len() > 0f) {
                focusRA.setText(nf.format(cb.posSph.x) + "°");
                focusDEC.setText(nf.format(cb.posSph.y) + "°");
            } else {
                Coordinates.cartesianToSpherical(cb.pos, pos);

                focusRA.setText(nf.format(cb.pos.x % 360) + "°");
                focusDEC.setText(nf.format(cb.pos.y % 360) + "°");
            }

            Float appmag = cb.appmag;

            if (appmag != null) {
                focusAppMag.setText(nf.format(appmag));
            } else {
                focusAppMag.setText("-");
            }
            Float absmag = cb.absmag;

            if (absmag != null) {
                focusAbsMag.setText(nf.format(absmag));
            } else {
                focusAbsMag.setText("-");
            }
            focusRadius.setText(sf.format(cb.getRadius() * Constants.U_TO_KM) + " km");

            break;
        case FOCUS_INFO_UPDATED:
            focusAngle.setText(sf.format(Math.toDegrees((float) data[1]) % 360) + "°");
            Object[] dist = GlobalResources.floatToDistanceString((float) data[0]);
            focusDist.setText(sf.format(Math.max(0d, (float) dist[0])) + " " + dist[1]);
            break;
        case CAMERA_MOTION_UPDATED:
            Vector3d campos = (Vector3d) data[0];
            camPos.setText("X: " + nf.format(campos.x * Constants.U_TO_PC) + " pc\nY: " + nf.format(campos.y * Constants.U_TO_PC) + " pc\nZ: " + nf.format(campos.z * Constants.U_TO_PC) + " pc");
            camVel.setText(sf.format((double) data[1]) + " km/h");
            break;
        case CAMERA_MODE_CMD:
            // Update camera mode selection
            CameraMode mode = (CameraMode) data[0];
            if (mode.equals(CameraMode.Focus)) {
                displayFocusInfo();
            } else {
                hideFocusInfo();
            }
            break;
        }
    }

    private String txt(String key) {
        return I18n.bundle.get(key);
    }

    public void displayFocusInfo() {
        this.clearChildren();
        add(focusInfo);
        row();
        add(cameraInfo);
        pack();
    }

    public void hideFocusInfo() {
        this.clearChildren();
        add(cameraInfo);
        pack();
    }
}
