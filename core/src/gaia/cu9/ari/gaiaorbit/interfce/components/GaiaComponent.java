package gaia.cu9.ari.gaiaorbit.interfce.components;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class GaiaComponent extends GuiComponent {

    protected CheckBox transitColor, onlyObservedStars, computeGaiaScan;

    public GaiaComponent(Skin skin, Stage stage) {
        super(skin, stage);
    }

    @Override
    public void initialize() {
        computeGaiaScan = new CheckBox(txt("gui.gaiascan.enable"), skin);
        computeGaiaScan.setName("compute gaia scan");
        computeGaiaScan.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.COMPUTE_GAIA_SCAN_CMD, txt("gui.gaiascan.compute"), computeGaiaScan.isChecked());
                    return true;
                }
                return false;
            }
        });
        computeGaiaScan.setChecked(GlobalConf.scene.COMPUTE_GAIA_SCAN);

        transitColor = new CheckBox(txt("gui.gaiascan.colour"), skin);
        transitColor.setName("transit color");
        transitColor.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.TRANSIT_COLOUR_CMD, txt("gui.gaiascan.transit"), transitColor.isChecked());
                    return true;
                }
                return false;
            }
        });
        transitColor.setChecked(GlobalConf.scene.STAR_COLOR_TRANSIT);

        onlyObservedStars = new CheckBox(txt("gui.gaiascan.onlyobserved"), skin);
        onlyObservedStars.setName("only observed stars");
        onlyObservedStars.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.ONLY_OBSERVED_STARS_CMD, txt("gui.gaiascan.only"), onlyObservedStars.isChecked());
                    return true;
                }
                return false;
            }
        });
        onlyObservedStars.setChecked(GlobalConf.scene.ONLY_OBSERVED_STARS);

        VerticalGroup gaiaGroup = new VerticalGroup().align(Align.left);
        gaiaGroup.addActor(computeGaiaScan);
        gaiaGroup.addActor(transitColor);
        gaiaGroup.addActor(onlyObservedStars);

        component = gaiaGroup;

    }

}
