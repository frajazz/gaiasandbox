package gaia.cu9.ari.gaiaorbit.util;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

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

    /** Properties object **/
    public static CommentedProperties p;

    public static final String TEXTURES_FOLDER = "data/tex/";

    public static interface IConf {
	/**
	 * Persists this configuration in the given properties object.
	 * @param p
	 */
	public void persist(Properties p);

	/**
	 * Initializes this configuration from the given properties object.
	 * @param p
	 */
	public void initialize(Properties p);

    }

    public static class ScreenshotConf implements IConf {
	public int SCREENSHOT_WIDTH;
	public int SCREENSHOT_HEIGHT;
	public String SCREENSHOT_FOLDER;

	@Override
	public void persist(Properties p) {
	    p.setProperty("screenshot.folder", SCREENSHOT_FOLDER);
	    p.setProperty("screenshot.width", Integer.toString(SCREENSHOT_WIDTH));
	    p.setProperty("screenshot.height", Integer.toString(SCREENSHOT_HEIGHT));
	}

	@Override
	public void initialize(Properties p) {
	    SCREENSHOT_FOLDER = p.getProperty("screenshot.folder").isEmpty() ? System.getProperty("java.io.tmpdir") : p.getProperty("screenshot.folder");
	    SCREENSHOT_WIDTH = Integer.parseInt(p.getProperty("screenshot.width"));
	    SCREENSHOT_HEIGHT = Integer.parseInt(p.getProperty("screenshot.height"));
	}

    }

    public static class PerformanceConf implements IConf {

	public boolean MULTITHREADING;
	public int NUMBER_THREADS;

	@Override
	public void persist(Properties p) {
	    p.setProperty("global.conf.multithreading", Boolean.toString(MULTITHREADING));
	    p.setProperty("global.conf.numthreads", Integer.toString(NUMBER_THREADS));
	}

	@Override
	public void initialize(Properties p) {
	    MULTITHREADING = Boolean.parseBoolean(p.getProperty("global.conf.multithreading"));
	    String propNumthreads = p.getProperty("global.conf.numthreads");
	    NUMBER_THREADS = Integer.parseInt((propNumthreads == null || propNumthreads.isEmpty()) ? "0" : propNumthreads);
	}

    }

    public static class PostprocessConf implements IConf, IObserver {

	public int POSTPROCESS_ANTIALIAS;
	public float POSTPROCESS_BLOOM_INTENSITY;
	/** This should be no smaller than 1 and no bigger than 5. The bigger the more stars with labels **/
	public boolean POSTPROCESS_LENS_FLARE;

	public PostprocessConf() {
	    EventManager.getInstance().subscribe(this, Events.BLOOM_CMD, Events.LENS_FLARE_CMD);
	}

	@Override
	public void persist(Properties p) {
	    p.setProperty("postprocess.antialiasing", Integer.toString(POSTPROCESS_ANTIALIAS));
	    p.setProperty("postprocess.bloom.intensity", Float.toString(POSTPROCESS_BLOOM_INTENSITY));
	    p.setProperty("postprocess.lensflare", Boolean.toString(POSTPROCESS_LENS_FLARE));

	}

	@Override
	public void initialize(Properties p) {
	    /** POSTPROCESS **/
	    /**
	     * aa
	     * value < 0 - FXAA
	     * value = 0 - no AA
	     * value > 0 - MSAA #samples = value
	     */
	    POSTPROCESS_ANTIALIAS = Integer.parseInt(p.getProperty("postprocess.antialiasing"));
	    POSTPROCESS_BLOOM_INTENSITY = Float.parseFloat(p.getProperty("postprocess.bloom.intensity"));
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
	public boolean GLOBAL_PAUSE;
	public boolean TIME_ON;
	public boolean INPUT_ENABLED;
	public float LIMIT_MAG_RUNTIME;

	public RuntimeConf() {
	    EventManager.getInstance().subscribe(this, Events.LIMIT_MAG_CMD, Events.INPUT_ENABLED_CMD, Events.TOGGLE_CLEANMODE, Events.TOGGLE_GLOBALPAUSE, Events.TOGGLE_TIME_CMD);
	}

	@Override
	public void persist(Properties p) {
	    // Runtime configuration is not persisted
	}

	@Override
	public void initialize(Properties p) {
	    // Input always enabled by default
	    INPUT_ENABLED = true;
	    LIMIT_MAG_RUNTIME = 20;
	    GLOBAL_PAUSE = false;
	    TIME_ON = false;

	}

	@Override
	public void notify(Events event, Object... data) {
	    switch (event) {
	    case LIMIT_MAG_CMD:
		LIMIT_MAG_RUNTIME = (float) data[0];
		break;

	    case INPUT_ENABLED_CMD:
		INPUT_ENABLED = (boolean) data[0];
		break;

	    case TOGGLE_CLEANMODE:
		CLEAN_MODE = !CLEAN_MODE;
		break;
	    case TOGGLE_GLOBALPAUSE:
		GLOBAL_PAUSE = !GLOBAL_PAUSE;
		break;
	    case TOGGLE_TIME_CMD:
		toggleTimeOn((Boolean) data[0]);
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
	public boolean RENDER_OUTPUT;

	public FrameConf() {
	    EventManager.getInstance().subscribe(this, Events.CONFIG_RENDER_SYSTEM, Events.RENDER_SYSTEM_CMD);
	}

	@Override
	public void persist(Properties p) {
	    p.setProperty("graphics.render.width", Integer.toString(RENDER_WIDTH));
	    p.setProperty("graphics.render.height", Integer.toString(RENDER_HEIGHT));
	    p.setProperty("graphics.render.targetfps", Integer.toString(RENDER_TARGET_FPS));
	    p.setProperty("graphics.render.folder", RENDER_FOLDER);
	    p.setProperty("graphics.render.filename", RENDER_FILE_NAME);
	    p.setProperty("graphics.render.time", Boolean.toString(RENDER_SCREENSHOT_TIME));
	    p.setProperty("graphics.render.output", Boolean.toString(RENDER_OUTPUT));
	}

	@Override
	public void initialize(Properties p) {
	    RENDER_WIDTH = Integer.parseInt(p.getProperty("graphics.render.width"));
	    RENDER_HEIGHT = Integer.parseInt(p.getProperty("graphics.render.height"));
	    RENDER_TARGET_FPS = Integer.parseInt(p.getProperty("graphics.render.targetfps"));
	    RENDER_FOLDER = p.getProperty("graphics.render.folder");
	    RENDER_FILE_NAME = p.getProperty("graphics.render.filename");
	    RENDER_SCREENSHOT_TIME = Boolean.parseBoolean(p.getProperty("graphics.render.time"));
	    RENDER_OUTPUT = Boolean.parseBoolean(p.getProperty("graphics.render.output"));
	}

	@Override
	public void notify(Events event, Object... data) {
	    switch (event) {
	    case CONFIG_RENDER_SYSTEM:
		RENDER_WIDTH = (int) data[0];
		RENDER_HEIGHT = (int) data[1];
		RENDER_TARGET_FPS = (int) data[2];
		RENDER_FOLDER = (String) data[3];
		RENDER_FILE_NAME = (String) data[4];
		break;
	    case RENDER_SYSTEM_CMD:
		RENDER_OUTPUT = (Boolean) data[0];
		break;
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
	public void persist(Properties p) {
	    p.setProperty("data.source.local", Boolean.toString(DATA_SOURCE_LOCAL));
	    p.setProperty("data.sg.file", DATA_SG_FILE);
	    p.setProperty("data.source.hostname", OBJECT_SERVER_HOSTNAME);
	    p.setProperty("data.source.port", Integer.toString(OBJECT_SERVER_PORT));
	    p.setProperty("data.source.visid", VISUALIZATION_ID);
	    p.setProperty("data.limit.mag", Float.toString(LIMIT_MAG_LOAD));
	}

	@Override
	public void initialize(Properties p) {
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
	public void persist(Properties p) {
	    p.setProperty("graphics.screen.width", Integer.toString(SCREEN_WIDTH));
	    p.setProperty("graphics.screen.height", Integer.toString(SCREEN_HEIGHT));
	    p.setProperty("graphics.screen.fullscreen.width", Integer.toString(FULLSCREEN_WIDTH));
	    p.setProperty("graphics.screen.fullscreen.height", Integer.toString(FULLSCREEN_HEIGHT));
	    p.setProperty("graphics.screen.fullscreen", Boolean.toString(FULLSCREEN));
	    p.setProperty("graphics.screen.resizable", Boolean.toString(RESIZABLE));
	    p.setProperty("graphics.screen.vsync", Boolean.toString(VSYNC));
	    p.setProperty("graphics.screen.screenoutput", Boolean.toString(SCREEN_OUTPUT));
	}

	@Override
	public void initialize(Properties p) {
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

	private DateFormat df;

	public ProgramConf() {
	    EventManager.getInstance().subscribe(this, Events.TOGGLE_STEREOSCOPIC);
	}

	@Override
	public void persist(Properties p) {
	    p.setProperty("program.tutorial", Boolean.toString(DISPLAY_TUTORIAL));
	    p.setProperty("program.tutorial.script", TUTORIAL_SCRIPT_LOCATION);
	    p.setProperty("program.configdialog", Boolean.toString(SHOW_CONFIG_DIALOG));
	    p.setProperty("program.debuginfo", Boolean.toString(SHOW_DEBUG_INFO));
	    p.setProperty("program.lastchecked", df.format(LAST_CHECKED));
	    p.setProperty("program.lastversion", LAST_VERSION);
	    p.setProperty("program.versioncheckurl", VERSION_CHECK_URL);
	    p.setProperty("program.ui.theme", UI_THEME);
	    p.setProperty("program.scriptlocation", SCRIPT_LOCATION);
	    p.setProperty("program.locale", LOCALE);
	    p.setProperty("program.stereoscopic", Boolean.toString(STEREOSCOPIC_MODE));
	}

	@Override
	public void initialize(Properties p) {
	    LOCALE = p.getProperty("program.locale");
	    df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.forLanguageTag(LOCALE));

	    DISPLAY_TUTORIAL = Boolean.parseBoolean(p.getProperty("program.tutorial"));
	    TUTORIAL_SCRIPT_LOCATION = p.getProperty("program.tutorial.script");
	    SHOW_CONFIG_DIALOG = Boolean.parseBoolean(p.getProperty("program.configdialog"));
	    SHOW_DEBUG_INFO = Boolean.parseBoolean(p.getProperty("program.debuginfo"));
	    try {
		LAST_CHECKED = p.getProperty("program.lastchecked").isEmpty() ? null : df.parse(p.getProperty("program.lastchecked"));
	    } catch (ParseException e) {
		EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	    }
	    LAST_VERSION = p.getProperty("program.lastversion");
	    VERSION_CHECK_URL = p.getProperty("program.versioncheckurl");
	    UI_THEME = p.getProperty("program.ui.theme");
	    SCRIPT_LOCATION = p.getProperty("program.scriptlocation").isEmpty() ? System.getProperty("user.dir") : p.getProperty("program.scriptlocation");

	    STEREOSCOPIC_MODE = Boolean.parseBoolean(p.getProperty("program.stereoscopic"));
	}

	public String getLastCheckedString() {
	    return df.format(LAST_CHECKED);
	}

	@Override
	public void notify(Events event, Object... data) {
	    switch (event) {
	    case TOGGLE_STEREOSCOPIC:
		STEREOSCOPIC_MODE = !STEREOSCOPIC_MODE;
		break;
	    }
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

	public SceneConf() {
	    EventManager.getInstance().subscribe(this, Events.FOCUS_LOCK_CMD, Events.STAR_BRIGHTNESS_CMD, Events.FOV_CHANGED_CMD, Events.CAMERA_SPEED_CMD, Events.ROTATION_SPEED_CMD, Events.TURNING_SPEED_CMD, Events.SPEED_LIMIT_CMD, Events.TRANSIT_COLOUR_CMD, Events.ONLY_OBSERVED_STARS_CMD, Events.COMPUTE_GAIA_SCAN_CMD);
	}

	@Override
	public void persist(Properties p) {
	    p.setProperty("scene.object.fadems", Long.toString(OBJECT_FADE_MS));
	    p.setProperty("scene.star.brightness", Float.toString(STAR_BRIGHTNESS));
	    p.setProperty("scene.ambient", Float.toString(AMBIENT_LIGHT));
	    p.setProperty("scene.camera.fov", Integer.toString(CAMERA_FOV));
	    p.setProperty("scene.camera.speedlimit", Integer.toString(CAMERA_SPEED_LIMIT_IDX));
	    p.setProperty("scene.camera.focus.vel", Float.toString(CAMERA_SPEED));
	    p.setProperty("scene.camera.turn.vel", Float.toString(TURNING_SPEED));
	    p.setProperty("scene.camera.rotate.vel", Float.toString(ROTATION_SPEED));
	    p.setProperty("scene.focuslock", Boolean.toString(FOCUS_LOCK));
	    p.setProperty("scene.labelfactor", Float.toString(LABEL_NUMBER_FACTOR));
	    // Visibility of components
	    int idx = 0;
	    ComponentType[] cts = ComponentType.values();
	    for (boolean b : VISIBILITY) {
		ComponentType ct = cts[idx];
		p.setProperty("scene.visibility." + ct.name(), Boolean.toString(b));
		idx++;
	    }
	}

	@Override
	public void initialize(Properties p) {
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
	    case 6:
		// 1 pc/s and 2 pc/s
		CAMERA_SPEED_LIMIT = (CAMERA_SPEED_LIMIT_IDX - 4) * Constants.PC_TO_U;
		break;
	    case 7:
		// 10 pc/s
		CAMERA_SPEED_LIMIT = 10 * Constants.PC_TO_U;
		break;
	    case 8:
		// 1000 pc/s
		CAMERA_SPEED_LIMIT = 1000 * Constants.PC_TO_U;
		break;
	    case 9:
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

	@Override
	public void persist(Properties p) {
	    // The version info can not be modified
	}

	@Override
	public void initialize(Properties p) {
	    version = p.getProperty("version");
	    buildtime = p.getProperty("buildtime");
	    builder = p.getProperty("builder");
	    build = p.getProperty("build");
	    system = p.getProperty("system");

	    int[] majmin = getMajorMinorFromString(version);
	    major = majmin[0];
	    minor = majmin[1];

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
    public static void initialize(InputStream propsFile, InputStream versionFile) {
	try {
	    if (configurations == null) {
		configurations = new ArrayList<IConf>();
	    }

	    if (version == null) {
		version = new VersionConf();

		Properties versionProps = new Properties();
		versionProps.load(versionFile);
		version.initialize(versionProps);
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

	    if (screenshot == null) {
		screenshot = new ScreenshotConf();
		configurations.add(screenshot);
	    }

	    if (postprocess == null) {
		postprocess = new PostprocessConf();
		configurations.add(postprocess);
	    }

	    if (performance == null) {
		performance = new PerformanceConf();
		configurations.add(performance);
	    }

	    initialize(propsFile);

	    initialized = true;
	} catch (Exception e) {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	}

    }

    /**
     * Runs the initialize method in all the configurations using the given properties file stream.
     * @param propsFile An input stream sourced in the configuration file.
     */
    public static void initialize(InputStream propsFile) {
	p = new CommentedProperties();
	try {
	    p.load(propsFile);

	    for (IConf conf : configurations) {
		conf.initialize(p);
	    }

	} catch (Exception e) {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	}

    }

    /**
     * Saves the current state of the properties to the properties file.
     */
    public static void saveProperties(URL propsFileURL) {
	updatePropertiesValues();
	try {
	    FileOutputStream fos = new FileOutputStream(propsFileURL.getFile());
	    p.store(fos, null);
	    fos.close();
	    EventManager.getInstance().post(Events.POST_NOTIFICATION, "Configuration saved to " + propsFileURL);
	} catch (Exception e) {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	}
    }

    /**
     * Updates the Properties object with the values of the actual property variables.
     */
    private static void updatePropertiesValues() {
	if (p != null && !p.isEmpty()) {

	    for (IConf conf : configurations) {
		conf.persist(p);
	    }

	}
    }

    public static String getFullApplicationName() {
	return APPLICATION_NAME + " - " + version.version;
    }

}
