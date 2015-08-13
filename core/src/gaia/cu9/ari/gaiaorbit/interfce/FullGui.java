package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.components.VisualEffectsComponent;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;

import com.badlogic.gdx.Application.ApplicationType;
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

    protected ControlsWindow controls;

    protected SearchDialog searchDialog;
    protected VisualEffectsComponent visualEffectsWindow;

    protected Container<FocusInfoInterface> fi;
    protected FocusInfoInterface focusInterface;
    protected NotificationsInterface notificationsInterface;
    protected MessagesInterface messagesInterface;
    protected DebugInterface debugInterface;
    protected CustomInterface customInterface;
    protected Container<WebGLInterface> wgl;
    protected WebGLInterface webglInterface;

    protected ISceneGraph sg;
    private ComponentType[] visibilityEntities;
    private boolean[] visible;

    /** Lock object for synchronisation **/
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

        buildGui();

        // We must subscribe to the desired events
        EventManager.instance.subscribe(this, Events.FOV_CHANGED_CMD, Events.SHOW_TUTORIAL_ACTION, Events.SHOW_SEARCH_ACTION, Events.REMOVE_KEYBOARD_FOCUS);
    }

    private void buildGui() {
        // Component types name init
        for (ComponentType ct : ComponentType.values()) {
            ct.getName();
        }

        ApplicationType type = Gdx.app.getType();
        if (/**type.equals(ApplicationType.WebGL)**/
        true) {
            // WEBGL INTERFACE - TOP LEFT
            webglInterface = new WebGLInterface(skin);
            wgl = new Container<WebGLInterface>(webglInterface);
            wgl.setFillParent(true);
            wgl.left().top();
            wgl.pad(5, 5, 0, 0);

        } else {
            // CONTROLS WINDOW
            initControlsWindow();
        }

        // FOCUS INFORMATION - BOTTOM RIGHT
        focusInterface = new FocusInfoInterface(skin);
        //focusInterface.setFillParent(true);
        focusInterface.left().top();
        fi = new Container<FocusInfoInterface>(focusInterface);
        fi.setFillParent(true);
        fi.bottom().right();
        fi.pad(0, 0, 10, 10);

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

        // CUSTOM OBJECTS INTERFACE
        customInterface = new CustomInterface(ui, skin, lock);

        /** ADD TO UI **/
        rebuildGui();
        //controls.collapse();
    }

    private void initControlsWindow() {
        controls = new ControlsWindow(txt("gui.controls"), skin, ui);
        controls.setSceneGraph(sg);
        controls.setVisibilityToggles(visibilityEntities, visible);
        controls.initialize();
        controls.left();
        controls.getTitleTable().align(Align.left);
        controls.setFillParent(false);
        controls.setMovable(true);
        controls.setResizable(false);
        controls.padRight(5);
        controls.padBottom(5);
    }

    public void recalculateOptionsSize() {
        controls.recalculateSize();
    }

    private void rebuildGui() {

        if (ui != null) {
            ui.clear();
            boolean collapsed = false;
            if (controls != null) {
                collapsed = controls.isCollapsed();
                recalculateOptionsSize();
                if (collapsed)
                    controls.collapse();
                controls.setPosition(0, Gdx.graphics.getHeight() - controls.getHeight());
                ui.addActor(controls);
            }
            if (webglInterface != null)
                ui.addActor(wgl);
            if (debugInterface != null)
                ui.addActor(debugInterface);
            if (notificationsInterface != null)
                ui.addActor(notificationsInterface);
            if (messagesInterface != null)
                ui.addActor(messagesInterface);
            if (focusInterface != null)
                ui.addActor(fi);

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
                            if (controls != null) {
                                if (ie.getTarget().isDescendantOf(controls)) {
                                    Actor scrollPanelAncestor = getScrollPanelAncestor(ie.getTarget());
                                    ui.setScrollFocus(scrollPanelAncestor);
                                } else {
                                    ui.setScrollFocus(controls);
                                }
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
