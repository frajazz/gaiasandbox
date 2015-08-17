package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.components.VisualEffectsComponent;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.format.INumberFormat;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Full OpenGL GUI with all the controls and whistles.
 * @author Toni Sagrista
 *
 */
public class FullGui implements IGui, IObserver {
    private Skin skin;
    /**
     * The user interface stage
     */
    protected Stage ui;

    protected ControlsWindow optionsWindow;

    protected SearchDialog searchDialog;
    protected VisualEffectsComponent visualEffectsWindow;

    protected Container<FocusInfoInterface> fi;
    protected FocusInfoInterface focusInterface;
    protected NotificationsInterface notificationsInterface;
    protected MessagesInterface messagesInterface;
    protected DebugInterface debugInterface;
    protected ScriptStateInterface inputInterface;
    protected CustomInterface customInterface;

    /**
     * Number formats
     */
    private INumberFormat format, sformat;

    /**
     * The scene graph
     */
    private ISceneGraph sg;

    /**
     * Entities that will go in the visibility check boxes
     */
    private ComponentType[] visibilityEntities;
    private boolean[] visible;

    /** Lock object for synchronization **/
    private Object lock;

    public void setSceneGraph(ISceneGraph sg) {
        this.sg = sg;
    }

    public void setVisibilityToggles(ComponentType[] entities, boolean[] visible) {
        this.visibilityEntities = entities;
        this.visible = visible;
    }

    public void initialize(AssetManager assetManager) {
        // User interface
        ui = new Stage(new ScreenViewport(), GlobalResources.spriteBatch);
        lock = new Object();
    }

    /**
     * Constructs the interface
     */
    public void doneLoading(AssetManager assetManager) {
        Logger.info(txt("notif.gui.init"));

        skin = GlobalResources.skin;
        format = NumberFormatFactory.getFormatter("0.0###");
        sformat = NumberFormatFactory.getFormatter("0.###E0");

        buildGui();

        // We must subscribe to the desired events
        EventManager.instance.subscribe(this, Events.FOV_CHANGED_CMD, Events.CAMERA_MODE_CMD, Events.SHOW_TUTORIAL_ACTION, Events.SHOW_SEARCH_ACTION, Events.REMOVE_KEYBOARD_FOCUS);
    }

    private void buildGui() {
        /** OPTIONS WINDOW **/
        optionsWindow = new ControlsWindow(txt("gui.controls"), skin, ui);
        optionsWindow.setVisibilityToggles(visibilityEntities, visible);
        optionsWindow.setSceneGraph(sg);
        optionsWindow.initialize();
        optionsWindow.left();
        optionsWindow.getTitleTable().align(Align.left);
        optionsWindow.setFillParent(false);
        optionsWindow.setMovable(true);
        optionsWindow.setResizable(false);
        optionsWindow.padRight(5);
        optionsWindow.padBottom(5);

        // FOCUS INFORMATION - BOTTOM RIGHT
        focusInterface = new FocusInfoInterface(skin, format, sformat);
        //focusInterface.setFillParent(true);
        focusInterface.left().top();
        fi = new Container<FocusInfoInterface>(focusInterface);
        fi.setFillParent(true);
        fi.bottom().right();
        fi.padBottom(10).padRight(10);

        // DEBUG INFO - TOP RIGHT
        debugInterface = new DebugInterface(skin, lock);
        debugInterface.setFillParent(true);
        debugInterface.right().top();
        debugInterface.pad(5, 0, 0, 5);

        // NOTIFICATIONS INTERFACE - BOTTOM LEFT
        notificationsInterface = new NotificationsInterface(skin, lock, true);
        notificationsInterface.setFillParent(true);
        notificationsInterface.left().bottom();
        notificationsInterface.pad(0, 5, 5, 0);

        // MESSAGES INTERFACE - LOW CENTER
        messagesInterface = new MessagesInterface(skin, lock);
        messagesInterface.setFillParent(true);
        messagesInterface.left().bottom();
        messagesInterface.pad(0, 300, 150, 0);

        // INPUT STATE
        inputInterface = new ScriptStateInterface(skin);
        inputInterface.setFillParent(true);
        inputInterface.right().top();
        inputInterface.pad(100, 0, 0, 5);

        // CUSTOM OBJECTS INTERFACE
        customInterface = new CustomInterface(ui, skin, lock);

        /** ADD TO UI **/
        rebuildGui();
        optionsWindow.collapse();
    }

    public void recalculateOptionsSize() {
        optionsWindow.recalculateSize();

    }

    private void rebuildGui() {

        if (ui != null) {
            ui.clear();
            boolean collapsed = false;
            if (optionsWindow != null) {
                collapsed = optionsWindow.isCollapsed();
                recalculateOptionsSize();
                if (collapsed)
                    optionsWindow.collapse();
                optionsWindow.setPosition(0, Gdx.graphics.getHeight() - optionsWindow.getHeight());
                ui.addActor(optionsWindow);
            }
            if (debugInterface != null)
                ui.addActor(debugInterface);
            if (notificationsInterface != null)
                ui.addActor(notificationsInterface);
            if (messagesInterface != null)
                ui.addActor(messagesInterface);
            if (focusInterface != null)
                ui.addActor(fi);
            if (inputInterface != null) {
                ui.addActor(inputInterface);
            }
            if (customInterface != null) {
                customInterface.reAddObjects();
            }

            /** CAPTURE SCROLL FOCUS **/
            ui.addListener(new EventListener() {

                @Override
                public boolean handle(Event event) {
                    if (event instanceof InputEvent) {
                        InputEvent ie = (InputEvent) event;

                        if (ie.getType() == Type.mouseMoved) {
                            if (ie.getTarget().isDescendantOf(optionsWindow)) {
                                Actor scrollPanelAncestor = getScrollPanelAncestor(ie.getTarget());
                                ui.setScrollFocus(scrollPanelAncestor);
                            } else {
                                ui.setScrollFocus(optionsWindow);

                            }
                        } else if (ie.getType() == Type.touchDown) {
                            if (ie.getTarget() instanceof TextField)
                                ui.setKeyboardFocus(ie.getTarget());
                        }
                    }
                    return false;
                }

                private Actor getScrollPanelAncestor(Actor actor) {
                    if (actor == null) {
                        return null;
                    } else if (actor instanceof ScrollPane) {
                        return actor;
                    } else {
                        return getScrollPanelAncestor(actor.getParent());
                    }
                }

            });
        }
    }

    /**
     * Removes the focus from this Gui and returns true if the focus was in the GUI, false otherwise.
     * @return true if the focus was in the GUI, false otherwise.
     */
    public boolean cancelTouchFocus() {
        if (ui.getScrollFocus() != null) {
            ui.setScrollFocus(null);
            ui.setKeyboardFocus(null);
            return true;
        }
        return false;
    }

    public Stage getGuiStage() {
        return ui;
    }

    public void dispose() {
        ui.dispose();
    }

    public void update(float dt) {
        ui.act(dt);
        notificationsInterface.update();
    }

    public void render() {
        synchronized (lock) {
            ui.draw();
        }
    }

    public String getName() {
        return "GUI";
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case CAMERA_MODE_CMD:
            // Update camera mode selection
            CameraMode mode = (CameraMode) data[0];
            if (mode.equals(CameraMode.Focus)) {
                focusInterface.displayFocusInfo();
            } else {
                focusInterface.hideFocusInfo();
            }
            break;
        case SHOW_TUTORIAL_ACTION:
            EventManager.instance.post(Events.RUN_SCRIPT_PATH, GlobalConf.program.TUTORIAL_SCRIPT_LOCATION);
            break;
        case SHOW_SEARCH_ACTION:
            if (searchDialog == null) {
                searchDialog = new SearchDialog(this, skin, sg);
            } else {
                searchDialog.clearText();
            }
            searchDialog.display();
            break;
        case REMOVE_KEYBOARD_FOCUS:
            ui.setKeyboardFocus(null);
            break;
        }

    }

    @Override
    public void resize(final int width, final int height) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                ui.getViewport().update(width, height, true);
                rebuildGui();
            }
        });
    }

    /**
     * Small override that returns the user set width as preferred width.
     * @author Toni Sagrista
     *
     */
    private class OwnTextField extends TextField {

        public OwnTextField(String text, Skin skin) {
            super(text, skin);
        }

        @Override
        public float getPrefWidth() {
            return getWidth() > 0 ? getWidth() : 150;
        }

    }

    @Override
    public Actor findActor(String name) {
        return ui.getRoot().findActor(name);
    }

    private String txt(String key) {
        return I18n.bundle.get(key);
    }

    private String txt(String key, Object... params) {
        return I18n.bundle.format(key, params);
    }
}
