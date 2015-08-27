package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.system.AbstractRenderSystem;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory.DateType;
import gaia.cu9.ari.gaiaorbit.util.format.IDateFormat;
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
    public static final String WEBPAGE = "http://www.zah.uni-heidelberg.de/gaia/outreach/gaiasandbox/";
    public static final String WIKI = "https://github.com/ari-zah/gaiasandbox/wiki";
    public static final String ICON_URL = "http://www.zah.uni-heidelberg.de/uploads/pics/gaiasandboxlogo_02.png";

    public static boolean OPENGL_GUI;

    public static final String TEXTURES_FOLDER = "data/tex/";

    public static interface IConf {
    }

    public enum ScreenshotMode {
        simple, redraw
    }

    public static class ScreenshotConf implements IConf {

        public int SCREENSHOT_WIDTH;
        public int SCREENSHOT_HEIGHT;
        public String SCREENSHOT_FOLDER;
        public ScreenshotMode SCREENSHOT_MODE;

        public void initialize(int sCREENSHOT_WIDTH, int sCREENSHOT_HEIGHT, String sCREENSHOT_FOLDER, ScreenshotMode sCREENSHOT_MODE) {
            SCREENSHOT_WIDTH = sCREENSHOT_WIDTH;
            SCREENSHOT_HEIGHT = sCREENSHOT_HEIGHT;
            SCREENSHOT_FOLDER = sCREENSHOT_FOLDER;
            SCREENSHOT_MODE = sCREENSHOT_MODE;
        }

        public boolean isSimpleMode() {
            return SCREENSHOT_MODE.equals(ScreenshotMode.simple);
        }

        public boolean isRedrawMode() {
            return SCREENSHOT_MODE.equals(ScreenshotMode.redraw);
        }

    }

    public static class PerformanceConf implements IConf {

        public boolean MULTITHREADING;
        public int NUMBER_THREADS;

        public void initialize(boolean MULTITHREADING, int NUMBER_THREADS) {
            this.MULTITHREADING = MULTITHREADING;
            this.NUMBER_THREADS = NUMBER_THREADS;
        }

        public int NUMBER_THREADS() {
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

        public void initialize(int POSTPROCESS_ANTIALIAS, float POSTPROCESS_BLOOM_INTENSITY, float POSTPROCESS_MOTION_BLUR, boolean POSTPROCESS_LENS_FLARE) {
            this.POSTPROCESS_ANTIALIAS = POSTPROCESS_ANTIALIAS;
            this.POSTPROCESS_BLOOM_INTENSITY = POSTPROCESS_BLOOM_INTENSITY;
            this.POSTPROCESS_MOTION_BLUR = POSTPROCESS_MOTION_BLUR;
            this.POSTPROCESS_LENS_FLARE = POSTPROCESS_LENS_FLARE;
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
        /** Whether we use the RealTimeClock or the GlobalClock **/
        public boolean REAL_TIME;
        public boolean INPUT_ENABLED;
        public boolean RECORD_CAMERA;
        public float LIMIT_MAG_RUNTIME;
        public int OUTPUT_FRAME_BUFFER_SIZE = 250;
        public boolean STRIPPED_FOV_MODE = false;

        public RuntimeConf() {
            EventManager.instance.subscribe(this, Events.LIMIT_MAG_CMD, Events.INPUT_ENABLED_CMD, Events.TOGGLE_CLEANMODE, Events.TOGGLE_UPDATEPAUSE, Events.TOGGLE_TIME_CMD, Events.RECORD_CAMERA_CMD);
        }

        public void initialize(boolean cLEAN_MODE, boolean uPDATE_PAUSE, boolean sTRIPPED_FOV_MODE, boolean tIME_ON, boolean iNPUT_ENABLED, boolean rECORD_CAMERA, float lIMIT_MAG_RUNTIME, boolean rEAL_TIME) {
            CLEAN_MODE = cLEAN_MODE;
            UPDATE_PAUSE = uPDATE_PAUSE;
            TIME_ON = tIME_ON;
            INPUT_ENABLED = iNPUT_ENABLED;
            RECORD_CAMERA = rECORD_CAMERA;
            LIMIT_MAG_RUNTIME = lIMIT_MAG_RUNTIME;
            STRIPPED_FOV_MODE = sTRIPPED_FOV_MODE;
            REAL_TIME = rEAL_TIME;
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

        public void initialize(int rENDER_WIDTH, int rENDER_HEIGHT, int rENDER_TARGET_FPS, String rENDER_FOLDER, String rENDER_FILE_NAME, boolean rENDER_SCREENSHOT_TIME, boolean rENDER_OUTPUT, ScreenshotMode fRAME_MODE) {
            RENDER_WIDTH = rENDER_WIDTH;
            RENDER_HEIGHT = rENDER_HEIGHT;
            RENDER_TARGET_FPS = rENDER_TARGET_FPS;
            RENDER_FOLDER = rENDER_FOLDER;
            RENDER_FILE_NAME = rENDER_FILE_NAME;
            RENDER_SCREENSHOT_TIME = rENDER_SCREENSHOT_TIME;
            RENDER_OUTPUT = rENDER_OUTPUT;
            FRAME_MODE = fRAME_MODE;
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
                    //GaiaSandbox.instance.frameRenderer.flush();
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
        /** The json data file in case of local data source **/
        public String DATA_JSON_FILE;
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

        public void initialize(boolean dATA_SOURCE_LOCAL, String dATA_JSON_FILE, String oBJECT_SERVER_HOSTNAME, int oBJECT_SERVER_PORT, String vISUALIZATION_ID, float lIMIT_MAG_LOAD) {
            DATA_SOURCE_LOCAL = dATA_SOURCE_LOCAL;
            DATA_JSON_FILE = dATA_JSON_FILE;
            OBJECT_SERVER_HOSTNAME = oBJECT_SERVER_HOSTNAME;
            OBJECT_SERVER_PORT = oBJECT_SERVER_PORT;
            VISUALIZATION_ID = vISUALIZATION_ID;
            LIMIT_MAG_LOAD = lIMIT_MAG_LOAD;
        }

        public void initialize(String dATA_JSON_FILE, boolean dATA_SOURCE_LOCAL, float lIMIT_MAG_LOAD) {
            this.DATA_JSON_FILE = dATA_JSON_FILE;
            this.DATA_SOURCE_LOCAL = dATA_SOURCE_LOCAL;
            this.LIMIT_MAG_LOAD = lIMIT_MAG_LOAD;
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

        public void initialize(int sCREEN_WIDTH, int sCREEN_HEIGHT, int fULLSCREEN_WIDTH, int fULLSCREEN_HEIGHT, boolean fULLSCREEN, boolean rESIZABLE, boolean vSYNC, boolean sCREEN_OUTPUT) {
            SCREEN_WIDTH = sCREEN_WIDTH;
            SCREEN_HEIGHT = sCREEN_HEIGHT;
            FULLSCREEN_WIDTH = fULLSCREEN_WIDTH;
            FULLSCREEN_HEIGHT = fULLSCREEN_HEIGHT;
            FULLSCREEN = fULLSCREEN;
            RESIZABLE = rESIZABLE;
            VSYNC = vSYNC;
            SCREEN_OUTPUT = sCREEN_OUTPUT;
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

        private IDateFormat df = DateFormatFactory.getFormatter("dd/MM/yyyy HH:mm:ss");

        public ProgramConf() {
            EventManager.instance.subscribe(this, Events.TOGGLE_STEREOSCOPIC, Events.TOGGLE_STEREO_PROFILE);
        }

        public void initialize(boolean dISPLAY_TUTORIAL, String tUTORIAL_SCRIPT_LOCATION, boolean sHOW_CONFIG_DIALOG, boolean sHOW_DEBUG_INFO, Date lAST_CHECKED, String lAST_VERSION, String vERSION_CHECK_URL, String uI_THEME, String sCRIPT_LOCATION, String lOCALE, boolean sTEREOSCOPIC_MODE, StereoProfile sTEREO_PROFILE) {
            DISPLAY_TUTORIAL = dISPLAY_TUTORIAL;
            TUTORIAL_SCRIPT_LOCATION = tUTORIAL_SCRIPT_LOCATION;
            SHOW_CONFIG_DIALOG = sHOW_CONFIG_DIALOG;
            SHOW_DEBUG_INFO = sHOW_DEBUG_INFO;
            LAST_CHECKED = lAST_CHECKED;
            LAST_VERSION = lAST_VERSION;
            VERSION_CHECK_URL = vERSION_CHECK_URL;
            UI_THEME = uI_THEME;
            SCRIPT_LOCATION = sCRIPT_LOCATION;
            LOCALE = lOCALE;
            STEREOSCOPIC_MODE = sTEREOSCOPIC_MODE;
            STEREO_PROFILE = sTEREO_PROFILE;
        }

        public void initialize(boolean dISPLAY_TUTORIAL, boolean sHOW_DEBUG_INFO, String uI_THEME, String lOCALE, boolean sTEREOSCOPIC_MODE, StereoProfile sTEREO_PROFILE) {
            DISPLAY_TUTORIAL = dISPLAY_TUTORIAL;
            SHOW_DEBUG_INFO = sHOW_DEBUG_INFO;
            UI_THEME = uI_THEME;
            LOCALE = lOCALE;
            STEREOSCOPIC_MODE = sTEREOSCOPIC_MODE;
            STEREO_PROFILE = sTEREO_PROFILE;
        }

        public String getLastCheckedString() {
            IDateFormat df = DateFormatFactory.getFormatter(I18n.locale, DateType.DATE);
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
        public String buildtime;
        public String builder;
        public String system;
        public String build;
        public int major;
        public int minor;

        public void initialize(String version, String buildtime, String builder, String system, String build, int major, int minor) {
            this.version = version;
            this.buildtime = buildtime;
            this.builder = builder;
            this.system = system;
            this.build = build;
            this.major = major;
            this.minor = minor;
        }

        public static int[] getMajorMinorFromString(String version) {
            String majorS = version.substring(0, version.indexOf("."));
            String minorS = version.substring(version.indexOf(".") + 1, version.length());
            if (majorS.matches("^\\D{1}\\d+$")) {
                majorS = majorS.substring(1, majorS.length());
            }
            if (minorS.matches("^\\d+\\D{1}$")) {
                minorS = minorS.substring(0, minorS.length() - 1);
            }
            return new int[] { Integer.parseInt(majorS), Integer.parseInt(minorS) };
        }

        @Override
        public String toString() {
            return version;
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

        public void initialize(long oBJECT_FADE_MS, float sTAR_BRIGHTNESS, float aMBIENT_LIGHT, int cAMERA_FOV, float cAMERA_SPEED, float tURNING_SPEED, float rOTATION_SPEED, int cAMERA_SPEED_LIMIT_IDX, boolean fOCUS_LOCK, float lABEL_NUMBER_FACTOR, boolean[] vISIBILITY, int pIXEL_RENDERER, int lINE_RENDERER, double sTAR_TH_ANGLE_NONE, double sTAR_TH_ANGLE_POINT, double sTAR_TH_ANGLE_QUAD, float pOINT_ALPHA_MIN, float pOINT_ALPHA_MAX) {
            OBJECT_FADE_MS = oBJECT_FADE_MS;
            STAR_BRIGHTNESS = sTAR_BRIGHTNESS;
            AMBIENT_LIGHT = aMBIENT_LIGHT;
            CAMERA_FOV = cAMERA_FOV;
            CAMERA_SPEED = cAMERA_SPEED;
            TURNING_SPEED = tURNING_SPEED;
            ROTATION_SPEED = rOTATION_SPEED;
            CAMERA_SPEED_LIMIT_IDX = cAMERA_SPEED_LIMIT_IDX;
            this.updateSpeedLimit();
            FOCUS_LOCK = fOCUS_LOCK;
            LABEL_NUMBER_FACTOR = lABEL_NUMBER_FACTOR;
            VISIBILITY = vISIBILITY;
            PIXEL_RENDERER = pIXEL_RENDERER;
            LINE_RENDERER = lINE_RENDERER;
            STAR_TH_ANGLE_NONE = sTAR_TH_ANGLE_NONE;
            STAR_TH_ANGLE_POINT = sTAR_TH_ANGLE_POINT;
            STAR_TH_ANGLE_QUAD = sTAR_TH_ANGLE_QUAD;
            POINT_ALPHA_MIN = pOINT_ALPHA_MIN;
            POINT_ALPHA_MAX = pOINT_ALPHA_MAX;
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
    public static ScreenshotConf screenshot;
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
    public static void initialize(VersionConf vc, ProgramConf pc, SceneConf sc, DataConf dc, RuntimeConf rc, PostprocessConf ppc, PerformanceConf pfc, FrameConf fc, ScreenConf scrc, ScreenshotConf shc) throws Exception {
        if (!initialized) {
            if (configurations == null) {
                configurations = new ArrayList<IConf>();
            }

            version = vc;
            program = pc;
            scene = sc;
            data = dc;
            runtime = rc;
            postprocess = ppc;
            performance = pfc;
            frame = fc;
            screenshot = shc;
            screen = scrc;

            configurations.add(program);
            configurations.add(scene);
            configurations.add(data);
            configurations.add(runtime);
            configurations.add(postprocess);
            configurations.add(performance);
            configurations.add(frame);
            configurations.add(screenshot);
            configurations.add(screen);

            initialized = true;
        }

    }

    public static String getFullApplicationName() {
        return APPLICATION_NAME + " - " + version.version;
    }

}
