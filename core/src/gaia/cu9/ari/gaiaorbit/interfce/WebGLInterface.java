package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnSlider;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;

import java.util.Date;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class WebGLInterface extends Table implements IObserver {

    protected OwnLabel date;
    protected OwnLabel gaiaObsLabel, sceneModeLabel;
    protected Slider slider;
    protected Button switchFull;

    int prevVal = 0;

    public WebGLInterface(Skin skin) {
        super(skin);
        this.setBackground("table-bg-inv");
        this.pad(5);

        date = new OwnLabel("", skin, "default-inv");

        gaiaObsLabel = new OwnLabel(txt("gui.webgl.gaiaobs"), skin, "default-inv");
        sceneModeLabel = new OwnLabel(txt("gui.webgl.scenemode"), skin, "default-inv");
        slider = new OwnSlider(0, 1, 1, false, skin);
        slider.setWidth(20);
        slider.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent && ((int) slider.getValue()) != prevVal) {
                    int val = (int) slider.getValue();
                    switch (val) {
                    case 0:
                        // Gaia FoV view
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Gaia_FOV1and2);
                                EventManager.instance.post(Events.AMBIENT_LIGHT_CMD, 0f);
                            }
                        });
                        break;
                    case 1:
                        // Scene mode
                        Gdx.app.postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Gaia_Scene);
                                EventManager.instance.post(Events.GO_TO_OBJECT_CMD);
                                EventManager.instance.post(Events.AMBIENT_LIGHT_CMD, .7f);
                            }
                        });

                        break;
                    }
                }
                prevVal = (int) slider.getValue();
                return true;
            }
        });

        switchFull = new OwnTextButton(txt("gui.webgl.switchfull"), skin, "link");

        HorizontalGroup hg = new HorizontalGroup().space(3);
        hg.addActor(gaiaObsLabel);
        hg.addActor(slider);
        hg.addActor(sceneModeLabel);

        add(date).left().padBottom(10);
        row();
        add(hg).left();
        row();
        add(switchFull).left();

        pack();

        EventManager.instance.subscribe(this, Events.TIME_CHANGE_INFO, Events.TIME_CHANGE_CMD);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case TIME_CHANGE_CMD:
        case TIME_CHANGE_INFO:
            // Update input time
            Date time = (Date) data[0];
            date.setText(time.toString());
            break;
        }

    }

    private String txt(String key) {
        return I18n.bundle.get(key);
    }

}
