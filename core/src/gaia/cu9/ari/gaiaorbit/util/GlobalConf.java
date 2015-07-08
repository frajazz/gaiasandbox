package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.system.AbstractRenderSystem;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Holds the global configuration options
 * @author Toni Sagrista
 *
 */
public class GlobalConf {
    public static final String APPLICATION_NAME = "Gaia Sandbox";
    public static final String WEBPAGE = "http://www.zah.uni-heidelberg.de/gaia2/outreach/gaiasandbox/";
    public static final String WIKI = "https://github.com/ari-zah/gaiasandbox/wiki";
    public static final String ICON_URL = "http://www.zah.uni-heidelberg.de/uploads/pics/gaiasandboxlogo_02.png";

    public static boolean OPENGL_GUI;

    public static final String TEXTURES_FOLDER = "data/tex/";

    public static interface IConf {

        /**
         * Initializes this configuration from the given properties object.
         * @param p
         */
        public void initialize();

    }

    public enum ScreenshotMode {
        simple, redraw
    }

    public static class PerformanceConf implements IConf {

        public boolean MULTITHREADING;
        public int NUMBER_THREADS;

        @Override
        public void initialize() {
            MULTITHREADING = Boolean.parseBoolean(p.getProperty("global.conf.multithreading"));
            String propNumthreads = p.getProperty("global.conf.numthreads");
            NUMBER_THREADS = Integer.parseInt((propNumthreads == null || propNumthreads.isEmpty()) ? "0" : propNumthreads);
        }

        /**
         * Returns the actual number of threads. It accounts for the number of threads being 0 or less,
         * "let the program decide" option, in which case the number of processors is returned.
         * @return
         */
        public int NUMBER_THREADS() {
            if (NUMBER_THREADS <= 0)
                return Runtime.getRuntime().availableProcessors();
            else
                return NUMBER_THREADS;
        }

    }

    public static class PostprocessConf implements IConf, IObserver {

        public int POSTPROCESS_ANTIALIAS;
        public float POSTPROCESS_BLOOM_INTENSITY;
        public float POSTPROCESS_MOTION_BLUR;
        /** This should be no smaller than 1 and no bigger than 5. The bigger the more stars with labels **/
        public boolean POSTPROCESS_LENS_FLARE;

        public PostprocessConf() {
            EventManager.instance.subscribe(this, Events.BLOOM_CMD, Events.LENS_FLARE_CMD, Events.MOTION_BLUR_CMD);
        }

        @Override
        public void initialize() {
            /** POSTPROCESS **/
            /**
             * aa
             * value < 0 - FXAA
             * value = 0 - no AA
             * value > 0 - MSAA #samples = value
             */
            POSTPROCESS_ANTIALIAS = Integer.parseInt(p.getProperty("postprocess.antialiasing"));
            POSTPROCESS_BLOOM_INTENSITY = Float.parseFloat(p.getProperty("postprocess.bloom.intensity"));
            POSTPROCESS_MOTION_BLUR = Float.parseFloat(p.getProperty("postprocess.motionblur"));
            POSTPROCESS_LENS_FLARE = Boolean.parseBoolean(p.getProperty("postprocess.lensflare"));
        }

        @Override
        public void notify(Events event, Object... data) {
            switch (event) {
            case BLOOM_CMD:
                POSTPROCESS_BLOOM_INTENSITY = (float) data[0];
                break;
            case LENS_FLARE_CMD:
                POSTPROCESS_LENS_FLARE = (Boolean) data[0];
                break;
            case MOTION_BLUR_CMD:
                POSTPROCESS_MOTION_BLUR = (float) data[0];
            }
        }

    }

    /**
     * Runtime configuration values, which are never persisted.
     * @author Toni Sagrista
     *
     */
    public static class RuntimeConf implements IConf, IObserver {

        public boolean CLEAN_MODE;
        public boolean UPDATE_PAUSE;
        public boolean TIME_ON;
        public boolean INPUT_ENABLED;
        public boolean RECORD_CAMERA;
        public float LIMIT_MAG_RUNTIME;
        public int OUTPUT_FRAME_BUFFER_SIZE = 250;

        public RuntimeConf() {
            EventManager.instance.subscribe(this, Events.LIMIT_MAG_CMD, Events.INPUT_ENABLED_CMD, Events.TOGGLE_CLEANMODE, Events.TOGGLE_UPDATEPAUSE, Events.TOGGLE_TIME_CMD, Events.RECORD_CAMERA_CMD);
        }

        @Override
        public void initialize() {
            // Input always enabled by default
            INPUT_ENABLED = true;
            LIMIT_MAG_RUNTIME = 20;
            UPDATE_PAUSE = false;
            TIME_ON = false;
            RECORD_CAMERA = false;

        }

        @Override
        public void notify(Events event, Object... data) {
            switch (event) {
            case LIMIT_MAG_CMD:
                LIMIT_MAG_RUNTIME = (float) data[0];
                AbstractRenderSystem.POINT_UPDATE_FLAG = true;
                break;

            case INPUT_ENABLED_CMD:
                INPUT_ENABLED = (boolean) data[0];
                break;

            case TOGGLE_CLEANMODE:
                CLEAN_MODE = !CLEAN_MODE;
                break;
            case TOGGLE_UPDATEPAUSE:
                UPDATE_PAUSE = !UPDATE_PAUSE;
                EventManager.instance.post(Events.UPDATEPAUSE_CHANGED, UPDATE_PAUSE);
                break;
            case TOGGLE_TIME_CMD:
                toggleTimeOn((Boolean) data[0]);
                break;
            case RECORD_CAMERA_CMD:
                toggleRecord((Boolean) data[0]);
                break;
            }

        }

        /**
         * Toggles the time
         */
        public void toggleTimeOn(Boolean timeOn) {
            if (timeOn != null) {
                TIME_ON = timeOn;
            } else {
                TIME_ON = !TIME_ON;
            }
        }

        /**
         * Toggles the record camera
         */
        public void toggleRecord(Boolean rec) {
            if (rec != null) {
                RECORD_CAMERA = rec;
            } else {
                RECORD_CAMERA = !RECORD_CAMERA;
            }
        }

    }

    /**
     * Holds the configuration for the output frame subsystem.
     * @author Toni Sagrista
     *
     */
    public static class FrameConf implements IConf, IObserver {
        /** The width of the image frames **/
        public int RENDER_WIDTH;
        /** The height of the image frames **/
        public int RENDER_HEIGHT;
        /** The number of images per second to produce **/
        public int RENDER_TARGET_FPS;
        /** The output folder **/
        public String RENDER_FOLDER;
        /** The prefix for the image files **/
        public String RENDER_FILE_NAME;
        /** Should we write the simulation time to the images? **/
        public boolean RENDER_SCREENSHOT_TIME;
        /** Whether the frame system is activated or not **/
        public boolean RENDER_OUTPUT = false;
        /** The frame output screenshot mode **/
        public ScreenshotMode FRAME_MODE;

        public FrameConf() {
            EventManager.instance.subscribe(this, Events.CONFIG_PIXEL_RENDERER, Events.FRAME_OUTPUT_CMD);
        }

        @Override
        public void initialize() {
            String renderFolder = null;
            if (p.getProperty("graphics.render.folder") == null || p.getProperty("graphics.render.folder").isEmpty()) {
                File framesDir = SysUtils.getDefaultFramesDir();
                framesDir.mkdirs();
                renderFolder = framesDir.getAbsolutePath();
            } else {
                renderFolder = p.getProperty("graphics.render.folder");
            }
            RENDER_FOLDER = renderFolder;
            RENDER_FILE_NAME = p.getProperty("graphics.render.filename");
            RENDER_WIDTH = Integer.parseInt(p.getProperty("graphics.render.width"));
            RENDER_HEIGHT = Integer.parseInt(p.getProperty("graphics.render.height"));
            RENDER_TARGET_FPS = Integer.parseInt(p.getProperty("graphics.render.targetfps"));
            RENDER_SCREENSHOT_TIME = Boolean.parseBoolean(p.getProperty("graphics.render.time"));
            FRAME_MODE = ScreenshotMode.valueOf(p.getProperty("graphics.render.mode"));
        }

        @Override
        public void notify(Events event, Object... data) {
            switch (event) {
            case CONFIG_PIXEL_RENDERER:
                RENDER_WIDTH = (int) data[0];
                RENDER_HEIGHT = (int) data[1];
                RENDER_TARGET_FPS = (int) data[2];
                RENDER_FOLDER = (String) data[3];
                RENDER_FILE_NAME = (String) data[4];
                break;
            case FRAME_OUTPUT_CMD:
                if (data.length > 0) {
                    RENDER_OUTPUT = (Boolean) data[0];
                } else {
                    RENDER_OUTPUT = !RENDER_OUTPUT;
                }
                // Flush buffer if needed
                if (!RENDER_OUTPUT && GaiaSandbox.instance != null) {
                    GaiaSandbox.instance.frameRenderer.flush();
                }
            }
        }
    }

    /**
     * Holds all configuration values related to data.
     * @author Toni Sagrista
     *
     */
    public static class DataConf implements IConf {
        /** Whether we use the local data source (HYG binary) or the object server **/
        public boolean DATA_SOURCE_LOCAL = false;
        /** The .sg file in case of local data source **/
        public String DATA_SG_FILE;
        /** If we use the ObjectServer, this contains the visualization id **/
        public String VISUALIZATION_ID;
        /** Object server IP address/hostname **/
        public String OBJECT_SERVER_HOSTNAME = "localhost";
        /** Object server port **/
        public int OBJECT_SERVER_PORT = 5555;
        /** Object server user name **/
        public String OBJECT_SERVER_USERNAME;
        /** Object Server pass **/
        public String OBJECT_SERVER_PASSWORD;
        /** Limit magnitude used for loading stars. All stars above this magnitude will not even be loaded by the sandbox. **/
        public float LIMIT_MAG_LOAD;

        @Override
        public void initialize() {
            /** DATA **/
            DATA_SOURCE_LOCAL = Boolean.parseBoolean(p.getProperty("data.source.local"));
            DATA_SG_FILE = p.getProperty("data.sg.file");
            OBJECT_SERVER_HOSTNAME = p.getProperty("data.source.hostname");
            OBJECT_SERVER_PORT = Integer.parseInt(p.getProperty("data.source.port"));
            VISUALIZATION_ID = p.getProperty("data.source.visid");

            if (p.getProperty("data.limit.mag") != null && !p.getProperty("data.limit.mag").isEmpty()) {
                LIMIT_MAG_LOAD = Float.parseFloat(p.getProperty("data.limit.mag"));
            } else {
                LIMIT_MAG_LOAD = Float.MAX_VALUE;
            }
        }
    }

    public static class ScreenConf implements IConf {

        public int SCREEN_WIDTH;
        public int SCREEN_HEIGHT;
        public int FULLSCREEN_WIDTH;
        public int FULLSCREEN_HEIGHT;
        public boolean FULLSCREEN;
        public boolean RESIZABLE;
        public boolean VSYNC;
        public boolean SCREEN_OUTPUT;

        @Override
        public void initialize() {
            SCREEN_WIDTH = Integer.parseInt(p.getProperty("graphics.screen.width"));
            SCREEN_HEIGHT = Integer.parseInt(p.getProperty("graphics.screen.height"));
            FULLSCREEN_WIDTH = Integer.parseInt(p.getProperty("graphics.screen.fullscreen.width"));
            FULLSCREEN_HEIGHT = Integer.parseInt(p.getProperty("graphics.screen.fullscreen.height"));
            FULLSCREEN = Boolean.parseBoolean(p.getProperty("graphics.screen.fullscreen"));
            RESIZABLE = Boolean.parseBoolean(p.getProperty("graphics.screen.resizable"));
            VSYNC = Boolean.parseBoolean(p.getProperty("graphics.screen.vsync"));
            SCREEN_OUTPUT = Boolean.parseBoolean(p.getProperty("graphics.screen.screenoutput"));
        }

        public int getScreenWidth() {
            return FULLSCREEN ? FULLSCREEN_WIDTH : SCREEN_WIDTH;
        }

        public int getScreenHeight() {
            return FULLSCREEN ? FULLSCREEN_HEIGHT : SCREEN_HEIGHT;
        }

    }

    public static class ProgramConf implements IConf, IObserver {

        public static enum StereoProfile {
            /** Left image -> left eye, no distortion **/
            VR_HEADSET,
            /** Left image -> left eye, distortion **/
            HD_3DTV,
            /** Left image -> right eye, no distortion **/
            CROSSEYE
        }

        public boolean DISPLAY_TUTORIAL;
        public String TUTORIAL_SCRIPT_LOCATION;
        public boolean SHOW_CONFIG_DIALOG;
        public boolean SHOW_DEBUG_INFO;
        public Date LAST_CHECKED;
        public String LAST_VERSION;
        public String VERSION_CHECK_URL;
        public String UI_THEME;
        public String SCRIPT_LOCATION;
        public String LOCALE;
        public boolean STEREOSCOPIC_MODE;
        /** Eye separation in stereoscopic mode in meters **/
        public float STEREOSCOPIC_EYE_SEPARATION_M = 1;
        /** This controls the side of the images in the stereoscopic mode **/
        public StereoProfile STEREO_PROFILE = StereoProfile.VR_HEADSET;

        private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        public ProgramConf() {
            EventManager.instance.subscribe(this, Events.TOGGLE_STEREOSCOPIC, Events.TOGGLE_STEREO_PROFILE);
        }

        @Override
        public void initialize() {
            LOCALE = p.getProperty("program.locale");

            DISPLAY_TUTORIAL = Boolean.parseBoolean(p.getProperty("program.tutorial"));
            TUTORIAL_SCRIPT_LOCATION = p.getProperty("program.tutorial.script");
            SHOW_CONFIG_DIALOG = Boolean.parseBoolean(p.getProperty("program.configdialog"));
            SHOW_DEBUG_INFO = Boolean.parseBoolean(p.getProperty("program.debuginfo"));
            try {
                LAST_CHECKED = p.getProperty("program.lastchecked").isEmpty() ? null : df.parse(p.getProperty("program.lastchecked"));
            } catch (ParseException e) {
                Logger.error(e);
            }
            LAST_VERSION = p.getProperty("program.lastversion");
            VERSION_CHECK_URL = p.getProperty("program.versioncheckurl");
            UI_THEME = p.getProperty("program.ui.theme");
            SCRIPT_LOCATION = p.getProperty("program.scriptlocation").isEmpty() ? System.getProperty("user.dir") : p.getProperty("program.scriptlocation");

            STEREOSCOPIC_MODE = Boolean.parseBoolean(p.getProperty("program.stereoscopic"));
            STEREO_PROFILE = StereoProfile.values()[Integer.parseInt(p.getProperty("program.stereoscopic.profile"))];
        }

        public String getLastCheckedString() {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, I18n.locale);
            return df.format(LAST_CHECKED);
        }

        @Override
        public void notify(Events event, Object... data) {
            switch (event) {
            case TOGGLE_STEREOSCOPIC:
                STEREOSCOPIC_MODE = !STEREOSCOPIC_MODE;
                break;
            case TOGGLE_STEREO_PROFILE:
                int idx = STEREO_PROFILE.ordinal();
                StereoProfile[] vals = StereoProfile.values();
                idx = (idx + 1) % vals.length;
                STEREO_PROFILE = vals[idx];
                break;
            }
        }

    }

    public static class VersionConf implements IConf {
        public String version;

        @Override
        public void initialize() {
            version = "0.706b";
        }

    }

    public static class SceneConf implements IConf, IObserver {
        public long OBJECT_FADE_MS;
        public float STAR_BRIGHTNESS;
        public float AMBIENT_LIGHT;
        public int CAMERA_FOV;
        public float CAMERA_SPEED;
        public float TURNING_SPEED;
        public float ROTATION_SPEED;
        public int CAMERA_SPEED_LIMIT_IDX;
        public double CAMERA_SPEED_LIMIT;
        public boolean FOCUS_LOCK;
        public float LABEL_NUMBER_FACTOR;
        public boolean[] VISIBILITY;
        public boolean STAR_COLOR_TRANSIT;
        public boolean ONLY_OBSERVED_STARS;
        public boolean COMPUTE_GAIA_SCAN;
        /** The pixel render system: 0 - normal, 1 - bloom, 2 - fuzzy **/
        public int PIXEL_RENDERER;
        /** The line render system: 0 - normal, 1 - shader **/
        public int LINE_RENDERER;

        public double STAR_TH_ANGLE_NONE;
        public double STAR_TH_ANGLE_POINT;
        public double STAR_TH_ANGLE_QUAD;

        public float POINT_ALPHA_MIN;
        public float POINT_ALPHA_MAX;

        public SceneConf() {
            EventManager.instance.subscribe(this, Events.FOCUS_LOCK_CMD, Events.STAR_BRIGHTNESS_CMD, Events.FOV_CHANGED_CMD, Events.CAMERA_SPEED_CMD, Events.ROTATION_SPEED_CMD, Events.TURNING_SPEED_CMD, Events.SPEED_LIMIT_CMD, Events.TRANSIT_COLOUR_CMD, Events.ONLY_OBSERVED_STARS_CMD, Events.COMPUTE_GAIA_SCAN_CMD, Events.PIXEL_RENDERER_CMD);
        }

        @Override
        public void initialize() {
            OBJECT_FADE_MS = Long.parseLong(p.getProperty("scene.object.fadems"));
            STAR_BRIGHTNESS = Float.parseFloat(p.getProperty("scene.star.brightness"));
            AMBIENT_LIGHT = Float.parseFloat(p.getProperty("scene.ambient"));
            CAMERA_FOV = Integer.parseInt(p.getProperty("scene.camera.fov"));
            CAMERA_SPEED_LIMIT_IDX = Integer.parseInt(p.getProperty("scene.camera.speedlimit"));
            updateSpeedLimit();
            CAMERA_SPEED = Float.parseFloat(p.getProperty("scene.camera.focus.vel"));
            FOCUS_LOCK = Boolean.parseBoolean(p.getProperty("scene.focuslock"));
            TURNING_SPEED = Float.parseFloat(p.getProperty("scene.camera.turn.vel"));
            ROTATION_SPEED = Float.parseFloat(p.getProperty("scene.camera.rotate.vel"));
            LABEL_NUMBER_FACTOR = Float.parseFloat(p.getProperty("scene.labelfactor"));
            STAR_TH_ANGLE_QUAD = Math.toRadians(Double.parseDouble(p.getProperty("scene.star.thresholdangle.quad")));
            STAR_TH_ANGLE_POINT = Math.toRadians(Double.parseDouble(p.getProperty("scene.star.thresholdangle.point")));
            STAR_TH_ANGLE_NONE = Math.toRadians(Double.parseDouble(p.getProperty("scene.star.thresholdangle.none")));
            POINT_ALPHA_MIN = Float.parseFloat(p.getProperty("scene.point.alpha.min"));
            POINT_ALPHA_MAX = Float.parseFloat(p.getProperty("scene.point.alpha.max"));
            PIXEL_RENDERER = Integer.parseInt(p.getProperty("scene.renderer.star"));
            LINE_RENDERER = Integer.parseInt(p.getProperty("scene.renderer.line"));
            //Visibility of components
            ComponentType[] cts = ComponentType.values();
            VISIBILITY = new boolean[cts.length];
            for (ComponentType ct : cts) {
                String key = "scene.visibility." + ct.name();
                if (p.containsKey(key)) {
                    VISIBILITY[ct.ordinal()] = Boolean.parseBoolean(p.getProperty(key));
                }
            }
        }

        public void updateSpeedLimit() {
            switch (CAMERA_SPEED_LIMIT_IDX) {
            case 0:
                // 100 km/h is 0.027 km/s
                CAMERA_SPEED_LIMIT = 0.0277777778 * Constants.KM_TO_U;
                break;
            case 1:
            case 2:
                // 1 c and 2 c
                CAMERA_SPEED_LIMIT = CAMERA_SPEED_LIMIT_IDX * 3e8 * Constants.M_TO_U;
                break;
            case 3:
                // 10 c
                CAMERA_SPEED_LIMIT = 10 * 3e8 * Constants.M_TO_U;
                break;
            case 4:
                //1000 c
                CAMERA_SPEED_LIMIT = 1000 * 3e8 * Constants.M_TO_U;
                break;
            case 5:
                CAMERA_SPEED_LIMIT = 1 * Constants.AU_TO_U;
                break;
            case 6:
                CAMERA_SPEED_LIMIT = 10 * Constants.AU_TO_U;
                break;
            case 7:
                CAMERA_SPEED_LIMIT = 1000 * Constants.AU_TO_U;
                break;
            case 8:
                CAMERA_SPEED_LIMIT = 10000 * Constants.AU_TO_U;
                break;
            case 9:
            case 10:
                // 1 pc/s and 2 pc/s
                CAMERA_SPEED_LIMIT = (CAMERA_SPEED_LIMIT_IDX - 8) * Constants.PC_TO_U;
                break;
            case 11:
                // 10 pc/s
                CAMERA_SPEED_LIMIT = 10 * Constants.PC_TO_U;
                break;
            case 12:
                // 1000 pc/s
                CAMERA_SPEED_LIMIT = 1000 * Constants.PC_TO_U;
                break;
            case 13:
                // No limit
                CAMERA_SPEED_LIMIT = -1;
                break;
            }
        }

        @Override
        public void notify(Events event, Object... data) {
            switch (event) {
            case TRANSIT_COLOUR_CMD:
                STAR_COLOR_TRANSIT = (boolean) data[1];
                break;
            case ONLY_OBSERVED_STARS_CMD:
                ONLY_OBSERVED_STARS = (boolean) data[1];
                break;
            case COMPUTE_GAIA_SCAN_CMD:
                COMPUTE_GAIA_SCAN = (boolean) data[1];
                break;
            case FOCUS_LOCK_CMD:
                FOCUS_LOCK = (boolean) data[1];
                break;

            case STAR_BRIGHTNESS_CMD:
                STAR_BRIGHTNESS = (float) data[0];
                break;
            case FOV_CHANGED_CMD:
                CAMERA_FOV = MathUtilsd.clamp(((Float) data[0]).intValue(), Constants.MIN_FOV, Constants.MAX_FOV);
                break;

            case CAMERA_SPEED_CMD:
                CAMERA_SPEED = (float) data[0];
                break;
            case ROTATION_SPEED_CMD:
                ROTATION_SPEED = (float) data[0];
                break;
            case TURNING_SPEED_CMD:
                TURNING_SPEED = (float) data[0];
                break;
            case SPEED_LIMIT_CMD:
                CAMERA_SPEED_LIMIT_IDX = (Integer) data[0];
                updateSpeedLimit();
                break;
            case PIXEL_RENDERER_CMD:
                PIXEL_RENDERER = (Integer) data[0];
                break;
            }

        }

        public boolean isNormalPixelRenderer() {
            return PIXEL_RENDERER == 0;
        }

        public boolean isBloomPixelRenderer() {
            return PIXEL_RENDERER == 1;
        }

        public boolean isFuzzyPixelRenderer() {
            return PIXEL_RENDERER == 2;
        }

        public boolean isNormalLineRenderer() {
            return LINE_RENDERER == 0;
        }

        public boolean isQuadLineRenderer() {
            return LINE_RENDERER == 1;
        }
    }

    public static List<IConf> configurations;

    public static FrameConf frame;
    public static ScreenConf screen;
    public static ProgramConf program;
    public static DataConf data;
    public static SceneConf scene;
    public static RuntimeConf runtime;
    public static PerformanceConf performance;
    public static PostprocessConf postprocess;
    public static VersionConf version;

    static boolean initialized = false;

    public GlobalConf() {
        super();
    }

    public static boolean initialized() {
        return initialized;
    }

    /**
     * Initializes the properties
     */
    public static void initialize() throws Exception {

        if (configurations == null) {
            configurations = new ArrayList<IConf>();
        }

        if (version == null) {
            version = new VersionConf();
            version.initialize();
        }

        if (frame == null) {
            frame = new FrameConf();
            configurations.add(frame);
        }

        if (screen == null) {
            screen = new ScreenConf();
            configurations.add(screen);
        }

        if (program == null) {
            program = new ProgramConf();
            configurations.add(program);
        }

        if (scene == null) {
            scene = new SceneConf();
            configurations.add(scene);
        }

        if (data == null) {
            data = new DataConf();
            configurations.add(data);
        }

        if (runtime == null) {
            runtime = new RuntimeConf();
            configurations.add(runtime);
        }

        if (postprocess == null) {
            postprocess = new PostprocessConf();
            configurations.add(postprocess);
        }

        if (performance == null) {
            performance = new PerformanceConf();
            configurations.add(performance);
        }

        initialize();

        initialized = true;

    }

    /**
     * Runs the initialize method in all the configurations using the given properties file stream.
     * @param propsFile An input stream sourced in the configuration file.
     * @throws Exception
     */
    public static void initializeProps() throws Exception {

        try {
            for (IConf conf : configurations) {
                conf.initialize();
            }

        } catch (Exception e) {
            Logger.error(e);
            throw (e);
        }

    }

    public static String getFullApplicationName() {
        return APPLICATION_NAME + " - " + version.version;
    }

}
