package gaia.cu9.ari.gaiaorbit.interfce.components;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextIconButton;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;

public class VisibilityComponent extends GuiComponent implements IObserver {
    protected Map<String, Button> buttonMap;
    /**
     * Entities that will go in the visibility check boxes
     */
    private ComponentType[] visibilityEntities;
    private boolean[] visible;

    public VisibilityComponent(Skin skin, Stage stage) {
        super(skin, stage);
        EventManager.instance.subscribe(this, Events.TOGGLE_VISIBILITY_CMD);
    }

    public void setVisibilityEntitites(ComponentType[] ve, boolean[] v) {
        visibilityEntities = ve;
        visible = v;
    }

    public void initialize() {
        final Table visibilityTable = new Table(skin);
        visibilityTable.setName("visibility table");
        buttonMap = new HashMap<String, Button>();
        Set<Button> buttons = new HashSet<Button>();
        if (visibilityEntities != null) {
            for (int i = 0; i < visibilityEntities.length; i++) {
                final ComponentType ct = visibilityEntities[i];
                final String name = ct.getName();

                Button button = null;
                if (ct.style != null) {
                    Image icon = new Image(skin.getDrawable(ct.style));
                    button = new OwnTextIconButton(name, icon, skin, "toggle");
                } else {
                    button = new OwnTextButton(name, skin, "toggle");
                }
                button.setName(name);

                buttonMap.put(name, button);
                if (!ct.toString().equals(name)) {
                    buttonMap.put(ct.toString(), button);
                }

                button.setChecked(visible[i]);
                button.addListener(new EventListener() {
                    @Override
                    public boolean handle(Event event) {
                        if (event instanceof ChangeEvent) {
                            EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, name, true, ((Button) event.getListenerActor()).isChecked());
                            return true;
                        }
                        return false;
                    }
                });
                visibilityTable.add(button).pad(1).align(Align.center);
                if (i % 2 != 0) {
                    visibilityTable.row();
                }
                buttons.add(button);
            }
        }
        // Set button width to max width
        visibilityTable.pack();
        float maxw = 0f;
        for (Button b : buttons) {
            if (b.getWidth() > maxw) {
                maxw = b.getWidth();
            }
        }
        for (Button b : buttons) {
            b.setSize(maxw, 20);
        }
        visibilityTable.pack();

        component = visibilityTable;
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case TOGGLE_VISIBILITY_CMD:
            boolean interf = (Boolean) data[1];
            if (!interf) {
                String name = (String) data[0];
                Button b = buttonMap.get(name);

                b.setProgrammaticChangeEvents(false);
                if (b != null) {
                    if (data.length == 3) {
                        b.setChecked((Boolean) data[2]);
                    } else {
                        b.setChecked(!b.isChecked());
                    }
                }
                b.setProgrammaticChangeEvents(true);
            }
            break;
        }

    }

}
