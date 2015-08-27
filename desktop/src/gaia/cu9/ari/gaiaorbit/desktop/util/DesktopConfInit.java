package gaia.cu9.ari.gaiaorbit.desktop.util;

import gaia.cu9.ari.gaiaorbit.desktop.GaiaSandboxDesktop;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.DataConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.FrameConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.PerformanceConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.PostprocessConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.RuntimeConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.SceneConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ScreenConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ScreenshotConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ScreenshotMode;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.VersionConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.SysUtils;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.IDateFormat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.Date;
import java.util.Properties;

/**
 * Desktop GlobalConf initialiser, where the configuration comes from a
 * global.properties file.
 * @author tsagrista
 *
 */
public class DesktopConfInit extends ConfInit {
    CommentedProperties p;
    Properties vp;

    IDateFormat df = DateFormatFactory.getFormatter("dd/MM/yyyy HH:mm:ss");

    public DesktopConfInit() {
        super();
        try {
            String propsFileProperty = System.getProperty("properties.file");
            if (propsFileProperty == null || propsFileProperty.isEmpty()) {
                propsFileProperty = initConfigFile(false);
            }

            File confFile = new File(propsFileProperty);
            InputStream fis = new FileInputStream(confFile);
            // This should work for the normal execution
            InputStream vis = GaiaSandboxDesktop.class.getResourceAsStream("/version");
            if (vis == null) {
                // In case of running in 'developer' mode
                vis = new FileInputStream(new File(System.getProperty("assets.location") + "data/dummyversion"));
            }
            vp = new Properties();
            vp.load(vis);

            p = new CommentedProperties();
            p.load(fis);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public DesktopConfInit(InputStream fis, InputStream vis) {
        super();
        try {
            vp = new Properties();
            vp.load(vis);

            p = new CommentedProperties();
            p.load(fis);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    @Override
    public void initGlobalConf() throws Exception {

        /** VERSION CONF **/
        VersionConf vc = new VersionConf();
        String versionStr = vp.getProperty("version");
        int[] majmin = getMajorMinorFromString(versionStr);
        vc.initialize(versionStr, vp.getProperty("buildtime"), vp.getProperty("builder"), vp.getProperty("build"), vp.getProperty("system"), majmin[0], majmin[1]);

        /** PERFORMANCE CONF **/
        PerformanceConf pc = new PerformanceConf();
        boolean MULTITHREADING = Boolean.parseBoolean(p.getProperty("global.conf.multithreading"));
        String propNumthreads = p.getProperty("global.conf.numthreads");
        int NUMBER_THREADS = Integer.parseInt((propNumthreads == null || propNumthreads.isEmpty()) ? "0" : propNumthreads);
        pc.initialize(MULTITHREADING, NUMBER_THREADS);

        /** POSTPROCESS CONF **/
        PostprocessConf ppc = new PostprocessConf();
        int POSTPROCESS_ANTIALIAS = Integer.parseInt(p.getProperty("postprocess.antialiasing"));
        float POSTPROCESS_BLOOM_INTENSITY = Float.parseFloat(p.getProperty("postprocess.bloom.intensity"));
        float POSTPROCESS_MOTION_BLUR = Float.parseFloat(p.getProperty("postprocess.motionblur"));
        boolean POSTPROCESS_LENS_FLARE = Boolean.parseBoolean(p.getProperty("postprocess.lensflare"));
        ppc.initialize(POSTPROCESS_ANTIALIAS, POSTPROCESS_BLOOM_INTENSITY, POSTPROCESS_MOTION_BLUR, POSTPROCESS_LENS_FLARE);

        /** RUNTIME CONF **/
        RuntimeConf rc = new RuntimeConf();
        rc.initialize(false, false, false, false, true, false, 20, false);

        /** DATA CONF **/
        DataConf dc = new DataConf();
        boolean DATA_SOURCE_LOCAL = Boolean.parseBoolean(p.getProperty("data.source.local"));
        String DATA_JSON_FILE = p.getProperty("data.json.file");
        String OBJECT_SERVER_HOSTNAME = p.getProperty("data.source.hostname");
        int OBJECT_SERVER_PORT = Integer.parseInt(p.getProperty("data.source.port"));
        String VISUALIZATION_ID = p.getProperty("data.source.visid");

        float LIMIT_MAG_LOAD;
        if (p.getProperty("data.limit.mag") != null && !p.getProperty("data.limit.mag").isEmpty()) {
            LIMIT_MAG_LOAD = Float.parseFloat(p.getProperty("data.limit.mag"));
        } else {
            LIMIT_MAG_LOAD = Float.MAX_VALUE;
        }
        dc.initialize(DATA_SOURCE_LOCAL, DATA_JSON_FILE, OBJECT_SERVER_HOSTNAME, OBJECT_SERVER_PORT, VISUALIZATION_ID, LIMIT_MAG_LOAD);

        /** PROGRAM CONF **/
        ProgramConf prc = new ProgramConf();
        String LOCALE = p.getProperty("program.locale");

        boolean DISPLAY_TUTORIAL = Boolean.parseBoolean(p.getProperty("program.tutorial"));
        String TUTORIAL_SCRIPT_LOCATION = p.getProperty("program.tutorial.script");
        boolean SHOW_CONFIG_DIALOG = Boolean.parseBoolean(p.getProperty("program.configdialog"));
        boolean SHOW_DEBUG_INFO = Boolean.parseBoolean(p.getProperty("program.debuginfo"));
        Date LAST_CHECKED = p.getProperty("program.lastchecked").isEmpty() ? null : df.parse(p.getProperty("program.lastchecked"));
        String LAST_VERSION = p.getProperty("program.lastversion");
        String VERSION_CHECK_URL = p.getProperty("program.versioncheckurl");
        String UI_THEME = p.getProperty("program.ui.theme");
        String SCRIPT_LOCATION = p.getProperty("program.scriptlocation").isEmpty() ? System.getProperty("user.dir") : p.getProperty("program.scriptlocation");

        boolean STEREOSCOPIC_MODE = Boolean.parseBoolean(p.getProperty("program.stereoscopic"));
        StereoProfile STEREO_PROFILE = StereoProfile.values()[Integer.parseInt(p.getProperty("program.stereoscopic.profile"))];
        prc.initialize(DISPLAY_TUTORIAL, TUTORIAL_SCRIPT_LOCATION, SHOW_CONFIG_DIALOG, SHOW_DEBUG_INFO, LAST_CHECKED, LAST_VERSION, VERSION_CHECK_URL, UI_THEME, SCRIPT_LOCATION, LOCALE, STEREOSCOPIC_MODE, STEREO_PROFILE);

        /** SCENE CONF **/
        long OBJECT_FADE_MS = Long.parseLong(p.getProperty("scene.object.fadems"));
        float STAR_BRIGHTNESS = Float.parseFloat(p.getProperty("scene.star.brightness"));
        float AMBIENT_LIGHT = Float.parseFloat(p.getProperty("scene.ambient"));
        int CAMERA_FOV = Integer.parseInt(p.getProperty("scene.camera.fov"));
        int CAMERA_SPEED_LIMIT_IDX = Integer.parseInt(p.getProperty("scene.camera.speedlimit"));
        float CAMERA_SPEED = Float.parseFloat(p.getProperty("scene.camera.focus.vel"));
        boolean FOCUS_LOCK = Boolean.parseBoolean(p.getProperty("scene.focuslock"));
        float TURNING_SPEED = Float.parseFloat(p.getProperty("scene.camera.turn.vel"));
        float ROTATION_SPEED = Float.parseFloat(p.getProperty("scene.camera.rotate.vel"));
        float LABEL_NUMBER_FACTOR = Float.parseFloat(p.getProperty("scene.labelfactor"));
        double STAR_TH_ANGLE_QUAD = Math.toRadians(Double.parseDouble(p.getProperty("scene.star.thresholdangle.quad")));
        double STAR_TH_ANGLE_POINT = Math.toRadians(Double.parseDouble(p.getProperty("scene.star.thresholdangle.point")));
        double STAR_TH_ANGLE_NONE = Math.toRadians(Double.parseDouble(p.getProperty("scene.star.thresholdangle.none")));
        float POINT_ALPHA_MIN = Float.parseFloat(p.getProperty("scene.point.alpha.min"));
        float POINT_ALPHA_MAX = Float.parseFloat(p.getProperty("scene.point.alpha.max"));
        int PIXEL_RENDERER = Integer.parseInt(p.getProperty("scene.renderer.star"));
        int LINE_RENDERER = Integer.parseInt(p.getProperty("scene.renderer.line"));
        //Visibility of components
        ComponentType[] cts = ComponentType.values();
        boolean[] VISIBILITY = new boolean[cts.length];
        for (ComponentType ct : cts) {
            String key = "scene.visibility." + ct.name();
            if (p.containsKey(key)) {
                VISIBILITY[ct.ordinal()] = Boolean.parseBoolean(p.getProperty(key));
            }
        }
        SceneConf sc = new SceneConf();
        sc.initialize(OBJECT_FADE_MS, STAR_BRIGHTNESS, AMBIENT_LIGHT, CAMERA_FOV, CAMERA_SPEED, TURNING_SPEED, ROTATION_SPEED, CAMERA_SPEED_LIMIT_IDX, FOCUS_LOCK, LABEL_NUMBER_FACTOR, VISIBILITY, PIXEL_RENDERER, LINE_RENDERER, STAR_TH_ANGLE_NONE, STAR_TH_ANGLE_POINT, STAR_TH_ANGLE_QUAD, POINT_ALPHA_MIN, POINT_ALPHA_MAX);

        /** FRAME CONF **/
        String renderFolder = null;
        if (p.getProperty("graphics.render.folder") == null || p.getProperty("graphics.render.folder").isEmpty()) {
            File framesDir = SysUtils.getDefaultFramesDir();
            framesDir.mkdirs();
            renderFolder = framesDir.getAbsolutePath();
        } else {
            renderFolder = p.getProperty("graphics.render.folder");
        }
        String RENDER_FOLDER = renderFolder;
        String RENDER_FILE_NAME = p.getProperty("graphics.render.filename");
        int RENDER_WIDTH = Integer.parseInt(p.getProperty("graphics.render.width"));
        int RENDER_HEIGHT = Integer.parseInt(p.getProperty("graphics.render.height"));
        int RENDER_TARGET_FPS = Integer.parseInt(p.getProperty("graphics.render.targetfps"));
        boolean RENDER_SCREENSHOT_TIME = Boolean.parseBoolean(p.getProperty("graphics.render.time"));
        ScreenshotMode FRAME_MODE = ScreenshotMode.valueOf(p.getProperty("graphics.render.mode"));
        FrameConf fc = new FrameConf();
        fc.initialize(RENDER_WIDTH, RENDER_HEIGHT, RENDER_TARGET_FPS, RENDER_FOLDER, RENDER_FILE_NAME, RENDER_SCREENSHOT_TIME, RENDER_SCREENSHOT_TIME, FRAME_MODE);

        /** SCREEN CONF **/
        int SCREEN_WIDTH = Integer.parseInt(p.getProperty("graphics.screen.width"));
        int SCREEN_HEIGHT = Integer.parseInt(p.getProperty("graphics.screen.height"));
        int FULLSCREEN_WIDTH = Integer.parseInt(p.getProperty("graphics.screen.fullscreen.width"));
        int FULLSCREEN_HEIGHT = Integer.parseInt(p.getProperty("graphics.screen.fullscreen.height"));
        boolean FULLSCREEN = Boolean.parseBoolean(p.getProperty("graphics.screen.fullscreen"));
        boolean RESIZABLE = Boolean.parseBoolean(p.getProperty("graphics.screen.resizable"));
        boolean VSYNC = Boolean.parseBoolean(p.getProperty("graphics.screen.vsync"));
        boolean SCREEN_OUTPUT = Boolean.parseBoolean(p.getProperty("graphics.screen.screenoutput"));
        ScreenConf scrc = new ScreenConf();
        scrc.initialize(SCREEN_WIDTH, SCREEN_HEIGHT, FULLSCREEN_WIDTH, FULLSCREEN_HEIGHT, FULLSCREEN, RESIZABLE, VSYNC, SCREEN_OUTPUT);

        /** SCREENSHOT CONF **/
        String screenshotFolder = null;
        if (p.getProperty("screenshot.folder") == null || p.getProperty("screenshot.folder").isEmpty()) {
            File screenshotDir = SysUtils.getDefaultScreenshotsDir();
            screenshotDir.mkdirs();
            screenshotFolder = screenshotDir.getAbsolutePath();
        } else {
            screenshotFolder = p.getProperty("screenshot.folder");
        }
        String SCREENSHOT_FOLDER = screenshotFolder;
        int SCREENSHOT_WIDTH = Integer.parseInt(p.getProperty("screenshot.width"));
        int SCREENSHOT_HEIGHT = Integer.parseInt(p.getProperty("screenshot.height"));
        ScreenshotMode SCREENSHOT_MODE = ScreenshotMode.valueOf(p.getProperty("screenshot.mode"));
        ScreenshotConf shc = new ScreenshotConf();
        shc.initialize(SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT, SCREENSHOT_FOLDER, SCREENSHOT_MODE);

        /** INIT GLOBAL CONF **/
        GlobalConf.initialize(vc, prc, sc, dc, rc, ppc, pc, fc, scrc, shc);

    }

    @Override
    public void persistGlobalConf(File propsFile) {

        /** SCREENSHOT **/
        p.setProperty("screenshot.folder", GlobalConf.screenshot.SCREENSHOT_FOLDER);
        p.setProperty("screenshot.width", Integer.toString(GlobalConf.screenshot.SCREENSHOT_WIDTH));
        p.setProperty("screenshot.height", Integer.toString(GlobalConf.screenshot.SCREENSHOT_HEIGHT));
        p.setProperty("screenshot.mode", GlobalConf.screenshot.SCREENSHOT_MODE.toString());

        /** PERFORMANCE **/
        p.setProperty("global.conf.multithreading", Boolean.toString(GlobalConf.performance.MULTITHREADING));
        p.setProperty("global.conf.numthreads", Integer.toString(GlobalConf.performance.NUMBER_THREADS));

        /** POSTPROCESS **/
        p.setProperty("postprocess.antialiasing", Integer.toString(GlobalConf.postprocess.POSTPROCESS_ANTIALIAS));
        p.setProperty("postprocess.bloom.intensity", Float.toString(GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY));
        p.setProperty("postprocess.motionblur", Float.toString(GlobalConf.postprocess.POSTPROCESS_MOTION_BLUR));
        p.setProperty("postprocess.lensflare", Boolean.toString(GlobalConf.postprocess.POSTPROCESS_LENS_FLARE));

        /** FRAME CONF **/
        p.setProperty("graphics.render.folder", GlobalConf.frame.RENDER_FOLDER);
        p.setProperty("graphics.render.filename", GlobalConf.frame.RENDER_FILE_NAME);
        p.setProperty("graphics.render.width", Integer.toString(GlobalConf.frame.RENDER_WIDTH));
        p.setProperty("graphics.render.height", Integer.toString(GlobalConf.frame.RENDER_HEIGHT));
        p.setProperty("graphics.render.targetfps", Integer.toString(GlobalConf.frame.RENDER_TARGET_FPS));
        p.setProperty("graphics.render.time", Boolean.toString(GlobalConf.frame.RENDER_SCREENSHOT_TIME));
        p.setProperty("graphics.render.mode", GlobalConf.frame.FRAME_MODE.toString());

        /** DATA **/
        p.setProperty("data.source.local", Boolean.toString(GlobalConf.data.DATA_SOURCE_LOCAL));
        p.setProperty("data.json.file", GlobalConf.data.DATA_JSON_FILE);
        p.setProperty("data.source.hostname", GlobalConf.data.OBJECT_SERVER_HOSTNAME);
        p.setProperty("data.source.port", Integer.toString(GlobalConf.data.OBJECT_SERVER_PORT));
        p.setProperty("data.source.visid", GlobalConf.data.VISUALIZATION_ID);
        p.setProperty("data.limit.mag", Float.toString(GlobalConf.data.LIMIT_MAG_LOAD));

        /** SCREEN **/
        p.setProperty("graphics.screen.width", Integer.toString(GlobalConf.screen.SCREEN_WIDTH));
        p.setProperty("graphics.screen.height", Integer.toString(GlobalConf.screen.SCREEN_HEIGHT));
        p.setProperty("graphics.screen.fullscreen.width", Integer.toString(GlobalConf.screen.FULLSCREEN_WIDTH));
        p.setProperty("graphics.screen.fullscreen.height", Integer.toString(GlobalConf.screen.FULLSCREEN_HEIGHT));
        p.setProperty("graphics.screen.fullscreen", Boolean.toString(GlobalConf.screen.FULLSCREEN));
        p.setProperty("graphics.screen.resizable", Boolean.toString(GlobalConf.screen.RESIZABLE));
        p.setProperty("graphics.screen.vsync", Boolean.toString(GlobalConf.screen.VSYNC));
        p.setProperty("graphics.screen.screenoutput", Boolean.toString(GlobalConf.screen.SCREEN_OUTPUT));

        /** PROGRAM **/
        p.setProperty("program.tutorial", Boolean.toString(GlobalConf.program.DISPLAY_TUTORIAL));
        p.setProperty("program.tutorial.script", GlobalConf.program.TUTORIAL_SCRIPT_LOCATION);
        p.setProperty("program.configdialog", Boolean.toString(GlobalConf.program.SHOW_CONFIG_DIALOG));
        p.setProperty("program.debuginfo", Boolean.toString(GlobalConf.program.SHOW_DEBUG_INFO));
        p.setProperty("program.lastchecked", df.format(GlobalConf.program.LAST_CHECKED));
        p.setProperty("program.lastversion", GlobalConf.program.LAST_VERSION);
        p.setProperty("program.versioncheckurl", GlobalConf.program.VERSION_CHECK_URL);
        p.setProperty("program.ui.theme", GlobalConf.program.UI_THEME);
        p.setProperty("program.scriptlocation", GlobalConf.program.SCRIPT_LOCATION);
        p.setProperty("program.locale", GlobalConf.program.LOCALE);
        p.setProperty("program.stereoscopic", Boolean.toString(GlobalConf.program.STEREOSCOPIC_MODE));
        p.setProperty("program.stereoscopic.profile", Integer.toString(GlobalConf.program.STEREO_PROFILE.ordinal()));

        /** SCENE **/
        p.setProperty("scene.object.fadems", Long.toString(GlobalConf.scene.OBJECT_FADE_MS));
        p.setProperty("scene.star.brightness", Float.toString(GlobalConf.scene.STAR_BRIGHTNESS));
        p.setProperty("scene.ambient", Float.toString(GlobalConf.scene.AMBIENT_LIGHT));
        p.setProperty("scene.camera.fov", Integer.toString(GlobalConf.scene.CAMERA_FOV));
        p.setProperty("scene.camera.speedlimit", Integer.toString(GlobalConf.scene.CAMERA_SPEED_LIMIT_IDX));
        p.setProperty("scene.camera.focus.vel", Float.toString(GlobalConf.scene.CAMERA_SPEED));
        p.setProperty("scene.camera.turn.vel", Float.toString(GlobalConf.scene.TURNING_SPEED));
        p.setProperty("scene.camera.rotate.vel", Float.toString(GlobalConf.scene.ROTATION_SPEED));
        p.setProperty("scene.focuslock", Boolean.toString(GlobalConf.scene.FOCUS_LOCK));
        p.setProperty("scene.labelfactor", Float.toString(GlobalConf.scene.LABEL_NUMBER_FACTOR));
        p.setProperty("scene.star.thresholdangle.quad", Double.toString(Math.toDegrees(GlobalConf.scene.STAR_TH_ANGLE_QUAD)));
        p.setProperty("scene.star.thresholdangle.point", Double.toString(Math.toDegrees(GlobalConf.scene.STAR_TH_ANGLE_POINT)));
        p.setProperty("scene.star.thresholdangle.none", Double.toString(Math.toDegrees(GlobalConf.scene.STAR_TH_ANGLE_NONE)));
        p.setProperty("scene.point.alpha.min", Float.toString(GlobalConf.scene.POINT_ALPHA_MIN));
        p.setProperty("scene.point.alpha.max", Float.toString(GlobalConf.scene.POINT_ALPHA_MAX));
        p.setProperty("scene.renderer.star", Integer.toString(GlobalConf.scene.PIXEL_RENDERER));
        p.setProperty("scene.renderer.line", Integer.toString(GlobalConf.scene.LINE_RENDERER));
        // Visibility of components
        int idx = 0;
        ComponentType[] cts = ComponentType.values();
        for (boolean b : GlobalConf.scene.VISIBILITY) {
            ComponentType ct = cts[idx];
            p.setProperty("scene.visibility." + ct.name(), Boolean.toString(b));
            idx++;
        }

        try {
            FileOutputStream fos = new FileOutputStream(propsFile);
            p.store(fos, null);
            fos.close();
            Logger.info("Configuration saved to " + propsFile.getAbsolutePath());
        } catch (Exception e) {
            Logger.error(e);
        }

    }

    private String initConfigFile(boolean ow) throws IOException {
        // Use user folder
        File userFolder = SysUtils.getGSHomeDir();
        userFolder.mkdirs();
        File userFolderConfFile = new File(userFolder, "global.properties");

        if (ow || !userFolderConfFile.exists()) {
            // Copy file
            copyFile(new File("conf" + File.separator + "global.properties"), userFolderConfFile, ow);
        }
        String props = userFolderConfFile.getAbsolutePath();
        System.setProperty("properties.file", props);
        return props;
    }

    private void copyFile(File sourceFile, File destFile, boolean ow) throws IOException {
        if (destFile.exists()) {
            if (ow) {
                // Overwrite, delete file
                destFile.delete();
            } else {
                return;
            }
        }
        // Create new
        destFile.createNewFile();

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    public int[] getMajorMinorFromString(String version) {
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

}
