package gaia.cu9.ari.gaiaorbit;

import gaia.cu9.ari.gaiaorbit.data.AssetBean;
import gaia.cu9.ari.gaiaorbit.data.SGLoader;
import gaia.cu9.ari.gaiaorbit.data.SGLoader.SGLoaderParameter;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitDataLoader;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.FullGui;
import gaia.cu9.ari.gaiaorbit.interfce.GaiaInputController;
import gaia.cu9.ari.gaiaorbit.interfce.HUDGui;
import gaia.cu9.ari.gaiaorbit.interfce.IGui;
import gaia.cu9.ari.gaiaorbit.interfce.LoadingGui;
import gaia.cu9.ari.gaiaorbit.interfce.MobileGui;
import gaia.cu9.ari.gaiaorbit.interfce.RenderGui;
import gaia.cu9.ari.gaiaorbit.render.AbstractRenderer;
import gaia.cu9.ari.gaiaorbit.render.GSPostProcessor;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.RenderType;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.ImageRenderer;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.GlobalClock;

import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

/**
 * The main class. Holds all the entities manages the update/draw cycle as well as the image rendering.
 * @author Toni Sagrista
 *
 */
public class GaiaSandbox implements ApplicationListener, IObserver {
    private static boolean LOADING = true;

    private static GaiaSandbox instance;

    // Asset manager
    public AssetManager manager;

    /** This handles the input events **/
    private GaiaInputController inputController;

    // Camera
    public CameraManager cam;

    public ISceneGraph sg;
    private SceneGraphRenderer sgr;
    private IPostProcessor pp;

    /**
     * The user interface
     */
    public IGui gui, loadingGui, renderGui;

    private boolean initialized = false;

    /** Command to take screenshot **/
    private class ScreenshotCmd {
	public static final String FILENAME = "screenshot";
	public String folder;
	public int width, height;
	public boolean active = false;

	public ScreenshotCmd() {
	    super();
	}

	public void takeScreenshot(int width, int height, String folder) {
	    this.folder = folder;
	    this.width = width;
	    this.height = height;
	    this.active = true;
	}

    }

    private ScreenshotCmd screenshot;

    public static GaiaSandbox getInstance() {
	return instance;
    }

    /**
     * Creates a Gaia Sandbox instance.
     * @param openGLGUI This will paint the GUI in OpenGL. True for Desktop (if not Swing GUI) and Android.
     */
    public GaiaSandbox(boolean openGLGUI) {
	super();
	GlobalConf.OPENGL_GUI = openGLGUI;
	instance = this;
    }

    public void setSceneGraph(ISceneGraph sg) {
	this.sg = sg;
    }

    @Override
    public void create() {
	Gdx.app.setLogLevel(Application.LOG_INFO);

	boolean mobile = Constants.mobile;
	boolean desktop = !mobile;

	// Disable all kinds of input
	EventManager.getInstance().post(Events.INPUT_ENABLED_CMD, false);

	if (!GlobalClock.initialized()) {
	    Calendar c = new GregorianCalendar();
	    c.set(Calendar.YEAR, 2013);
	    c.set(Calendar.MONTH, Calendar.DECEMBER);
	    c.set(Calendar.DAY_OF_MONTH, 19);
	    c.set(Calendar.HOUR_OF_DAY, 10);
	    c.set(Calendar.MINUTE, 56);
	    // Initialize clock with a pace of 2 simulation hours/second
	    GlobalClock.initialize(0.01, c.getTime());
	}

	if (!GlobalConf.initialized()) {
	    // Initialize the configuration if needed
	    try {
		if (mobile) {
		    GlobalConf.initialize(Gdx.files.internal("conf/android/global.properties").read(), this.getClass().getResourceAsStream("/version"));
		} else {
		    File confFile = new File(System.getProperty("properties.file"));
		    FileInputStream fis = new FileInputStream(confFile);
		    FileHandle versionfile = Gdx.files.internal("version");
		    if (!versionfile.exists()) {
			versionfile = Gdx.files.internal("data/dummyversion");
		    }
		    GlobalConf.initialize(fis, versionfile.read());
		    fis.close();
		}
	    } catch (Exception e) {
		// Android
		EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	    }
	}

	// Precompute some math functions
	MathUtilsd.initialize();

	// Initialize i18n
	I18n.initialize();

	// Initialize asset manager
	FileHandleResolver resolver = new InternalFileHandleResolver();
	manager = new AssetManager(resolver);
	manager.setLoader(ISceneGraph.class, new SGLoader(resolver));
	manager.setLoader(OrbitData.class, new OrbitDataLoader(resolver));

	// Init global resources
	GlobalResources.initialize(manager);

	// Initialize Cameras
	cam = new CameraManager(manager, CameraMode.Focus);

	if (sg == null) {
	    // Set asset manager to asset bean
	    AssetBean.setAssetManager(manager);
	    manager.load(GlobalConf.instance.SG_FILE, ISceneGraph.class, new SGLoaderParameter(GlobalClock.clock, GlobalConf.instance.MULTITHREADING, GlobalConf.instance.NUMBER_THREADS));
	}

	// Initialize timestamp for screenshots
	renderGui = new RenderGui();
	renderGui.initialize(manager);

	if (GlobalConf.OPENGL_GUI) {
	    // Load scene graph
	    if (desktop) {
		// Full GUI for desktop
		gui = new FullGui();
	    } else {
		// Reduced GUI for android/iOS/...
		gui = new MobileGui();
	    }
	} else {
	    // Only the HUD
	    gui = new HUDGui();
	}
	gui.initialize(manager);

	// Tell the asset manager to load all the assets
	Set<AssetBean> assets = AssetBean.getAssets();
	for (AssetBean ab : assets) {
	    ab.load(manager);
	}

	screenshot = new ScreenshotCmd();

	// Initialize loading screen
	loadingGui = new LoadingGui(GlobalConf.OPENGL_GUI, desktop ? 23 : 20);
	loadingGui.initialize(manager);

	EventManager.getInstance().post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), I18n.bundle.format("notif.glslversion", Gdx.gl.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION)));
    }

    /**
     * Execute this when the models have finished loading. This sets the models to their classes and 
     * removes the Loading message
     */
    private void doneLoading() {
	loadingGui.dispose();
	loadingGui = null;

	pp = new GSPostProcessor();

	GlobalResources.doneLoading(manager);

	if (manager.isLoaded(GlobalConf.instance.SG_FILE)) {
	    sg = manager.get(GlobalConf.instance.SG_FILE);
	}

	AbstractRenderer.initialize(sg);
	sgr = new SceneGraphRenderer();
	sgr.initialize(manager);

	// First time, set assets
	List<SceneGraphNode> nodes = sg.getNodes();
	for (SceneGraphNode sgn : nodes) {
	    sgn.doneLoading(manager);
	}
	// Update whole tree to initialize positions
	GlobalClock.clock.update(0.00000001f);
	sg.update(GlobalClock.clock, cam);
	GlobalClock.clock.update(0);

	// Initialize  input handlers
	InputMultiplexer inputMultiplexer = new InputMultiplexer();
	if (GlobalConf.OPENGL_GUI) {
	    // Only for the Full GUI
	    gui.setSceneGraph(sg);
	    gui.setVisibilityToggles(ComponentType.values(), SceneGraphRenderer.visible);
	    inputMultiplexer.addProcessor(gui.getGuiStage());
	}
	// Initialize the GUI
	gui.doneLoading(manager);
	renderGui.doneLoading(manager);

	// Publish visibility
	EventManager.getInstance().post(Events.VISIBILITY_OF_COMPONENTS, new Object[] { SceneGraphRenderer.visible });

	inputController = new GaiaInputController(cam, gui);
	inputMultiplexer.addProcessor(inputController);

	Gdx.input.setInputProcessor(inputMultiplexer);

	EventManager.getInstance().post(Events.SCENE_GRAPH_LOADED, sg);
	EventManager.getInstance().post(Events.CAMERA_MODE_CMD, CameraMode.Focus);

	AbstractPositionEntity focus = (AbstractPositionEntity) sg.getNode("earth");
	EventManager.getInstance().post(Events.FOCUS_CHANGE_CMD, focus, true);
	float dst = focus.size * 3;
	Vector3d newCameraPos = focus.pos.cpy().add(0, 0, -dst);
	EventManager.getInstance().post(Events.CAMERA_POS_CMD, newCameraPos.values());

	// Update whole tree to reinitialize positions with the new camera position
	GlobalClock.clock.update(0.00000001f);
	sg.update(GlobalClock.clock, cam);
	GlobalClock.clock.update(0);

	Vector3d newCameraDir = focus.pos.cpy().sub(newCameraPos);
	EventManager.getInstance().post(Events.CAMERA_DIR_CMD, newCameraDir.values());

	// Initialize time in GUI
	EventManager.getInstance().post(Events.TIME_CHANGE_INFO, GlobalClock.clock.time);

	// Subscribe to events
	EventManager.getInstance().subscribe(this, Events.TOGGLE_AMBIENT_LIGHT, Events.AMBIENT_LIGHT_CMD, Events.SCREENSHOT_CMD, Events.FULLSCREEN_CMD);

	// Run garbage collector before starting
	System.gc();

	// Re-enable input
	EventManager.getInstance().post(Events.INPUT_ENABLED_CMD, true);

	initialized = true;

	// Run tutorial
	if (GlobalConf.instance.DISPLAY_TUTORIAL) {
	    EventManager.getInstance().post(Events.SHOW_TUTORIAL_ACTION);
	    GlobalConf.instance.DISPLAY_TUTORIAL = false;
	}

    }

    @Override
    public void dispose() {
	try {
	    if (!Constants.mobile)
		GlobalConf.instance.saveProperties(new File(System.getProperty("properties.file")).toURI().toURL());
	} catch (MalformedURLException e) {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	}
	gui.dispose();
	renderGui.dispose();
	if (sg != null) {
	    sg.dispose();
	}
	ModelCache.cache.dispose();
    }

    @Override
    public void render() {
	if (LOADING) {
	    if (manager.update()) {
		doneLoading();

		LOADING = false;
	    } else {
		// Display loading screen
		renderLoadingScreen();
	    }
	} else {
	    if (GlobalConf.instance.GLOBAL_PAUSE) {
		// We are in pause mode!
		try {
		    Thread.sleep(200);
		} catch (InterruptedException e) {
		    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
		}
	    } else {
		// Asynchronous load of textures and resources
		manager.update();

		/**
		 * UPDATE
		 */
		update(Gdx.graphics.getDeltaTime());

		/**
		 * RENDER
		 */

		/* SCREEN OUTPUT */
		if (GlobalConf.instance.SCREEN_OUTPUT) {
		    /** RENDER THE SCENE **/
		    // Set viewport
		    setViewportSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), cam);
		    preRenderScene();
		    sgr.render(cam, null, pp.getPostProcessBean(RenderType.screen));

		    if (!GlobalConf.instance.CLEAN_MODE) {
			// Render the GUI, setting the viewport
			gui.getGuiStage().getViewport().apply();
			gui.render();
		    }
		}

		/* FRAME OUTPUT */
		if (GlobalConf.instance.RENDER_OUTPUT) {
		    renderToImage(cam, pp.getPostProcessBean(RenderType.frame), GlobalConf.instance.RENDER_WIDTH, GlobalConf.instance.RENDER_HEIGHT, GlobalConf.instance.RENDER_FOLDER, GlobalConf.instance.RENDER_FILE_NAME);
		}

		/* SCREENSHOT OUTPUT */
		if (screenshot.active) {
		    String file = renderToImage(cam, pp.getPostProcessBean(RenderType.screenshot), screenshot.width, screenshot.height, screenshot.folder, ScreenshotCmd.FILENAME);
		    screenshot.active = false;
		    EventManager.getInstance().post(Events.SCREENSHOT_INFO, file);
		}

		sgr.clearLists();
	    }
	}

	EventManager.getInstance().post(Events.FPS_INFO, Gdx.graphics.getFramesPerSecond());
    }

    /**
     * Update method.
     * @param dt Delta time in seconds.
     */
    public void update(float dt) {
	if (GlobalConf.instance.RENDER_OUTPUT) {
	    // If RENDER_OUTPUT is active, we need to set our dt according to the fps
	    dt = 1f / GlobalConf.instance.RENDER_TARGET_FPS;
	} else {
	    // Max time step is 0.1 seconds. Not in RENDER_OUTPUT MODE.
	    dt = Math.min(dt, 0.1f);
	}

	gui.update(dt);
	renderGui.update(dt);

	float dtScene = dt;
	if (!GlobalConf.instance.TIME_ON) {
	    dtScene = 0;
	}
	// Update clock
	GlobalClock.clock.update(dtScene);

	// Update events
	EventManager.getInstance().dispatchDelayedMessages();

	// Update cameras
	cam.update(dt, GlobalClock.clock);

	// Update scene graph
	sg.update(GlobalClock.clock, cam);

    }

    public void preRenderScene() {
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
	Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Renders the current scene to an image and returns the file name where it has been written to
     * @param camera
     * @param width The width of the image.
     * @param height The height of the image.
     * @param folder The folder to save the image to.
     * @param filename The file name prefix.
     * @return
     */
    public String renderToImage(ICamera camera, PostProcessBean ppb, int width, int height, String folder, String filename) {
	setViewportSize(width, height, camera);
	FrameBuffer m_fbo = new FrameBuffer(Format.RGBA8888, width, height, true);
	// TODO That's a dirty trick, we should find a better way (i.e. making buildEnabledEffectsList() method public)
	boolean postprocessing = ppb.pp.captureNoClear();
	ppb.pp.captureEnd();
	if (!postprocessing) {
	    // If post processing is not active, we must start the buffer now.
	    // Otherwise, it is started in the renderScene().
	    m_fbo.begin();
	}

	// this is the main render function
	preRenderScene();
	sgr.render(camera, postprocessing ? m_fbo : null, ppb);

	if (postprocessing) {
	    // If post processing is active, we have to start now again because
	    // the renderScene() has closed it.
	    m_fbo.begin();
	}
	if (GlobalConf.instance.RENDER_SCREENSHOT_TIME) {
	    // Timestamp
	    renderGui.resize(camera.getViewport().getScreenWidth(), camera.getViewport().getScreenHeight());
	    renderGui.render();
	}

	// Screenshot while the frame buffer is on
	String file = ImageRenderer.renderToImageGl20(folder, filename, camera.getViewport().getScreenWidth(), camera.getViewport().getScreenHeight());
	m_fbo.end();
	m_fbo.dispose();
	return file;
    }

    /**
     * Sets the viewport size to w and h
     * @param w New viewport width
     * @param h New viewport height
     */
    private void setViewportSize(int width, int height, ICamera camera) {
	camera.getViewport().update(width, height);
    }

    @Override
    public void resize(int width, int height) {
	if (!initialized) {
	    loadingGui.resize(width, height);
	} else {
	    pp.resize(width, height);
	    gui.resize(width, height);
	}

	cam.updateAngleEdge(width, height);
	cam.getViewport().update(width, height, false);

	EventManager.getInstance().post(Events.SCREEN_RESIZE, width, height);

	Gdx.app.debug("Resize", width + "x" + height + ", new Viewport: " + cam.getViewport().getScreenX() + "," + cam.getViewport().getScreenY() + "|" + cam.getViewport().getScreenWidth() + "," + cam.getViewport().getScreenHeight());
    }

    /**
     * Renders the loading screen
     * @param g
     */
    private void renderLoadingScreen() {
	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
	Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
	loadingGui.render();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    public List<CelestialBody> getFocusableEntities() {

	return sg.getFocusableObjects();
    }

    public SceneGraphNode findEntity(String name) {
	return sg.getNode(name);
    }

    public CelestialBody findFocusByName(String name) {
	return sg.findFocus(name);
    }

    @Override
    public void notify(Events event, Object... data) {
	switch (event) {
	case TOGGLE_AMBIENT_LIGHT:
	    // TODO No better place to put this??
	    ModelComponent.toggleAmbientLight((Boolean) data[1]);
	    break;
	case AMBIENT_LIGHT_CMD:
	    ModelComponent.setAmbientLight((float) data[0]);
	    break;
	case SCREENSHOT_CMD:
	    screenshot.takeScreenshot((int) data[0], (int) data[1], (String) data[2]);
	    break;
	case FULLSCREEN_CMD:
	    boolean toFullscreen = data.length >= 1 ? (Boolean) data[0] : !Gdx.graphics.isFullscreen();
	    int width;
	    int height;
	    if (toFullscreen) {
		width = GlobalConf.instance.FULLSCREEN_WIDTH;
		height = GlobalConf.instance.FULLSCREEN_HEIGHT;
		GlobalConf.instance.SCREEN_WIDTH = Gdx.graphics.getWidth();
		GlobalConf.instance.SCREEN_HEIGHT = Gdx.graphics.getHeight();
	    } else {
		width = GlobalConf.instance.SCREEN_WIDTH;
		height = GlobalConf.instance.SCREEN_HEIGHT;
	    }
	    // Only switch if needed
	    if (Gdx.graphics.isFullscreen() != toFullscreen) {
		Gdx.graphics.setDisplayMode(width, height, toFullscreen);
	    }
	    break;
	default:
	    break;
	}

    }
}
