package gaia.cu9.ari.gaiaorbit;

import gaia.cu9.ari.gaiaorbit.data.AssetBean;
import gaia.cu9.ari.gaiaorbit.data.GaiaAttitudeLoader;
import gaia.cu9.ari.gaiaorbit.data.SGLoader;
import gaia.cu9.ari.gaiaorbit.data.SGLoader.SGLoaderParameter;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitDataLoader;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.FullGui;
import gaia.cu9.ari.gaiaorbit.interfce.GaiaControllerListener;
import gaia.cu9.ari.gaiaorbit.interfce.GaiaInputController;
import gaia.cu9.ari.gaiaorbit.interfce.HUDGui;
import gaia.cu9.ari.gaiaorbit.interfce.IGui;
import gaia.cu9.ari.gaiaorbit.interfce.LoadingGui;
import gaia.cu9.ari.gaiaorbit.interfce.MobileGui;
import gaia.cu9.ari.gaiaorbit.interfce.RenderGui;
import gaia.cu9.ari.gaiaorbit.render.AbstractRenderer;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.GSPostProcessor;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer;
import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.gaia.GaiaAttitudeServer;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.GlobalClock;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

/**
 * The main class. Holds all the entities manages the update/draw cycle as well
 * as the image rendering.
 * 
 * @author Toni Sagrista
 *
 */
public class GaiaSandbox implements ApplicationListener, IObserver {
    private static boolean LOADING = true;

    public static GaiaSandbox instance;

    // Asset manager
    public AssetManager manager;

    /** This handles the input events **/
    private GaiaInputController inputController;

    // Camera
    public CameraManager cam;

    public ISceneGraph sg;
    private SceneGraphRenderer sgr;
    private IPostProcessor pp;

    // Frame buffer map
    private Map<String, FrameBuffer> fbmap;

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

    /**
     * Creates a Gaia Sandbox instance.
     * 
     * @param openGLGUI
     *            This will paint the GUI in OpenGL. True for Desktop (if not
     *            Swing GUI) and Android.
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

        fbmap = new HashMap<String, FrameBuffer>();

        // Disable all kinds of input
        EventManager.instance.post(Events.INPUT_ENABLED_CMD, false);

        if (!GlobalClock.initialized()) {
            // Initialize clock with a pace of 2 simulation hours/second
            GlobalClock.initialize(0.01, new Date());
        }

        if (!GlobalConf.initialized()) {
            // Initialise the configuration if needed
            try {
                GlobalConf.initialize();
            } catch (Exception e) {
                // Android
                Logger.error(e);
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
        manager.setLoader(GaiaAttitudeServer.class, new GaiaAttitudeLoader(resolver));

        // Init global resources
        GlobalResources.initialize(manager);

        // Initialize Cameras
        cam = new CameraManager(manager, CameraMode.Focus);

        // Initialize Gaia attitudes
        manager.load("data/attitudexml/", GaiaAttitudeServer.class);

        if (sg == null) {
            // Set asset manager to asset bean
            AssetBean.setAssetManager(manager);
            manager.load(GlobalConf.data.DATA_SG_FILE, ISceneGraph.class, new SGLoaderParameter(GlobalClock.clock));
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
        loadingGui = new LoadingGui();
        loadingGui.initialize(manager);

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.glslversion", Gdx.gl.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION)));
    }

    /**
     * Execute this when the models have finished loading. This sets the models
     * to their classes and removes the Loading message
     */
    private void doneLoading() {
        loadingGui.dispose();
        loadingGui = null;

        // Get attitude
        GaiaAttitudeServer.instance = manager.get("data/attitudexml/");

        pp = new GSPostProcessor();

        GlobalResources.doneLoading(manager);

        if (manager.isLoaded(GlobalConf.data.DATA_SG_FILE)) {
            sg = manager.get(GlobalConf.data.DATA_SG_FILE);
        }

        AbstractRenderer.initialize(sg);
        sgr = new SceneGraphRenderer();
        sgr.initialize(manager);
        sgr.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // First time, set assets
        List<SceneGraphNode> nodes = sg.getNodes();
        for (SceneGraphNode sgn : nodes) {
            sgn.doneLoading(manager);
        }

        // Update whole tree to initialize positions
        OctreeNode.LOAD_ACTIVE = false;
        GlobalClock.clock.update(0.000000001f);
        sg.update(GlobalClock.clock, cam);
        GlobalClock.clock.update(0);
        OctreeNode.LOAD_ACTIVE = true;

        // Initialize input handlers
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
        EventManager.instance.post(Events.VISIBILITY_OF_COMPONENTS, new Object[] { SceneGraphRenderer.visible });

        inputController = new GaiaInputController(cam, gui);
        Controllers.addListener(new GaiaControllerListener(cam, gui));
        inputMultiplexer.addProcessor(inputController);

        Gdx.input.setInputProcessor(inputMultiplexer);

        EventManager.instance.post(Events.SCENE_GRAPH_LOADED, sg);
        EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Focus);

        AbstractPositionEntity focus = (AbstractPositionEntity) sg.getNode("Sol");
        EventManager.instance.post(Events.FOCUS_CHANGE_CMD, focus, true);
        float dst = focus.size * 3;
        Vector3d newCameraPos = focus.pos.cpy().add(0, 0, -dst);
        EventManager.instance.post(Events.CAMERA_POS_CMD, newCameraPos.values());

        // Update whole tree to reinitialize positions with the new camera
        // position
        GlobalClock.clock.update(0.00000001f);
        sg.update(GlobalClock.clock, cam);
        sgr.clearLists();
        GlobalClock.clock.update(0);

        Vector3d newCameraDir = focus.pos.cpy().sub(newCameraPos);
        EventManager.instance.post(Events.CAMERA_DIR_CMD, newCameraDir.values());

        // Initialize time in GUI
        EventManager.instance.post(Events.TIME_CHANGE_INFO, GlobalClock.clock.time);

        // Subscribe to events
        EventManager.instance.subscribe(this, Events.TOGGLE_AMBIENT_LIGHT, Events.AMBIENT_LIGHT_CMD);

        // Re-enable input
        EventManager.instance.post(Events.INPUT_ENABLED_CMD, true);

        initialized = true;

        // Run tutorial
        if (GlobalConf.program.DISPLAY_TUTORIAL) {
            EventManager.instance.post(Events.RUN_SCRIPT_PATH, "scripts/tutorial/tutorial-pointer.py");
            GlobalConf.program.DISPLAY_TUTORIAL = false;
        }

    }

    @Override
    public void dispose() {
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

            // Asynchronous load of textures and resources
            manager.update();

            if (!GlobalConf.runtime.UPDATE_PAUSE) {
                /**
                 * UPDATE
                 */
                update(Gdx.graphics.getDeltaTime());

                /** 
                 * RENDER THE SCENE
                 **/
                preRenderScene();
                sgr.render(cam, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), null, pp.getPostProcessBean());

                if (!GlobalConf.runtime.CLEAN_MODE) {
                    // Render the GUI, setting the viewport
                    gui.getGuiStage().getViewport().apply();
                    gui.render();
                }

                sgr.clearLists();
            }

        }

        EventManager.instance.post(Events.FPS_INFO, Gdx.graphics.getFramesPerSecond());
    }

    /**
     * Update method.
     * 
     * @param dt
     *            Delta time in seconds.
     */
    public void update(float dt) {

        // Max time step is 0.1 seconds. Not in RENDER_OUTPUT MODE.
        dt = Math.min(dt, 0.1f);

        gui.update(dt);
        renderGui.update(dt);

        float dtScene = dt;
        if (!GlobalConf.runtime.TIME_ON) {
            dtScene = 0;
        }
        // Update clock
        GlobalClock.clock.update(dtScene);

        // Update events
        EventManager.instance.dispatchDelayedMessages();

        // Update cameras
        cam.update(dt, GlobalClock.clock);

        // Update scene graph
        sg.update(GlobalClock.clock, cam);

    }

    public void preRenderScene() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void resize(final int width, final int height) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (!initialized) {
                    loadingGui.resize(width, height);
                } else {
                    pp.resize(width, height);
                    gui.resize(width, height);
                    sgr.resize(width, height);
                }

                cam.updateAngleEdge(width, height);

                EventManager.instance.post(Events.SCREEN_RESIZE, width, height);
            }
        });

    }

    /**
     * Renders the loading screen
     */
    private void renderLoadingScreen() {
        loadingGui.update(Gdx.graphics.getDeltaTime());
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

    private FrameBuffer getFrameBuffer(int w, int h) {
        String key = getKey(w, h);
        if (!fbmap.containsKey(key)) {
            FrameBuffer fb = new FrameBuffer(Format.RGB888, w, h, true);
            fbmap.put(key, fb);
        }
        return fbmap.get(key);
    }

    private String getKey(int w, int h) {
        return w + "x" + h;
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

        default:
            break;
        }

    }

}
