package gaia.cu9.ari.gaiaorbit;

import gaia.cu9.ari.gaiaorbit.data.AssetBean;
import gaia.cu9.ari.gaiaorbit.data.GaiaAttitudeLoader;
import gaia.cu9.ari.gaiaorbit.data.GaiaAttitudeLoader.GaiaAttitudeLoaderParameter;
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
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.RenderType;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer;
import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadPoolManager;
import gaia.cu9.ari.gaiaorbit.util.gaia.GaiaAttitudeServer;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.screenshot.BasicFileImageRenderer;
import gaia.cu9.ari.gaiaorbit.util.screenshot.BufferedFileImageRenderer;
import gaia.cu9.ari.gaiaorbit.util.screenshot.IFileImageRenderer;
import gaia.cu9.ari.gaiaorbit.util.screenshot.ImageRenderer;
import gaia.cu9.ari.gaiaorbit.util.time.GlobalClock;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.time.RealTimeClock;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.io.File;
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

    /** Attitude folder **/
    private static String ATTITUDE_FOLDER = "data/attitudexml/";

    /** Singleton instance **/
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

    /**
     * Time
     */
    public ITimeFrameProvider current;
    private ITimeFrameProvider clock, real;

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

    public IFileImageRenderer frameRenderer, screenshotRenderer;
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

        fbmap = new HashMap<String, FrameBuffer>();

        // Disable all kinds of input
        EventManager.instance.post(Events.INPUT_ENABLED_CMD, false);

        if (!GlobalConf.initialized()) {
            Logger.error(new RuntimeException("FATAL: Global configuration not initlaized"));
            return;
        }

        // Initialize times
        clock = new GlobalClock(0.000277778, new Date());
        real = new RealTimeClock();
        current = GlobalConf.runtime.REAL_TIME ? real : clock;

        // Precompute some math functions
        MathUtilsd.initialize();

        // Initialize i18n
        I18n.initialize();

        if (GlobalConf.performance.MULTITHREADING)
            // Initialize thread pool manager
            ThreadPoolManager.initialize(GlobalConf.performance.NUMBER_THREADS());

        // Init frame/screenshot renderer
        frameRenderer = new BufferedFileImageRenderer(GlobalConf.runtime.OUTPUT_FRAME_BUFFER_SIZE);
        screenshotRenderer = new BasicFileImageRenderer();

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
        manager.load(ATTITUDE_FOLDER, GaiaAttitudeServer.class, new GaiaAttitudeLoaderParameter(GlobalConf.runtime.STRIPPED_FOV_MODE ? new String[] { "OPS_RSLS_0022916_rsls_nsl_gareq1_afterFirstSpinPhaseOptimization.2.xml" } : new String[] {}));

        if (sg == null) {
            // Set asset manager to asset bean
            AssetBean.setAssetManager(manager);
            manager.load(GlobalConf.data.DATA_JSON_FILE, ISceneGraph.class, new SGLoaderParameter(current, GlobalConf.performance.MULTITHREADING, GlobalConf.performance.NUMBER_THREADS()));
        }

        // Initialize timestamp for screenshots
        renderGui = new RenderGui();
        renderGui.initialize(manager);

        if (GlobalConf.OPENGL_GUI) {
            // Load scene graph
            if (Constants.desktop || Constants.webgl) {
                // Full GUI for desktop
                gui = new FullGui();
            } else if (Constants.mobile) {
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
        if (manager.isLoaded(ATTITUDE_FOLDER)) {
            GaiaAttitudeServer.instance = manager.get(ATTITUDE_FOLDER);
        }

        pp = new GSPostProcessor();

        GlobalResources.doneLoading(manager);

        /**
         * GET SCENE GRAPH
         */
        if (manager.isLoaded(GlobalConf.data.DATA_JSON_FILE)) {
            sg = manager.get(GlobalConf.data.DATA_JSON_FILE);
        }

        /** 
         * INITIALIZE RENDERER
         */
        AbstractRenderer.initialize(sg);
        sgr = new SceneGraphRenderer();
        sgr.initialize(manager);
        sgr.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // First time, set assets
        for (SceneGraphNode sgn : sg.getNodes()) {
            sgn.doneLoading(manager);
        }

        // Update whole tree to initialize positions
        OctreeNode.LOAD_ACTIVE = false;
        current.update(0.000000001f);
        sg.update(current, cam);
        current.update(0);
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

        AbstractPositionEntity focus = null;
        Vector3d newCameraPos = null;
        if (!Constants.webgl) {
            focus = (AbstractPositionEntity) sg.getNode("Sol");
            EventManager.instance.post(Events.FOCUS_CHANGE_CMD, focus, true);
            EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Focus);
            float dst = focus.size * 3;
            newCameraPos = focus.pos.cpy().add(0, 0, -dst);
            EventManager.instance.post(Events.CAMERA_POS_CMD, newCameraPos.values());

        } else {
            focus = (AbstractPositionEntity) sg.getNode("Gaia");
            EventManager.instance.post(Events.FOCUS_CHANGE_CMD, focus, true);
            EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Gaia_FOV1and2);
        }

        // Update whole tree to reinitialize positions with the new camera
        // position
        current.update(0.0000000001f);
        sg.update(current, cam);
        sgr.clearLists();
        current.update(0);

        if (!Constants.webgl) {
            Vector3d newCameraDir = focus.pos.cpy().sub(newCameraPos);
            EventManager.instance.post(Events.CAMERA_DIR_CMD, newCameraDir.values());
        }

        // Initialize time in GUI
        EventManager.instance.post(Events.TIME_CHANGE_INFO, current.getTime());

        // Subscribe to events
        EventManager.instance.subscribe(this, Events.TOGGLE_AMBIENT_LIGHT, Events.AMBIENT_LIGHT_CMD, Events.SCREENSHOT_CMD, Events.FULLSCREEN_CMD);

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

        if (!Constants.mobile)
            ConfInit.instance.persistGlobalConf(new File(System.getProperty("properties.file")));

        frameRenderer.flush();
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
                 * FRAME OUTPUT
                 */
                if (GlobalConf.frame.RENDER_OUTPUT) {
                    switch (GlobalConf.frame.FRAME_MODE) {
                    case simple:
                        frameRenderer.saveScreenshot(GlobalConf.frame.RENDER_FOLDER, GlobalConf.frame.RENDER_FILE_NAME, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
                        break;
                    case redraw:
                        renderToImage(cam, pp.getPostProcessBean(RenderType.frame), GlobalConf.frame.RENDER_WIDTH, GlobalConf.frame.RENDER_HEIGHT, GlobalConf.frame.RENDER_FOLDER, GlobalConf.frame.RENDER_FILE_NAME, frameRenderer);
                        break;
                    }

                }

                /**
                 * SCREENSHOT OUTPUT - simple|redraw mode
                 */
                if (screenshot.active) {
                    String file = null;
                    switch (GlobalConf.screenshot.SCREENSHOT_MODE) {
                    case simple:
                        file = ImageRenderer.renderToImageGl20(screenshot.folder, ScreenshotCmd.FILENAME, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                        break;
                    case redraw:
                        file = renderToImage(cam, pp.getPostProcessBean(RenderType.screenshot), screenshot.width, screenshot.height, screenshot.folder, ScreenshotCmd.FILENAME, screenshotRenderer);
                        break;
                    }
                    if (file != null) {
                        screenshot.active = false;
                        EventManager.instance.post(Events.SCREENSHOT_INFO, file);
                    }

                }

                /**
                 * SCREEN OUTPUT
                 */
                if (GlobalConf.screen.SCREEN_OUTPUT) {
                    /** RENDER THE SCENE **/
                    preRenderScene();
                    sgr.render(cam, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), null, pp.getPostProcessBean(RenderType.screen));

                    if (!GlobalConf.runtime.CLEAN_MODE) {
                        // Render the GUI, setting the viewport
                        gui.getGuiStage().getViewport().apply();
                        gui.render();
                    }

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
        if (GlobalConf.frame.RENDER_OUTPUT) {
            // If RENDER_OUTPUT is active, we need to set our dt according to
            // the fps
            dt = 1f / GlobalConf.frame.RENDER_TARGET_FPS;
        } else {
            // Max time step is 0.1 seconds. Not in RENDER_OUTPUT MODE.
            dt = Math.min(dt, 0.1f);
        }

        gui.update(dt);
        renderGui.update(dt);

        float dtScene = dt;
        if (!GlobalConf.runtime.TIME_ON) {
            dtScene = 0;
        }
        // Update clock
        current.update(dtScene);

        // Update events
        EventManager.instance.dispatchDelayedMessages();

        // Update cameras
        cam.update(dt, current);

        // Update scene graph
        sg.update(current, cam);

    }

    public void preRenderScene() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Renders the current scene to an image and returns the file name where it
     * has been written to
     * 
     * @param camera
     * @param width
     *            The width of the image.
     * @param height
     *            The height of the image.
     * @param folder
     *            The folder to save the image to.
     * @param filename
     *            The file name prefix.
     * @param renderer
     *            the {@link IFileImageRenderer} to use.
     * @return
     */
    public String renderToImage(ICamera camera, PostProcessBean ppb, int width, int height, String folder, String filename, IFileImageRenderer renderer) {
        FrameBuffer frameBuffer = getFrameBuffer(width, height);
        // TODO That's a dirty trick, we should find a better way (i.e. making
        // buildEnabledEffectsList() method public)
        boolean postprocessing = ppb.pp.captureNoClear();
        ppb.pp.captureEnd();
        if (!postprocessing) {
            // If post processing is not active, we must start the buffer now.
            // Otherwise, it is used in the render method to write the results
            // of the pp.
            frameBuffer.begin();
        }

        // this is the main render function
        preRenderScene();
        // sgr.render(camera, width, height, postprocessing ? m_fbo : null,
        // ppb);
        sgr.render(camera, width, height, frameBuffer, ppb);

        if (postprocessing) {
            // If post processing is active, we have to start now again because
            // the renderScene() has closed it.
            frameBuffer.begin();
        }
        if (GlobalConf.frame.RENDER_SCREENSHOT_TIME) {
            // Timestamp
            renderGui.resize(width, height);
            renderGui.render();
        }

        String res = renderer.saveScreenshot(folder, filename, width, height, false);

        frameBuffer.end();
        return res;
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
        frameRenderer.flush();
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
        case SCREENSHOT_CMD:
            screenshot.takeScreenshot((int) data[0], (int) data[1], (String) data[2]);
            break;
        case FULLSCREEN_CMD:
            boolean toFullscreen = data.length >= 1 ? (Boolean) data[0] : !Gdx.graphics.isFullscreen();
            int width;
            int height;
            if (toFullscreen) {
                width = GlobalConf.screen.FULLSCREEN_WIDTH;
                height = GlobalConf.screen.FULLSCREEN_HEIGHT;
                GlobalConf.screen.SCREEN_WIDTH = Gdx.graphics.getWidth();
                GlobalConf.screen.SCREEN_HEIGHT = Gdx.graphics.getHeight();
            } else {
                width = GlobalConf.screen.SCREEN_WIDTH;
                height = GlobalConf.screen.SCREEN_HEIGHT;
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
