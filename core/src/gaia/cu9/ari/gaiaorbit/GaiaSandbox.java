package gaia.cu9.ari.gaiaorbit;

import gaia.cu9.ari.gaiaorbit.data.AssetBean;
import gaia.cu9.ari.gaiaorbit.data.BoundaryDataLoader;
import gaia.cu9.ari.gaiaorbit.data.ConstellationDataLoader;
import gaia.cu9.ari.gaiaorbit.data.GaiaAttitudeLoader;
import gaia.cu9.ari.gaiaorbit.data.HYGCatalogLoader;
import gaia.cu9.ari.gaiaorbit.data.JsonDataLoader;
import gaia.cu9.ari.gaiaorbit.data.bean.BoundariesBean;
import gaia.cu9.ari.gaiaorbit.data.bean.ConstellationsBean;
import gaia.cu9.ari.gaiaorbit.data.bean.HYGBean;
import gaia.cu9.ari.gaiaorbit.data.bean.JsonBean;
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
import gaia.cu9.ari.gaiaorbit.interfce.RenderGui;
import gaia.cu9.ari.gaiaorbit.render.AbstractRenderer;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.GSPostProcessor;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.DataFilesFactory;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.IDataFiles;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.gaia.GaiaAttitudeServer;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.time.GlobalClock;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

import java.util.ArrayList;
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
import com.badlogic.gdx.utils.GdxRuntimeException;

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

    /** Data files to load **/
    private IDataFiles dataFiles;

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

        if (!GlobalClock.initialized()) {
            // Initialize clock with a real time pace
            GlobalClock.initialize(0.000277778, new Date());
        }

        if (!GlobalConf.initialized()) {
            Logger.error(new RuntimeException("FATAL: Global configuration not initlaized"));
            return;
        }

        // Init data files
        dataFiles = DataFilesFactory.getDataFiles();

        // Precompute some math functions
        MathUtilsd.initialize();

        // Initialize i18n
        I18n.initialize();

        // Initialize asset manager
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager = new AssetManager(resolver);
        manager.setLoader(HYGBean.class, new HYGCatalogLoader(resolver));
        manager.setLoader(JsonBean.class, new JsonDataLoader(resolver));
        manager.setLoader(ConstellationsBean.class, new ConstellationDataLoader(resolver));
        manager.setLoader(BoundariesBean.class, new BoundaryDataLoader(resolver));
        manager.setLoader(OrbitData.class, new OrbitDataLoader(resolver));
        manager.setLoader(GaiaAttitudeServer.class, new GaiaAttitudeLoader(resolver));

        // Init global resources
        GlobalResources.initialize(manager);

        // Initialize Cameras
        cam = new CameraManager(manager, CameraMode.Focus);

        // Set asset manager to asset bean
        AssetBean.setAssetManager(manager);

        // Initialize Gaia attitudes
        manager.load(ATTITUDE_FOLDER, GaiaAttitudeServer.class);

        // Load catalogue
        if (dataFiles.getCatalogFiles() != null)
            manager.load(dataFiles.getCatalogFiles(), HYGBean.class);
        // Load json files
        if (dataFiles.getJsonFiles() != null)
            manager.load(dataFiles.getJsonFiles(), JsonBean.class);
        // Load constellations
        if (dataFiles.getConstellationFiles() != null)
            manager.load(dataFiles.getConstellationFiles(), ConstellationsBean.class);
        // Load boundaries
        if (dataFiles.getBoundaryFiles() != null)
            manager.load(dataFiles.getBoundaryFiles(), BoundariesBean.class);

        // Initialize timestamp for screenshots
        renderGui = new RenderGui();
        renderGui.initialize(manager);

        if (GlobalConf.OPENGL_GUI) {
            gui = new FullGui();
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
         * GET ALL NODES (stars, json, constel, boundaries)
         */
        List<SceneGraphNode> nodeList;
        HYGBean hygbean;
        JsonBean jsonbean;
        ConstellationsBean constelbean;
        BoundariesBean boundbean;
        try {
            hygbean = manager.get(dataFiles.getCatalogFiles());
        } catch (GdxRuntimeException | NullPointerException e) {
            hygbean = new HYGBean();
        }
        try {
            jsonbean = manager.get(dataFiles.getJsonFiles());
        } catch (GdxRuntimeException | NullPointerException e) {
            jsonbean = new JsonBean();
        }
        try {
            constelbean = manager.get(dataFiles.getConstellationFiles());
        } catch (GdxRuntimeException | NullPointerException e) {
            constelbean = new ConstellationsBean();
        }
        try {
            boundbean = manager.get(dataFiles.getBoundaryFiles());
        } catch (GdxRuntimeException | NullPointerException e) {
            boundbean = new BoundariesBean();
        }

        int n = hygbean.size() + jsonbean.size() + constelbean.size() + boundbean.size();
        nodeList = new ArrayList<SceneGraphNode>(n);

        nodeList.addAll(hygbean.list());
        nodeList.addAll(jsonbean.list());
        nodeList.addAll(constelbean.list());
        nodeList.addAll(boundbean.list());

        sg = new SceneGraph();
        sg.initialize(nodeList, GlobalClock.clock);

        /** 
         * INITIALIZE RENDERER
         */
        AbstractRenderer.initialize(sg);
        sgr = new SceneGraphRenderer();
        sgr.initialize(manager);
        sgr.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // First time, set assets
        for (SceneGraphNode sgn : nodeList) {
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

        // Set focus to Gaia
        EventManager.instance.post(Events.FOCUS_CHANGE_CMD, "Gaia", true);

        EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Gaia_FOV1and2);

        // Update whole tree to reinitialize positions with the new camera
        // position
        GlobalClock.clock.update(0.0000000001f);
        sg.update(GlobalClock.clock, cam);
        sgr.clearLists();
        GlobalClock.clock.update(0);

        //        Vector3d newCameraDir = focus.pos.cpy().sub(newCameraPos);
        //        EventManager.instance.post(Events.CAMERA_DIR_CMD, newCameraDir.values());

        // Initialize time in GUI
        EventManager.instance.post(Events.TIME_CHANGE_INFO, GlobalClock.clock.time);

        // Subscribe to events
        EventManager.instance.subscribe(this, Events.TOGGLE_AMBIENT_LIGHT, Events.AMBIENT_LIGHT_CMD);

        // Re-enable input
        EventManager.instance.post(Events.INPUT_ENABLED_CMD, true);

        // Set current date
        EventManager.instance.post(Events.TIME_CHANGE_CMD, new Date());

        // Activate time
        EventManager.instance.post(Events.TOGGLE_TIME_CMD, true, false);

        initialized = true;
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
