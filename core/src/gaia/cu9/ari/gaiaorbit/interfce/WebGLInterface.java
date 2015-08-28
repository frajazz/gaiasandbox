package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory.DateType;
import gaia.cu9.ari.gaiaorbit.util.format.IDateFormat;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnSlider;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

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
    protected Button switchFull, toggleLabels, plus, minus;

    int prevVal = 0;
    private IDateFormat df;

    public WebGLInterface(Skin skin, ITimeFrameProvider time) {
        super(skin);
        this.setBackground("table-bg-inv");
        this.pad(5);

        df = DateFormatFactory.getFormatter(I18n.locale, DateType.DATE);

        date = new OwnLabel(df.format(time.getTime()), skin, "default-inv");

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
        switchFull.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    // Remove webgl, add controls window
                    EventManager.instance.post(Events.REMOVE_GUI_COMPONENT, "webglInterface");
                    EventManager.instance.post(Events.ADD_GUI_COMPONENT, "controlsWindow");
                }
                return true;
            }
        });
        HorizontalGroup hg = new HorizontalGroup().space(3);
        hg.addActor(gaiaObsLabel);
        hg.addActor(slider);
        hg.addActor(sceneModeLabel);

        toggleLabels = new OwnTextButton(txt("action.toggle", txt("element.labels")), skin, "link");
        toggleLabels.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, txt("element.labels"), false);
                }
                return true;
            }
        });

        plus = new OwnTextButton("+", skin, "link");
        plus.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.STAR_BRIGHTNESS_CMD, GlobalConf.scene.STAR_BRIGHTNESS + 1f);
                }
                return true;
            }
        });

        minus = new OwnTextButton("-", skin, "link");
        minus.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.STAR_BRIGHTNESS_CMD, GlobalConf.scene.STAR_BRIGHTNESS - 1f);
                }
                return true;
            }
        });

        add(date).left();
        padBottom(10);
        row();
        if (!GlobalConf.runtime.STRIPPED_FOV_MODE) {
            add(hg).left();
            row();
            add(switchFull).left();
        } else {
            add(toggleLabels).left().padRight(5);
            add(minus).left().padRight(5);
            add(plus).left();
        }

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
            date.setText(df.format(time));
            break;
        }

    }

    private String txt(String key) {
        return I18n.bundle.get(key);
    }

    private String txt(String key, Object... args) {
        return I18n.bundle.format(key, args);
    }

}
