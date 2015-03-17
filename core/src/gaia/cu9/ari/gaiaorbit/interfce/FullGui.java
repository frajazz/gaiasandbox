package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.TwoWayHashmap;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.scene2d.CollapsibleWindow;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnImageButton;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnScrollPane;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextIconButton;
import gaia.cu9.ari.gaiaorbit.util.scene2d.Tooltip;
import gaia.cu9.ari.gaiaorbit.util.time.GlobalClock;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Full OpenGL GUI with all the controls and whistles.
 * @author Toni Sagrista
 *
 */
public class FullGui implements IGui, IObserver {
    /** Horizontal padding for groups **/
    private static final int HPADDING = 10;

    boolean tree = false;
    boolean list = true;

    private Skin skin;
    /**
     * The user interface stage	    
     */
    protected Stage ui;
    /**
     * The gaia text field
     */
    protected OwnLabel fov, brightness, bloom, ambient, speed, turn, rotate, date;
    protected Actor objectsList;
    protected SelectBox<String> cameraMode, cameraSpeedLimit;
    protected TextField inputPace, searchBox;
    protected Button plus, minus;
    protected ImageButton dateEdit;
    protected OwnImageButton playstop, recCamera;
    protected OwnScrollPane focusListScrollPane;
    protected Slider fieldOfView, starBrightness, bloomEffect, ambientLight, cameraSpeed, turnSpeed, rotateSpeed;
    protected CheckBox focusLock, transitColor, onlyObservedStars, computeGaiaScan, lensFlare;

    protected CollapsibleWindow options;
    protected VerticalGroup mainVertical;
    protected OwnScrollPane windowScroll;
    protected Table guiLayout;

    protected SearchDialog searchDialog;
    protected DateDialog dateDialog;

    protected Container<FocusInfoInterface> fi;
    protected FocusInfoInterface focusInterface;
    protected NotificationsInterface notificationsInterface;
    protected MessagesInterface messagesInterface;
    protected DebugInterface debugInterface;
    protected ScriptStateInterface inputInterface;
    protected CustomInterface customInterface;

    protected List<Actor> tooltips;

    protected Map<String, Button> buttonMap;

    /**
     * Tree to model equivalences
     */
    private TwoWayHashmap<SceneGraphNode, Node> treeToModel;

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

    /** Date format **/
    private DateFormat df/** = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")**/
    ;

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
	df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, I18n.locale);
    }

    /**
     * Constructs the interface
     */
    public void doneLoading(AssetManager assetManager) {
	EventManager.getInstance().post(Events.POST_NOTIFICATION, txt("notif.gui.init"));

	skin = GlobalResources.skin;
	format = new DecimalFormat("0.0###");
	sformat = new DecimalFormat("0.###E0");

	buildGui();

	// We must subscribe to the desired events
	EventManager.getInstance().subscribe(this, Events.FOV_CHANGED_CMD, Events.CAMERA_MODE_CMD, Events.TIME_CHANGE_INFO, Events.TOGGLE_TIME_CMD, Events.SHOW_TUTORIAL_ACTION, Events.SHOW_SEARCH_ACTION, Events.FOCUS_CHANGED, Events.TOGGLE_VISIBILITY_CMD, Events.PACE_CHANGED_INFO, Events.GUI_SCROLL_POSITION_CMD, Events.GUI_FOLD_CMD, Events.GUI_MOVE_CMD, Events.ROTATION_SPEED_CMD, Events.CAMERA_SPEED_CMD, Events.TURNING_SPEED_CMD, Events.TIME_CHANGE_CMD, Events.SPEED_LIMIT_CMD);
    }

    private void buildGui() {
	final IGui gui = this;
	tooltips = new ArrayList<Actor>(10);

	/** Global resources **/
	TextureRegion septexreg = ((TextureRegionDrawable) skin.newDrawable("separator")).getRegion();
	septexreg.getTexture().setWrap(TextureWrap.Repeat, TextureWrap.ClampToEdge);
	TiledDrawable separator = new TiledDrawable(septexreg);

	/** The Options window **/
	options = new CollapsibleWindow(txt("gui.controls"), skin);
	options.left();
	options.setTitleAlignment(Align.left);
	options.setFillParent(false);
	options.setMovable(true);
	options.setResizable(false);
	options.padRight(5);
	options.padBottom(5);

	/** Global layout **/
	guiLayout = new Table();
	guiLayout.align(Align.left);
	//	guiLayout.space(VPADDING);

	/** ----CAMERA MODE---- **/
	VerticalGroup cameraGroup = new VerticalGroup().align(Align.left);

	Label cameraLabel = new Label(txt("gui.camera"), skin, "header");

	// Record camera button

	recCamera = new OwnImageButton(skin, "rec");
	recCamera.setName("rec");
	recCamera.setChecked(GlobalConf.runtime.RECORD_CAMERA);
	recCamera.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    EventManager.getInstance().post(Events.RECORD_CAMERA_CMD, recCamera.isChecked(), true);
		    return true;
		}
		return false;
	    }
	});
	Label recTooltip = new Label(txt("gui.tooltip.reccamera"), skin, "tooltip");
	tooltips.add(recTooltip);
	recCamera.addListener(new Tooltip<Label>(recTooltip));

	Label modeLabel = new Label(txt("gui.camera.mode"), skin, "default");
	int cameraModes = CameraMode.values().length;
	String[] cameraOptions = new String[cameraModes];
	for (int i = 0; i < cameraModes; i++) {
	    cameraOptions[i] = CameraMode.getMode(i).toString();
	}
	cameraMode = new SelectBox<String>(skin);
	cameraMode.setName("camera mode");
	cameraMode.setItems(cameraOptions);
	cameraMode.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    String selection = cameraMode.getSelected();
		    CameraMode mode = null;
		    try {
			mode = CameraMode.fromString(selection);
		    } catch (IllegalArgumentException e) {
			// Foucs to one of our models
			mode = CameraMode.Focus;
			EventManager.getInstance().post(Events.FOCUS_CHANGE_CMD, selection, true);
		    }

		    EventManager.getInstance().post(Events.CAMERA_MODE_CMD, mode);
		    return true;
		}
		return false;
	    }
	});

	Label fovLabel = new Label(txt("gui.camera.fov"), skin, "default");
	fieldOfView = new Slider(Constants.MIN_FOV, Constants.MAX_FOV, 1, false, skin);
	fieldOfView.setName("field of view");
	fieldOfView.setValue(GlobalConf.scene.CAMERA_FOV);
	fieldOfView.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    float value = MathUtilsd.clamp(fieldOfView.getValue(), Constants.MIN_FOV, Constants.MAX_FOV);
		    EventManager.getInstance().post(Events.FOV_CHANGED_CMD, value);
		    fov.setText(Integer.toString((int) value) + "°");
		    return true;
		}
		return false;
	    }

	});
	fov = new OwnLabel(Integer.toString((int) GlobalConf.scene.CAMERA_FOV) + "°", skin, "default");

	/** CAMERA SPEED LIMIT **/
	String[] speedLimits = new String[14];
	speedLimits[0] = txt("gui.camera.speedlimit.100kmh");
	speedLimits[1] = txt("gui.camera.speedlimit.c");
	speedLimits[2] = txt("gui.camera.speedlimit.cfactor", 2);
	speedLimits[3] = txt("gui.camera.speedlimit.cfactor", 10);
	speedLimits[4] = txt("gui.camera.speedlimit.cfactor", 1000);
	speedLimits[5] = txt("gui.camera.speedlimit.aus", 1);
	speedLimits[6] = txt("gui.camera.speedlimit.aus", 10);
	speedLimits[7] = txt("gui.camera.speedlimit.aus", 1000);
	speedLimits[8] = txt("gui.camera.speedlimit.aus", 10000);
	speedLimits[9] = txt("gui.camera.speedlimit.pcs", 1);
	speedLimits[10] = txt("gui.camera.speedlimit.pcs", 2);
	speedLimits[11] = txt("gui.camera.speedlimit.pcs", 10);
	speedLimits[12] = txt("gui.camera.speedlimit.pcs", 1000);
	speedLimits[13] = txt("gui.camera.speedlimit.nolimit");

	cameraSpeedLimit = new SelectBox<String>(skin);
	cameraSpeedLimit.setName("camera speed limit");
	cameraSpeedLimit.setItems(speedLimits);
	cameraSpeedLimit.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    int idx = cameraSpeedLimit.getSelectedIndex();
		    EventManager.getInstance().post(Events.SPEED_LIMIT_CMD, idx, true);
		    return true;
		}
		return false;
	    }
	});
	cameraSpeedLimit.setSelectedIndex(GlobalConf.scene.CAMERA_SPEED_LIMIT_IDX);

	/** CAMERA SPEED **/
	cameraSpeed = new Slider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
	cameraSpeed.setName("camera speed");
	cameraSpeed.setValue(GlobalConf.scene.CAMERA_SPEED * 10);
	cameraSpeed.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    EventManager.getInstance().post(Events.CAMERA_SPEED_CMD, cameraSpeed.getValue() / 10f, true);
		    speed.setText(Integer.toString((int) cameraSpeed.getValue()));
		    return true;
		}
		return false;
	    }

	});
	speed = new OwnLabel(Integer.toString((int) (GlobalConf.scene.CAMERA_SPEED * 10)), skin, "default");

	/** ROTATION SPEED **/
	rotateSpeed = new Slider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
	rotateSpeed.setName("rotate speed");
	rotateSpeed.setValue(MathUtilsd.lint(GlobalConf.scene.ROTATION_SPEED, Constants.MIN_ROT_SPEED, Constants.MAX_ROT_SPEED, Constants.MIN_SLIDER, Constants.MAX_SLIDER));
	rotateSpeed.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    EventManager.getInstance().post(Events.ROTATION_SPEED_CMD, MathUtilsd.lint(rotateSpeed.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_ROT_SPEED, Constants.MAX_ROT_SPEED), true);
		    rotate.setText(Integer.toString((int) rotateSpeed.getValue()));
		    return true;
		}
		return false;
	    }

	});
	rotate = new OwnLabel(Integer.toString((int) MathUtilsd.lint(GlobalConf.scene.ROTATION_SPEED, Constants.MIN_ROT_SPEED, Constants.MAX_ROT_SPEED, Constants.MIN_SLIDER, Constants.MAX_SLIDER)), skin, "default");

	/** TURNING SPEED **/
	turnSpeed = new Slider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
	turnSpeed.setName("turn speed");
	turnSpeed.setValue(MathUtilsd.lint(GlobalConf.scene.TURNING_SPEED, Constants.MIN_TURN_SPEED, Constants.MAX_TURN_SPEED, Constants.MIN_SLIDER, Constants.MAX_SLIDER));
	turnSpeed.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    EventManager.getInstance().post(Events.TURNING_SPEED_CMD, MathUtilsd.lint(turnSpeed.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_TURN_SPEED, Constants.MAX_TURN_SPEED), true);
		    turn.setText(Integer.toString((int) turnSpeed.getValue()));
		    return true;
		}
		return false;
	    }

	});
	turn = new OwnLabel(Integer.toString((int) MathUtilsd.lint(GlobalConf.scene.TURNING_SPEED, Constants.MIN_TURN_SPEED, Constants.MAX_TURN_SPEED, Constants.MIN_SLIDER, Constants.MAX_SLIDER)), skin, "default");

	/** Focus lock **/
	focusLock = new CheckBox(txt("gui.camera.lock"), skin);
	focusLock.setName("focus lock");
	focusLock.setChecked(GlobalConf.scene.FOCUS_LOCK);
	focusLock.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    EventManager.getInstance().post(Events.FOCUS_LOCK_CMD, "Focus lock", focusLock.isChecked());
		    return true;
		}
		return false;
	    }
	});

	HorizontalGroup camLabelGroup = new HorizontalGroup();
	camLabelGroup.space(HPADDING);
	camLabelGroup.addActor(cameraLabel);
	camLabelGroup.addActor(recCamera);
	cameraGroup.addActor(camLabelGroup);

	cameraGroup.addActor(modeLabel);
	cameraGroup.addActor(cameraMode);
	cameraGroup.addActor(fovLabel);

	HorizontalGroup fovGroup = new HorizontalGroup();
	fovGroup.space(3);
	fovGroup.addActor(fieldOfView);
	fovGroup.addActor(fov);

	HorizontalGroup speedGroup = new HorizontalGroup();
	speedGroup.space(3);
	speedGroup.addActor(cameraSpeed);
	speedGroup.addActor(speed);

	HorizontalGroup rotateGroup = new HorizontalGroup();
	rotateGroup.space(3);
	rotateGroup.addActor(rotateSpeed);
	rotateGroup.addActor(rotate);

	HorizontalGroup turnGroup = new HorizontalGroup();
	turnGroup.space(3);
	turnGroup.addActor(turnSpeed);
	turnGroup.addActor(turn);

	cameraGroup.addActor(fovGroup);
	cameraGroup.addActor(new Label(txt("gui.camera.speedlimit"), skin, "default"));
	cameraGroup.addActor(cameraSpeedLimit);
	cameraGroup.addActor(new Label(txt("gui.camera.speed"), skin, "default"));
	cameraGroup.addActor(speedGroup);
	cameraGroup.addActor(new Label(txt("gui.rotation.speed"), skin, "default"));
	cameraGroup.addActor(rotateGroup);
	cameraGroup.addActor(new Label(txt("gui.turn.speed"), skin, "default"));
	cameraGroup.addActor(turnGroup);
	cameraGroup.addActor(focusLock);

	/** ----OBJECTS TREE---- **/
	VerticalGroup objectsGroup = new VerticalGroup().align(Align.left).space(3);
	Label objectsLabel = new Label(txt("gui.objects"), skin, "header");

	searchBox = new TextField("", skin);
	searchBox.setName("search box");
	searchBox.setMessageText(txt("gui.objects.search"));
	searchBox.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof InputEvent) {
		    InputEvent ie = (InputEvent) event;
		    if (ie.getType() == Type.keyUp) {
			String text = searchBox.getText();
			if (sg.containsNode(text.toLowerCase())) {
			    SceneGraphNode node = sg.getNode(text.toLowerCase());
			    if (node instanceof CelestialBody) {
				EventManager.getInstance().post(Events.FOCUS_CHANGE_CMD, node, true);
			    }
			}
			GaiaInputController.pressedKeys.remove(ie.getKeyCode());
		    }
		    return true;
		}
		return false;
	    }
	});

	treeToModel = new TwoWayHashmap<SceneGraphNode, Node>();

	EventManager.getInstance().post(Events.POST_NOTIFICATION, txt("notif.sgtree.init"));

	if (tree) {
	    final Tree objectsTree = new Tree(skin, "bright");
	    objectsTree.setName("objects list");
	    objectsTree.setPadding(1);
	    objectsTree.setIconSpacing(1, 1);
	    objectsTree.setYSpacing(0);
	    Array<Node> nodes = createTree(sg.getRoot().children);
	    for (Node node : nodes) {
		objectsTree.add(node);
	    }
	    objectsTree.expandAll();
	    objectsTree.addListener(new EventListener() {
		@Override
		public boolean handle(Event event) {
		    if (event instanceof ChangeEvent) {
			if (objectsTree.getSelection().hasItems()) {
			    if (objectsTree.getSelection().hasItems()) {
				Node n = objectsTree.getSelection().first();
				SceneGraphNode sgn = treeToModel.getBackward(n);
				EventManager.getInstance().post(Events.CAMERA_MODE_CMD, CameraMode.Focus);
				EventManager.getInstance().post(Events.FOCUS_CHANGE_CMD, sgn, false);
			    }

			}
			return true;
		    }
		    return false;
		}

	    });
	    objectsList = objectsTree;
	} else if (list) {
	    final com.badlogic.gdx.scenes.scene2d.ui.List<String> focusList = new com.badlogic.gdx.scenes.scene2d.ui.List<String>(skin, "light");
	    focusList.setName("objects list");
	    List<CelestialBody> focusableObjects = sg.getFocusableObjects();
	    Array<String> names = new Array<String>(focusableObjects.size());
	    for (CelestialBody cb : focusableObjects) {
		// Omit stars with no proper names
		if (!cb.name.startsWith("star_") && !cb.name.startsWith("Hip ") && !cb.name.startsWith("dummy")) {
		    names.add(cb.name);
		}
	    }
	    focusList.setItems(names);
	    focusList.pack();//
	    focusList.addListener(new EventListener() {
		@Override
		public boolean handle(Event event) {
		    if (event instanceof ChangeEvent) {
			ChangeEvent ce = (ChangeEvent) event;
			Actor actor = ce.getTarget();
			String name = ((com.badlogic.gdx.scenes.scene2d.ui.List<String>) actor).getSelected();
			if (name != null) {
			    // Change focus
			    EventManager.getInstance().post(Events.FOCUS_CHANGE_CMD, sg.getNode(name), false);
			    // Change camera mode to focus
			    EventManager.getInstance().post(Events.CAMERA_MODE_CMD, CameraMode.Focus);
			}
			return true;
		    }
		    return false;
		}
	    });
	    objectsList = focusList;
	}
	EventManager.getInstance().post(Events.POST_NOTIFICATION, txt("notif.sgtree.initialised"));

	if (tree || list) {
	    focusListScrollPane = new OwnScrollPane(objectsList, skin, "minimalist");
	    focusListScrollPane.setName("objects list scroll");
	    focusListScrollPane.setFadeScrollBars(false);
	    focusListScrollPane.setScrollingDisabled(true, false);

	    focusListScrollPane.setHeight(tree ? 200 : 100);
	    focusListScrollPane.setWidth(160);
	}

	objectsGroup.addActor(objectsLabel);
	objectsGroup.addActor(searchBox);
	if (focusListScrollPane != null) {
	    objectsGroup.addActor(focusListScrollPane);
	}

	/** ----OBJECT TOGGLES GROUP---- **/
	VerticalGroup visibilityGroup = new VerticalGroup().align(Align.left);

	Label visibilityLabel = new Label(txt("gui.visibility"), skin, "header");

	Table visibilityTable = new Table(skin);
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
			    EventManager.getInstance().post(Events.TOGGLE_VISIBILITY_CMD, name, true, ((Button) event.getListenerActor()).isChecked());
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

	visibilityGroup.addActor(visibilityLabel);
	visibilityGroup.addActor(visibilityTable);

	/** ----LIGHTING GROUP---- **/
	VerticalGroup lightingGroup = new VerticalGroup().align(Align.left);
	Label lightingLabel = new Label(txt("gui.lighting"), skin, "header");
	Label brightnessLabel = new Label(txt("gui.starbrightness"), skin, "default");
	brightness = new OwnLabel(Integer.toString((int) (MathUtilsd.lint(GlobalConf.scene.STAR_BRIGHTNESS, Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT, Constants.MIN_SLIDER, Constants.MAX_SLIDER))), skin);
	starBrightness = new Slider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
	starBrightness.setName("star brightness");
	starBrightness.setValue(MathUtilsd.lint(GlobalConf.scene.STAR_BRIGHTNESS, Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT, Constants.MIN_SLIDER, Constants.MAX_SLIDER));
	starBrightness.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    EventManager.getInstance().post(Events.STAR_BRIGHTNESS_CMD, MathUtilsd.lint(starBrightness.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT));
		    brightness.setText(Integer.toString((int) starBrightness.getValue()));
		    return true;
		}
		return false;
	    }
	});
	HorizontalGroup brightnessGroup = new HorizontalGroup();
	brightnessGroup.space(3);
	brightnessGroup.addActor(starBrightness);
	brightnessGroup.addActor(brightness);

	Label ambientLightLabel = new Label(txt("gui.light.ambient"), skin, "default");
	ambient = new OwnLabel(Integer.toString((int) (GlobalConf.scene.AMBIENT_LIGHT * 100)), skin);
	ambientLight = new Slider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
	ambientLight.setName("ambient light");
	ambientLight.setValue(GlobalConf.scene.AMBIENT_LIGHT * 100);
	ambientLight.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    EventManager.getInstance().post(Events.AMBIENT_LIGHT_CMD, ambientLight.getValue() / 100f);
		    ambient.setText(Integer.toString((int) ambientLight.getValue()));
		    return true;
		}
		return false;
	    }
	});
	HorizontalGroup ambientGroup = new HorizontalGroup();
	ambientGroup.space(3);
	ambientGroup.addActor(ambientLight);
	ambientGroup.addActor(ambient);

	Label bloomLabel = new Label(txt("gui.bloom"), skin, "default");
	bloom = new OwnLabel(Integer.toString((int) (GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY * 10)), skin);
	bloomEffect = new Slider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
	bloomEffect.setName("bloom effect");
	bloomEffect.setValue(GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY * 10f);
	bloomEffect.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    EventManager.getInstance().post(Events.BLOOM_CMD, bloomEffect.getValue() / 10f);
		    bloom.setText(Integer.toString((int) bloomEffect.getValue()));
		    return true;
		}
		return false;
	    }
	});

	HorizontalGroup bloomGroup = new HorizontalGroup();
	bloomGroup.space(3);
	bloomGroup.addActor(bloomEffect);
	bloomGroup.addActor(bloom);

	lensFlare = new CheckBox(txt("gui.lensflare"), skin);
	lensFlare.setName("lens flare");
	lensFlare.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    EventManager.getInstance().post(Events.LENS_FLARE_CMD, lensFlare.isChecked());
		    return true;
		}
		return false;
	    }
	});
	lensFlare.setChecked(GlobalConf.postprocess.POSTPROCESS_LENS_FLARE);

	lightingGroup.addActor(lightingLabel);
	lightingGroup.addActor(brightnessLabel);
	lightingGroup.addActor(brightnessGroup);
	lightingGroup.addActor(ambientLightLabel);
	lightingGroup.addActor(ambientGroup);
	lightingGroup.addActor(bloomLabel);
	lightingGroup.addActor(bloomGroup);
	lightingGroup.addActor(lensFlare);

	/** ----GAIA SCAN GROUP---- **/
	VerticalGroup optionsGroup = new VerticalGroup().align(Align.left);
	Label togglesLabel = new Label(txt("gui.gaiascan"), skin, "header");

	computeGaiaScan = new CheckBox(txt("gui.gaiascan.enable"), skin);
	computeGaiaScan.setName("compute gaia scan");
	computeGaiaScan.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    EventManager.getInstance().post(Events.COMPUTE_GAIA_SCAN_CMD, txt("gui.gaiascan.compute"), computeGaiaScan.isChecked());
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
		    EventManager.getInstance().post(Events.TRANSIT_COLOUR_CMD, txt("gui.gaiascan.transit"), transitColor.isChecked());
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
		    EventManager.getInstance().post(Events.ONLY_OBSERVED_STARS_CMD, txt("gui.gaiascan.only"), onlyObservedStars.isChecked());
		    return true;
		}
		return false;
	    }
	});
	onlyObservedStars.setChecked(GlobalConf.scene.ONLY_OBSERVED_STARS);

	optionsGroup.addActor(togglesLabel);
	optionsGroup.addActor(computeGaiaScan);
	optionsGroup.addActor(transitColor);
	optionsGroup.addActor(onlyObservedStars);

	/** ----TIME GROUP---- **/
	VerticalGroup timeGroup = new VerticalGroup().align(Align.left).space(3).padTop(3);
	Label timeLabel = new Label(txt("gui.time"), skin, "header");

	// Time
	date = new OwnLabel("", skin);
	date.setName("input time");

	dateEdit = new OwnImageButton(skin, "edit");
	dateEdit.addListener(new EventListener() {

	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    // Left button click
		    if (dateDialog == null) {
			dateDialog = new DateDialog(gui, skin);
		    }
		    dateDialog.updateTime(GlobalClock.clock.time);
		    dateDialog.display();
		}
		return false;
	    }

	});
	Label dateEditTooltip = new Label(txt("gui.tooltip.dateedit"), skin, "tooltip");
	tooltips.add(dateEditTooltip);
	dateEdit.addListener(new Tooltip<Label>(dateEditTooltip));

	// Play/stop

	playstop = new OwnImageButton(skin, "playstop");
	playstop.setName("play stop");
	playstop.setChecked(GlobalConf.runtime.TIME_ON);
	playstop.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    EventManager.getInstance().post(Events.TOGGLE_TIME_CMD, playstop.isChecked(), true);
		    return true;
		}
		return false;
	    }
	});
	Label playstopTooltip = new Label(txt("gui.tooltip.playstop"), skin, "tooltip");
	tooltips.add(playstopTooltip);
	playstop.addListener(new Tooltip<Label>(playstopTooltip));

	// Pace
	Label paceLabel = new Label(txt("gui.pace"), skin);
	plus = new ImageButton(skin.getDrawable("tree-plus"));
	plus.setName("plus");
	plus.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    // Plus pressed
		    EventManager.getInstance().post(Events.PACE_DOUBLE_CMD);

		    return true;
		}
		return false;
	    }
	});
	minus = new ImageButton(skin.getDrawable("tree-minus"));
	minus.setName("minus");
	minus.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    // Minus pressed
		    EventManager.getInstance().post(Events.PACE_DIVIDE_CMD);
		    return true;
		}
		return false;
	    }
	});
	inputPace = new OwnTextField(Double.toString(GlobalClock.clock.pace), skin);
	inputPace.setName("input pace");
	inputPace.setMaxLength(15);
	inputPace.setWidth(60f);
	inputPace.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof InputEvent) {
		    InputEvent ie = (InputEvent) event;
		    if (ie.getType() == Type.keyTyped) {
			try {
			    double pace = Double.parseDouble(inputPace.getText());
			    EventManager.getInstance().post(Events.PACE_CHANGE_CMD, pace, true);
			} catch (Exception e) {
			    return false;
			}
			return true;
		    }
		}
		return false;
	    }
	});

	HorizontalGroup labelGroup = new HorizontalGroup();
	labelGroup.space(HPADDING);
	labelGroup.addActor(timeLabel);
	labelGroup.addActor(playstop);
	timeGroup.addActor(labelGroup);

	HorizontalGroup dateGroup = new HorizontalGroup();
	dateGroup.space(4);
	dateGroup.addActor(date);
	dateGroup.addActor(dateEdit);
	timeGroup.addActor(dateGroup);

	HorizontalGroup paceGroup = new HorizontalGroup();
	paceGroup.space(1);
	paceGroup.addActor(paceLabel);
	paceGroup.addActor(minus);
	paceGroup.addActor(inputPace);
	paceGroup.addActor(plus);

	timeGroup.addActor(paceGroup);

	timeGroup.pack();

	/** BUTTONS **/
	Button preferences = new OwnTextButton(txt("gui.preferences"), skin);
	preferences.setName("preferences");
	preferences.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    EventManager.getInstance().post(Events.SHOW_PREFERENCES_ACTION);
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
		    EventManager.getInstance().post(Events.SHOW_TUTORIAL_ACTION);
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
		    EventManager.getInstance().post(Events.SHOW_ABOUT_ACTION);
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
		    EventManager.getInstance().post(Events.SHOW_RUNSCRIPT_ACTION);
		}
		return false;
	    }
	});

	/** ADD GROUPS TO VERTICAL LAYOUT **/
	int pad = 10;
	guiLayout.add(timeGroup).left().padBottom(pad);
	guiLayout.row();
	guiLayout.add(new Image(separator)).left().fill(true, false);
	guiLayout.row();
	guiLayout.add(cameraGroup).left().padBottom(pad);
	guiLayout.row();
	guiLayout.add(new Image(separator)).left().fill(true, false);
	guiLayout.row();
	guiLayout.add(visibilityGroup).left().padBottom(pad);
	guiLayout.row();
	guiLayout.add(new Image(separator)).left().fill(true, false);
	guiLayout.row();
	guiLayout.add(lightingGroup).left().padBottom(pad);
	guiLayout.row();
	guiLayout.add(new Image(separator)).left().fill(true, false);
	guiLayout.row();
	guiLayout.add(objectsGroup).left().padBottom(pad);
	guiLayout.row();
	guiLayout.add(new Image(separator)).left().fill(true, false);
	guiLayout.row();
	guiLayout.add(optionsGroup).left().padBottom(pad);
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
	mainVertical.align(Align.right);
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
	notificationsInterface = new NotificationsInterface(skin, lock);
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
	windowScroll.setHeight(Math.min(guiLayout.getHeight(), Gdx.graphics.getHeight() - 100));
	windowScroll.pack();

	mainVertical.setHeight(windowScroll.getHeight() + 30);
	mainVertical.pack();

	options.setHeight(windowScroll.getHeight() + 40);
	options.pack();
	options.validate();
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
	    for (Actor tooltip : tooltips) {
		ui.addActor(tooltip);
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
			    }
			    else {
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
	case TIME_CHANGE_INFO:
	case TIME_CHANGE_CMD:
	    // Update input time
	    Date time = (Date) data[0];
	    date.setText(df.format(time));
	    break;
	case CAMERA_MODE_CMD:
	    // Update camera mode selection
	    CameraMode mode = (CameraMode) data[0];
	    cameraMode.setSelected(mode.toString());
	    if (mode.equals(CameraMode.Focus)) {
		focusInterface.displayFocusInfo();
	    } else {
		focusInterface.hideFocusInfo();
	    }
	    break;
	case FOCUS_CHANGED:
	    // Update focus selection in focus list
	    SceneGraphNode sgn = null;
	    if (data[0] instanceof String) {
		sgn = sg.getNode((String) data[0]);
	    } else {
		sgn = (SceneGraphNode) data[0];
	    }
	    // Select only if data[1] is true
	    if (sgn != null) {
		if (tree) {
		    Tree objList = ((Tree) objectsList);
		    Node node = treeToModel.getForward(sgn);
		    objList.getSelection().set(node);
		    node.expandTo();

		    focusListScrollPane.setScrollY(focusListScrollPane.getMaxY() - node.getActor().getY());
		} else if (list) {
		    // Update focus selection in focus list
		    com.badlogic.gdx.scenes.scene2d.ui.List<String> objList = (com.badlogic.gdx.scenes.scene2d.ui.List<String>) objectsList;
		    Array<String> items = objList.getItems();
		    SceneGraphNode node = (SceneGraphNode) data[0];

		    // Select without firing events, do not use set()
		    objList.getSelection().items().clear();
		    objList.getSelection().items().add(node.name);

		    int itemIdx = items.indexOf(node.name, false);
		    if (itemIdx >= 0) {
			objList.getSelection().setProgrammaticChangeEvents(false);
			objList.setSelectedIndex(itemIdx);
			objList.getSelection().setProgrammaticChangeEvents(true);
			float itemHeight = objList.getItemHeight();
			focusListScrollPane.setScrollY(itemIdx * itemHeight);
		    }
		}
	    }
	    break;
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
	case SHOW_TUTORIAL_ACTION:
	    EventManager.getInstance().post(Events.RUN_SCRIPT_PATH, GlobalConf.program.TUTORIAL_SCRIPT_LOCATION);
	    break;
	case SHOW_SEARCH_ACTION:
	    if (searchDialog == null) {
		searchDialog = new SearchDialog(this, skin, sg);
	    } else {
		searchDialog.clearText();
	    }
	    searchDialog.display();
	    break;
	case TOGGLE_VISIBILITY_CMD:
	    boolean interf = (Boolean) data[1];
	    if (!interf) {
		String name = (String) data[0];
		Button b = buttonMap.get(name);
		if (b != null) {
		    if (data.length == 3) {
			b.setChecked((Boolean) data[2]);
		    } else {
			b.setChecked(!b.isChecked());
		    }
		}
	    }
	    break;
	case PACE_CHANGED_INFO:
	    if (data.length == 1)
		this.inputPace.setText(Double.toString((double) data[0]));
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
	case ROTATION_SPEED_CMD:
	    interf = (Boolean) data[1];
	    if (!interf) {
		float value = (Float) data[0];
		value = MathUtilsd.lint(value, Constants.MIN_ROT_SPEED, Constants.MAX_ROT_SPEED, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
		rotateSpeed.setValue(value);
		rotate.setText(Integer.toString((int) value));
	    }
	    break;
	case CAMERA_SPEED_CMD:
	    interf = (Boolean) data[1];
	    if (!interf) {
		float value = (Float) data[0];
		value *= 10;
		cameraSpeed.setValue(value);
		speed.setText(Integer.toString((int) value));
	    }
	    break;

	case TURNING_SPEED_CMD:
	    interf = (Boolean) data[1];
	    if (!interf) {
		float value = (Float) data[0];
		value = MathUtilsd.lint(value, Constants.MIN_TURN_SPEED, Constants.MAX_TURN_SPEED, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
		turnSpeed.setValue(value);
		turn.setText(Integer.toString((int) value));
	    }
	    break;
	case SPEED_LIMIT_CMD:
	    interf = (Boolean) data[1];
	    if (!interf) {
		int value = (Integer) data[0];
		cameraSpeedLimit.setSelectedIndex(value);
	    }
	    break;
	}

    }

    public void resize(int width, int height) {
	ui.getViewport().update(width, height, true);
	rebuildGui();
    }

    private Array<Node> createTree(List<SceneGraphNode> nodes) {
	Array<Node> treeNodes = new Array<Node>(nodes.size());
	for (SceneGraphNode node : nodes) {
	    Label l = new Label(node.name, skin, "ui-10");
	    l.setColor(Color.BLACK);
	    Node treeNode = new Node(l);

	    if (node.children != null && !node.children.isEmpty()) {
		treeNode.addAll(createTree(node.children));
	    }

	    treeNodes.add(treeNode);
	    treeToModel.add(node, treeNode);
	}

	return treeNodes;
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
