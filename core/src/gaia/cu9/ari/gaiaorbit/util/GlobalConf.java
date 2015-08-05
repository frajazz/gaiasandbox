package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.system.AbstractRenderSystem;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

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
            MULTITHREADING = false;

            NUMBER_THREADS = 1;
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
            POSTPROCESS_ANTIALIAS = 4;
            POSTPROCESS_BLOOM_INTENSITY = 0;
            POSTPROCESS_MOTION_BLUR = 0;
            POSTPROCESS_LENS_FLARE = false;
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
            EventManager.instance.subscribe(this, Events.LIMIT_MAG_CMD, Events.INPUT_ENABLED_CMD, Events.TOGGLE_CLEANMODE, Events.TOGGLE_UPDATEPAUSE, Events.TOGGLE_TIME_CMD);
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
     * Holds all configuration values related to data.
     * @author Toni Sagrista
     *
     */
    public static class DataConf implements IConf {
        /** Whether we use the local data source (HYG binary) or the object server **/
        public boolean DATA_SOURCE_LOCAL = false;
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
            DATA_SOURCE_LOCAL = true;

            LIMIT_MAG_LOAD = 15f;
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

        public ProgramConf() {
            EventManager.instance.subscribe(this, Events.TOGGLE_STEREOSCOPIC, Events.TOGGLE_STEREO_PROFILE);
        }

        @Override
        public void initialize() {
            LOCALE = "en-GB";

            DISPLAY_TUTORIAL = false;
            SHOW_DEBUG_INFO = true;
            UI_THEME = "dark";

            STEREOSCOPIC_MODE = false;
            STEREO_PROFILE = StereoProfile.CROSSEYE;
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
            OBJECT_FADE_MS = 2000;
            STAR_BRIGHTNESS = 1f;
            AMBIENT_LIGHT = 0.0f;
            CAMERA_FOV = 50;
            CAMERA_SPEED_LIMIT_IDX = 13;
            this.updateSpeedLimit();
            CAMERA_SPEED = 2.1f;
            FOCUS_LOCK = true;
            TURNING_SPEED = 1866f;
            ROTATION_SPEED = 2286f;
            LABEL_NUMBER_FACTOR = 7.0f;
            STAR_TH_ANGLE_QUAD = 0.0f;
            STAR_TH_ANGLE_POINT = 2e-8f;
            STAR_TH_ANGLE_NONE = 0f;
            POINT_ALPHA_MIN = 0.05f;
            POINT_ALPHA_MAX = 1f;
            PIXEL_RENDERER = 2;
            LINE_RENDERER = 0;
            //Visibility of components
            ComponentType[] cts = ComponentType.values();
            VISIBILITY = new boolean[cts.length];
            VISIBILITY[ComponentType.Stars.ordinal()] = true;
            VISIBILITY[ComponentType.Atmospheres.ordinal()] = true;
            VISIBILITY[ComponentType.Planets.ordinal()] = true;
            VISIBILITY[ComponentType.Moons.ordinal()] = true;
            VISIBILITY[ComponentType.Orbits.ordinal()] = true;
            VISIBILITY[ComponentType.Satellites.ordinal()] = true;
            VISIBILITY[ComponentType.MilkyWay.ordinal()] = true;
            VISIBILITY[ComponentType.Asteroids.ordinal()] = true;
            VISIBILITY[ComponentType.Galaxies.ordinal()] = true;
            VISIBILITY[ComponentType.Others.ordinal()] = true;

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
        if (!initialized) {
            if (configurations == null) {
                configurations = new ArrayList<IConf>();
            }

            if (version == null) {
                version = new VersionConf();
                version.initialize();
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

            initializeProps();

            initialized = true;
        }

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
