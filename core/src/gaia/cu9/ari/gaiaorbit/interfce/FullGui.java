package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.components.CameraComponent;
import gaia.cu9.ari.gaiaorbit.interfce.components.GaiaComponent;
import gaia.cu9.ari.gaiaorbit.interfce.components.ObjectsComponent;
import gaia.cu9.ari.gaiaorbit.interfce.components.TimeComponent;
import gaia.cu9.ari.gaiaorbit.interfce.components.VisibilityComponent;
import gaia.cu9.ari.gaiaorbit.interfce.components.VisualEffectsComponent;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.scene2d.CollapsiblePane;
import gaia.cu9.ari.gaiaorbit.util.scene2d.CollapsibleWindow;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnImageButton;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnScrollPane;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;
import gaia.cu9.ari.gaiaorbit.util.scene2d.Tooltip;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
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

    protected OwnImageButton recCamera, playCamera, playstop;

    protected CollapsibleWindow options;
    protected VerticalGroup mainVertical;
    protected OwnScrollPane windowScroll;
    protected Table guiLayout;

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
    private DecimalFormat format, sformat;

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
        format = new DecimalFormat("0.0###");
        sformat = new DecimalFormat("0.###E0");

        buildGui();

        // We must subscribe to the desired events
        EventManager.instance.subscribe(this, Events.FOV_CHANGED_CMD, Events.TOGGLE_TIME_CMD, Events.CAMERA_MODE_CMD, Events.SHOW_TUTORIAL_ACTION, Events.SHOW_SEARCH_ACTION, Events.GUI_SCROLL_POSITION_CMD, Events.GUI_FOLD_CMD, Events.GUI_MOVE_CMD, Events.RECALCULATE_OPTIONS_SIZE, Events.REMOVE_KEYBOARD_FOCUS);
    }

    private void buildGui() {
        /** Global resources **/
        TextureRegion septexreg = ((TextureRegionDrawable) skin.newDrawable("separator")).getRegion();
        septexreg.getTexture().setWrap(TextureWrap.Repeat, TextureWrap.ClampToEdge);
        TiledDrawable separator = new TiledDrawable(septexreg);

        /** The Options window **/
        options = new CollapsibleWindow(txt("gui.controls"), skin);
        options.left();
        options.getTitleTable().align(Align.left);
        options.setFillParent(false);
        options.setMovable(true);
        options.setResizable(false);
        options.padRight(5);
        options.padBottom(5);

        /** Global layout **/
        guiLayout = new Table();
        guiLayout.align(Align.left);

        List<Actor> mainActors = new ArrayList<Actor>();

        /** ----TIME GROUP---- **/
        playstop = new OwnImageButton(skin, "playstop");
        playstop.setName("play stop");
        playstop.setChecked(GlobalConf.runtime.TIME_ON);
        playstop.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.TOGGLE_TIME_CMD, playstop.isChecked(), true);
                    return true;
                }
                return false;
            }
        });
        Label playstopTooltip = new Label(txt("gui.tooltip.playstop"), skin, "tooltip");
        playstop.addListener(new Tooltip<Label>(playstopTooltip, ui));

        TimeComponent timeComponent = new TimeComponent(skin, ui);
        timeComponent.initialize();

        CollapsiblePane time = new CollapsiblePane(ui, txt("gui.time"), timeComponent.getActor(), skin, playstop);
        time.align(Align.left);
        mainActors.add(time);

        /** ----CAMERA---- **/
        // Record camera button
        recCamera = new OwnImageButton(skin, "rec");
        recCamera.setName("recCam");
        recCamera.setChecked(GlobalConf.runtime.RECORD_CAMERA);
        recCamera.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.RECORD_CAMERA_CMD, recCamera.isChecked(), true);
                    return true;
                }
                return false;
            }
        });
        Label recTooltip = new Label(txt("gui.tooltip.reccamera"), skin, "tooltip");
        recCamera.addListener(new Tooltip<Label>(recTooltip, ui));

        // Play camera button
        playCamera = new OwnImageButton(skin, "play");
        playCamera.setName("playCam");
        playCamera.setChecked(false);
        playCamera.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.SHOW_PLAYCAMERA_ACTION);
                    return true;
                }
                return false;
            }
        });

        Label playTooltip = new Label(txt("gui.tooltip.playcamera"), skin, "tooltip");
        playCamera.addListener(new Tooltip<Label>(playTooltip, ui));

        CameraComponent cameraComponent = new CameraComponent(skin, ui);
        cameraComponent.initialize();

        CollapsiblePane camera = new CollapsiblePane(ui, txt("gui.camera"), cameraComponent.getActor(), skin, recCamera, playCamera);
        camera.align(Align.left);
        mainActors.add(camera);

        /** ----OBJECTS TREE---- **/
        ObjectsComponent objectsComponent = new ObjectsComponent(skin, ui);
        objectsComponent.setSceneGraph(sg);
        objectsComponent.initialize();

        CollapsiblePane objects = new CollapsiblePane(ui, txt("gui.objects"), objectsComponent.getActor(), skin);
        objects.align(Align.left);
        mainActors.add(objects);

        /** ----OBJECT TOGGLES GROUP---- **/
        VisibilityComponent visibilityComponent = new VisibilityComponent(skin, ui);
        visibilityComponent.setVisibilityEntitites(visibilityEntities, visible);
        visibilityComponent.initialize();

        CollapsiblePane visibility = new CollapsiblePane(ui, txt("gui.visibility"), visibilityComponent.getActor(), skin);
        visibility.align(Align.left);
        mainActors.add(visibility);

        /** ----LIGHTING GROUP---- **/
        VisualEffectsComponent visualEffectsComponent = new VisualEffectsComponent(skin, ui);
        visualEffectsComponent.initialize();

        CollapsiblePane visualEffects = new CollapsiblePane(ui, txt("gui.lighting"), visualEffectsComponent.getActor(), skin);
        visualEffects.align(Align.left);
        mainActors.add(visualEffects);

        /** ----GAIA SCAN GROUP---- **/
        GaiaComponent gaiaComponent = new GaiaComponent(skin, ui);
        gaiaComponent.initialize();

        CollapsiblePane gaia = new CollapsiblePane(ui, txt("gui.gaiascan"), gaiaComponent.getActor(), skin);
        gaia.align(Align.left);
        mainActors.add(gaia);

        /** BUTTONS **/
        Button preferences = new OwnTextButton(txt("gui.preferences"), skin);
        preferences.setName("preferences");
        preferences.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.SHOW_PREFERENCES_ACTION);
                }
                return false;
            }
        });
        Button tutorial = new OwnTextButton(txt("gui.tutorial"), skin);
        tutorial.setName("tutorial");
        tutorial.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.SHOW_TUTORIAL_ACTION);
                }
                return false;
            }
        });
        Button about = new OwnTextButton(txt("gui.help"), skin);
        about.setName("about");
        about.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.SHOW_ABOUT_ACTION);
                }
                return false;
            }
        });
        Button runScript = new OwnTextButton(txt("gui.script.runscript"), skin);
        runScript.setName("run script");
        runScript.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.SHOW_RUNSCRIPT_ACTION);
                }
                return false;
            }
        });

        /** ADD GROUPS TO VERTICAL LAYOUT **/
        int pad = 10;
        int size = mainActors.size();
        for (int i = 0; i < size; i++) {
            Actor actor = mainActors.get(i);
            guiLayout.add(actor).left().padBottom(pad);
            if (i < size - 1) {
                // Not last
                guiLayout.row();
                guiLayout.add(new Image(separator)).left().fill(true, false);
                guiLayout.row();
            }
        }
        guiLayout.layout();
        guiLayout.pack();

        windowScroll = new OwnScrollPane(guiLayout, skin, "minimalist-nobg");
        windowScroll.setFadeScrollBars(true);
        windowScroll.setScrollingDisabled(true, false);
        windowScroll.setOverscroll(false, false);
        windowScroll.setSmoothScrolling(true);
        windowScroll.pack();
        windowScroll.setWidth(guiLayout.getWidth() + windowScroll.getStyle().vScroll.getMinWidth());

        Table buttonsTable = new Table(skin);
        buttonsTable.add(runScript).pad(1).top().left();
        buttonsTable.add(preferences).pad(1).top().left();
        buttonsTable.row();
        buttonsTable.add(tutorial).pad(1).top().left();
        buttonsTable.add(about).pad(1).top().left();

        int buttonwidth = 85;
        int buttonheight = 20;
        runScript.setSize(buttonwidth, buttonheight);
        preferences.setSize(buttonwidth, buttonheight);
        tutorial.setSize(buttonwidth, buttonheight);
        about.setSize(buttonwidth, buttonheight);
        buttonsTable.pack();

        mainVertical = new VerticalGroup();
        mainVertical.space(5f);
        mainVertical.align(Align.right).align(Align.top);
        mainVertical.addActor(windowScroll);
        mainVertical.addActor(buttonsTable);
        mainVertical.pack();

        /** ADD TO MAIN WINDOW **/
        options.add(mainVertical).top().left().expand();
        options.setPosition(0, Gdx.graphics.getHeight() - options.getHeight());

        options.setWidth(mainVertical.getWidth());
        options.pack();

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
        options.collapse();
    }

    public void recalculateOptionsSize() {
        // Save position
        float topy = options.getY() + options.getHeight();

        // Calculate new size
        guiLayout.pack();
        windowScroll.setHeight(Math.min(guiLayout.getHeight(), Gdx.graphics.getHeight() - 100));
        windowScroll.pack();

        mainVertical.setHeight(windowScroll.getHeight() + 30);
        mainVertical.pack();

        options.setHeight(windowScroll.getHeight() + 40);
        options.pack();
        options.validate();

        // Restore position
        options.setY(topy - options.getHeight());
    }

    private void rebuildGui() {

        if (ui != null) {
            ui.clear();
            boolean collapsed = false;
            if (options != null) {
                collapsed = options.isCollapsed();
                recalculateOptionsSize();
                if (collapsed)
                    options.collapse();
                options.setPosition(0, Gdx.graphics.getHeight() - options.getHeight());
                ui.addActor(options);
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
                            if (ie.getTarget().isDescendantOf(options)) {
                                Actor scrollPanelAncestor = getScrollPanelAncestor(ie.getTarget());
                                ui.setScrollFocus(scrollPanelAncestor);
                            } else {
                                ui.setScrollFocus(options);

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
        case TOGGLE_TIME_CMD:
            // Pause has been toggled, update playstop button only if this does not come from this interface
            if (!(Boolean) data[1]) {
                Boolean timeOn = null;
                if (data[0] != null) {
                    timeOn = (Boolean) data[0];
                } else {
                    timeOn = !playstop.isChecked();
                }
                playstop.setCheckedNoFire(timeOn);
            }
            break;
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
        case GUI_SCROLL_POSITION_CMD:
            this.windowScroll.setScrollY((float) data[0]);
            break;
        case GUI_FOLD_CMD:
            boolean collapse;
            if (data.length >= 1) {
                collapse = (boolean) data[0];
            } else {
                // Toggle
                collapse = !options.isCollapsed();
            }
            if (collapse) {
                options.collapse();
            } else {
                options.expand();
            }
            break;
        case GUI_MOVE_CMD:
            float x = (float) data[0];
            float y = (float) data[1];
            float width = Gdx.graphics.getWidth();
            float height = Gdx.graphics.getHeight();
            float windowWidth = options.getWidth();
            float windowHeight = options.getHeight();

            x = MathUtilsd.clamp(x * width, 0, width - windowWidth);
            y = MathUtilsd.clamp(y * height - windowHeight, 0, height - windowHeight);

            options.setPosition(x, y);

            break;
        case RECALCULATE_OPTIONS_SIZE:
            recalculateOptionsSize();
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
