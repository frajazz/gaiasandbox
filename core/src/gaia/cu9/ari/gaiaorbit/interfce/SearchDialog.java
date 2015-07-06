package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;

public class SearchDialog extends Window {
    private final Window me;
    private final IGui gui;
    private final TextField searchInput;

    public SearchDialog(IGui gui, Skin skin, final ISceneGraph sg) {
        super("Search", skin);
        this.me = this;
        this.gui = gui;
        searchInput = new TextField("", skin);
        searchInput.setMessageText(I18n.bundle.get("gui.objects.search"));
        searchInput.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    InputEvent ie = (InputEvent) event;
                    if (ie.getType() == Type.keyUp) {
                        if (ie.getKeyCode() == Keys.ESCAPE || ie.getKeyCode() == Keys.ENTER) {
                            me.remove();
                        } else {
                            String text = searchInput.getText();
                            if (sg.containsNode(text.toLowerCase())) {
                                SceneGraphNode node = sg.getNode(text.toLowerCase());
                                if (node instanceof CelestialBody) {
                                    EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Focus, true);
                                    EventManager.instance.post(Events.FOCUS_CHANGE_CMD, node, true);
                                    searchInput.selectAll();
                                }
                            }
                        }

                        GaiaInputController.pressedKeys.remove(ie.getKeyCode());
                    }
                }
                return false;
            }
        });

        HorizontalGroup buttonGroup = new HorizontalGroup();
        TextButton cls = new OwnTextButton(I18n.bundle.get("gui.close"), skin, "default");
        cls.setName("close");
        cls.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    me.remove();
                    return true;
                }

                return false;
            }

        });
        buttonGroup.addActor(cls);
        cls.setSize(70, 20);
        buttonGroup.align(Align.right).space(10);

        add(searchInput).top().left().expand().row();
        add(buttonGroup).pad(5, 0, 0, 0).bottom().right().expand();
        getTitleTable().align(Align.left);
        setModal(false);
        pack();

        this.setPosition(gui.getGuiStage().getWidth() / 2f - this.getWidth() / 2f, gui.getGuiStage().getHeight() / 2f - this.getHeight() / 2f);

    }

    public void clearText() {
        searchInput.setText("");
    }

    public void display() {
        if (!gui.getGuiStage().getActors().contains(me, true))
            gui.getGuiStage().addActor(this);
        gui.getGuiStage().setKeyboardFocus(searchInput);
    }
}
