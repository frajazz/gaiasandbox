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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Holds the global configuration options
 * @author Toni Sagrista
 *
 */
public class GlobalConf implements IObserver {
    public static final String APPLICATION_NAME = "Gaia Sandbox";
    public static final String WEBPAGE = "http://www.zah.uni-heidelberg.de/gaia2/outreach/gaiasandbox/";
    public static final String WIKI = "https://github.com/ari-zah/gaiasandbox/wiki";

    public static boolean OPENGL_GUI;
    private static DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /** Properties object **/
    public CommentedProperties p;

    public static String TEX_FOLDER = "data/tex/";

    /**
     * Property values
     */
    public int SCREEN_WIDTH, SCREEN_HEIGHT, FULLSCREEN_WIDTH, FULLSCREEN_HEIGHT, CAMERA_FOV;
    public int RENDER_WIDTH, RENDER_HEIGHT, RENDER_TARGET_FPS, POSTPROCESS_ANTIALIAS, NUMBER_THREADS;
    public long OBJECT_FADE_MS;
    public float LIMIT_MAG_LOAD, LIMIT_MAG_RUNTIME, POSTPROCESS_BLOOM_INTENSITY, STAR_BRIGHTNESS, AMBIENT_LIGHT;
    public float CAMERA_SPEED, TURNING_SPEED, ROTATION_SPEED;
    public boolean SCREEN_OUTPUT, RENDER_OUTPUT, DISPLAY_TUTORIAL, MULTITHREADING, STAR_COLOR_TRANSIT, ONLY_OBSERVED_STARS, COMPUTE_GAIA_SCAN, SHOW_DEBUG_INFO, VSYNC, POSTPROCESS_LENS_FLARE;
    public boolean FULLSCREEN, RESIZABLE, SHOW_CONFIG_DIALOG, FOCUS_LOCK, RENDER_SCREENSHOT_TIME, INPUT_ENABLED;
    public String RENDER_FOLDER, RENDER_FILE_NAME, SG_FILE, TUTORIAL_SCRIPT_LOCATION;
    public String SCREENSHOT_FOLDER, VERSION_CHECK_URL, LAST_VERSION, UI_THEME, SCRIPT_LOCATION;
    public String LOCALE;
    public int SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT;
    public Date LAST_CHECKED;
    /** Visibility of components **/
    public boolean[] VISIBILITY;

    public VersionInfo VERSION;

    public static GlobalConf instance;
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
    public static void initialize(InputStream propsFile) {
	if (instance == null)
	    instance = new GlobalConf();
	instance.init(propsFile);
	initialized = true;
    }

    /**
     * Initializes the properties
     */
    public static void initialize(InputStream propsFile, InputStream versionFile) {
	if (instance == null)
	    instance = new GlobalConf();
	if (instance.VERSION == null) {
	    instance.initializeVersion(versionFile);
	}

	instance.init(propsFile);
	initialized = true;

    }

    public void init(InputStream propsFile) {
	p = new CommentedProperties();
	try {
	    p.load(propsFile);

	    // Input always enabled by default
	    INPUT_ENABLED = true;

	    /** GRAPHICS.RENDER **/
	    RENDER_WIDTH = Integer.parseInt(p.getProperty("graphics.render.width"));
	    RENDER_HEIGHT = Integer.parseInt(p.getProperty("graphics.render.height"));
	    RENDER_TARGET_FPS = Integer.parseInt(p.getProperty("graphics.render.targetfps"));
	    RENDER_FOLDER = p.getProperty("graphics.render.folder");
	    RENDER_FILE_NAME = p.getProperty("graphics.render.filename");
	    RENDER_SCREENSHOT_TIME = Boolean.parseBoolean(p.getProperty("graphics.render.time"));
	    RENDER_OUTPUT = Boolean.parseBoolean(p.getProperty("graphics.render.output"));

	    /** GRAPHICS.SCREEN **/
	    SCREEN_WIDTH = Integer.parseInt(p.getProperty("graphics.screen.width"));
	    SCREEN_HEIGHT = Integer.parseInt(p.getProperty("graphics.screen.height"));
	    FULLSCREEN_WIDTH = Integer.parseInt(p.getProperty("graphics.screen.fullscreen.width"));
	    FULLSCREEN_HEIGHT = Integer.parseInt(p.getProperty("graphics.screen.fullscreen.height"));
	    FULLSCREEN = Boolean.parseBoolean(p.getProperty("graphics.screen.fullscreen"));
	    RESIZABLE = Boolean.parseBoolean(p.getProperty("graphics.screen.resizable"));
	    VSYNC = Boolean.parseBoolean(p.getProperty("graphics.screen.vsync"));
	    SCREEN_OUTPUT = Boolean.parseBoolean(p.getProperty("graphics.screen.screenoutput"));

	    /** PROGRAM **/
	    DISPLAY_TUTORIAL = Boolean.parseBoolean(p.getProperty("program.tutorial"));
	    TUTORIAL_SCRIPT_LOCATION = p.getProperty("program.tutorial.script");
	    SHOW_CONFIG_DIALOG = Boolean.parseBoolean(p.getProperty("program.configdialog"));
	    SHOW_DEBUG_INFO = Boolean.parseBoolean(p.getProperty("program.debuginfo"));
	    LAST_CHECKED = p.getProperty("program.lastchecked").isEmpty() ? null : df.parse(p.getProperty("program.lastchecked"));
	    LAST_VERSION = p.getProperty("program.lastversion");
	    VERSION_CHECK_URL = p.getProperty("program.versioncheckurl");
	    UI_THEME = p.getProperty("program.ui.theme");
	    SCRIPT_LOCATION = p.getProperty("program.scriptlocation").isEmpty() ? System.getProperty("user.dir") : p.getProperty("program.scriptlocation");
	    LOCALE = p.getProperty("program.locale");

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

	    /** GLOBAL **/
	    MULTITHREADING = Boolean.parseBoolean(p.getProperty("global.conf.multithreading"));
	    String propNumthreads = p.getProperty("global.conf.numthreads");
	    NUMBER_THREADS = Integer.parseInt((propNumthreads == null || propNumthreads.isEmpty()) ? "0" : propNumthreads);

	    /** DATA **/
	    SG_FILE = p.getProperty("data.sg.file");
	    if (p.getProperty("data.limit.mag") != null && !p.getProperty("data.limit.mag").isEmpty()) {
		LIMIT_MAG_LOAD = Float.parseFloat(p.getProperty("data.limit.mag"));
	    } else {
		LIMIT_MAG_LOAD = Float.MAX_VALUE;
	    }
	    LIMIT_MAG_RUNTIME = LIMIT_MAG_LOAD;

	    /** SCREENSHOT **/
	    SCREENSHOT_FOLDER = p.getProperty("screenshot.folder").isEmpty() ? System.getProperty("java.io.tmpdir") : p.getProperty("screenshot.folder");
	    SCREENSHOT_WIDTH = Integer.parseInt(p.getProperty("screenshot.width"));
	    SCREENSHOT_HEIGHT = Integer.parseInt(p.getProperty("screenshot.height"));

	    /** SCENE **/
	    OBJECT_FADE_MS = Long.parseLong(p.getProperty("scene.object.fadems"));
	    STAR_BRIGHTNESS = Float.parseFloat(p.getProperty("scene.star.brightness"));
	    AMBIENT_LIGHT = Float.parseFloat(p.getProperty("scene.ambient"));
	    CAMERA_FOV = Integer.parseInt(p.getProperty("scene.camera.fov"));
	    CAMERA_SPEED = Float.parseFloat(p.getProperty("scene.camera.focus.vel"));
	    FOCUS_LOCK = Boolean.parseBoolean(p.getProperty("scene.focuslock"));
	    TURNING_SPEED = Float.parseFloat(p.getProperty("scene.camera.turn.vel"));
	    ROTATION_SPEED = Float.parseFloat(p.getProperty("scene.camera.rotate.vel"));
	    //Visibility of components
	    ComponentType[] cts = ComponentType.values();
	    VISIBILITY = new boolean[cts.length];
	    for (ComponentType ct : cts) {
		String key = "scene.visibility." + ct.name();
		if (p.containsKey(key)) {
		    VISIBILITY[ct.ordinal()] = Boolean.parseBoolean(p.getProperty(key));
		}
	    }

	} catch (Exception e) {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	}

	EventManager.getInstance().subscribe(this, Events.TRANSIT_COLOUR_CMD, Events.ONLY_OBSERVED_STARS_CMD, Events.COMPUTE_GAIA_SCAN_CMD, Events.STAR_BRIGHTNESS_CMD, Events.BLOOM_CMD, Events.FOV_CHANGED_CMD, Events.FOCUS_LOCK_CMD, Events.CAMERA_SPEED_CMD, Events.ROTATION_SPEED_CMD, Events.TURNING_SPEED_CMD, Events.INPUT_ENABLED_CMD, Events.CONFIG_RENDER_SYSTEM, Events.RENDER_SYSTEM_CMD, Events.LENS_FLARE_CMD, Events.LIMIT_MAG_CMD);
    }

    /**
     * Saves the current state of the properties to the properties file.
     */
    public void saveProperties(URL propsFileURL) {
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
    private void updatePropertiesValues() {
	if (p != null && !p.isEmpty()) {
	    /** GRAPHICS.RENDER **/
	    p.setProperty("graphics.render.width", Integer.toString(RENDER_WIDTH));
	    p.setProperty("graphics.render.height", Integer.toString(RENDER_HEIGHT));
	    p.setProperty("graphics.render.targetfps", Integer.toString(RENDER_TARGET_FPS));
	    p.setProperty("graphics.render.folder", RENDER_FOLDER);
	    p.setProperty("graphics.render.filename", RENDER_FILE_NAME);
	    p.setProperty("graphics.render.time", Boolean.toString(RENDER_SCREENSHOT_TIME));
	    p.setProperty("graphics.render.output", Boolean.toString(RENDER_OUTPUT));

	    /** GRAPHICS.SCREEN **/
	    p.setProperty("graphics.screen.width", Integer.toString(SCREEN_WIDTH));
	    p.setProperty("graphics.screen.height", Integer.toString(SCREEN_HEIGHT));
	    p.setProperty("graphics.screen.fullscreen.width", Integer.toString(FULLSCREEN_WIDTH));
	    p.setProperty("graphics.screen.fullscreen.height", Integer.toString(FULLSCREEN_HEIGHT));
	    p.setProperty("graphics.screen.fullscreen", Boolean.toString(FULLSCREEN));
	    p.setProperty("graphics.screen.resizable", Boolean.toString(RESIZABLE));
	    p.setProperty("graphics.screen.vsync", Boolean.toString(VSYNC));
	    p.setProperty("graphics.screen.screenoutput", Boolean.toString(SCREEN_OUTPUT));

	    /** PROGRAM **/
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

	    /** POSTPROCESS **/
	    p.setProperty("postprocess.antialiasing", Integer.toString(POSTPROCESS_ANTIALIAS));
	    p.setProperty("postprocess.bloom.intensity", Float.toString(POSTPROCESS_BLOOM_INTENSITY));
	    p.setProperty("postprocess.lensflare", Boolean.toString(POSTPROCESS_LENS_FLARE));

	    /** GLOBAL **/
	    p.setProperty("global.conf.multithreading", Boolean.toString(MULTITHREADING));
	    p.setProperty("global.conf.numthreads", Integer.toString(NUMBER_THREADS));

	    /** DATA **/
	    p.setProperty("data.sg.file", SG_FILE);
	    p.setProperty("data.limit.mag", Float.toString(LIMIT_MAG_LOAD));

	    /** SCREENSHOT **/
	    p.setProperty("screenshot.folder", SCREENSHOT_FOLDER);
	    p.setProperty("screenshot.width", Integer.toString(SCREENSHOT_WIDTH));
	    p.setProperty("screenshot.height", Integer.toString(SCREENSHOT_HEIGHT));

	    /** SCENE **/
	    p.setProperty("scene.object.fadems", Long.toString(OBJECT_FADE_MS));
	    p.setProperty("scene.star.brightness", Float.toString(STAR_BRIGHTNESS));
	    p.setProperty("scene.ambient", Float.toString(AMBIENT_LIGHT));
	    p.setProperty("scene.camera.fov", Integer.toString(CAMERA_FOV));
	    p.setProperty("scene.camera.focus.vel", Float.toString(CAMERA_SPEED));
	    p.setProperty("scene.camera.turn.vel", Float.toString(TURNING_SPEED));
	    p.setProperty("scene.camera.rotate.vel", Float.toString(ROTATION_SPEED));
	    p.setProperty("scene.focuslock", Boolean.toString(FOCUS_LOCK));
	    // Visibility of components
	    int idx = 0;
	    ComponentType[] cts = ComponentType.values();
	    for (boolean b : VISIBILITY) {
		ComponentType ct = cts[idx];
		p.setProperty("scene.visibility." + ct.name(), Boolean.toString(b));
		idx++;
	    }

	}
    }

    public void initializeVersion(InputStream resource) {
	Properties p = new Properties();
	try {
	    p.load(resource);
	    VERSION = new VersionInfo();
	    VERSION.version = p.getProperty("version");
	    VERSION.buildtime = p.getProperty("buildtime");
	    VERSION.builder = p.getProperty("builder");
	    VERSION.build = p.getProperty("build");
	    VERSION.system = p.getProperty("system");
	    VERSION.initialize();
	} catch (Exception e) {
	    EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	}
    }

    public String getFullApplicationName() {
	return APPLICATION_NAME + " - " + VERSION.version;
    }

    public static class VersionInfo {
	public String version, buildtime, builder, system, build;
	public int major, minor;

	public void initialize() {
	    int[] majmin = getMajorMinorFromString(version);
	    this.major = majmin[0];
	    this.minor = majmin[1];
	}

	public static int[] getMajorMinorFromString(String version) {
	    String majorS = version.substring(0, version.indexOf("."));
	    String minorS = version.substring(version.indexOf(".") + 1, version.length());
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
	case BLOOM_CMD:
	    POSTPROCESS_BLOOM_INTENSITY = (float) data[0];
	    break;
	case STAR_BRIGHTNESS_CMD:
	    STAR_BRIGHTNESS = (float) data[0];
	    break;
	case FOV_CHANGED_CMD:
	    CAMERA_FOV = MathUtilsd.clamp(((Float) data[0]).intValue(), Constants.MIN_FOV, Constants.MAX_FOV);
	    break;
	case LIMIT_MAG_CMD:
	    LIMIT_MAG_RUNTIME = (float) data[0];
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
	case INPUT_ENABLED_CMD:
	    INPUT_ENABLED = (boolean) data[0];
	    break;
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
	case LENS_FLARE_CMD:
	    POSTPROCESS_LENS_FLARE = (Boolean) data[0];
	    break;
	default:
	    break;
	}

    }

    public int getScreenWidth() {
	return FULLSCREEN ? FULLSCREEN_WIDTH : SCREEN_WIDTH;
    }

    public int getScreenHeight() {
	return FULLSCREEN ? FULLSCREEN_HEIGHT : SCREEN_HEIGHT;
    }

    public String getLastCheckedString() {
	return df.format(LAST_CHECKED);
    }

}
