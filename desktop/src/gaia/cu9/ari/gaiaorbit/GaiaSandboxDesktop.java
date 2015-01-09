package gaia.cu9.ari.gaiaorbit;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.gui.swing.ConfigDialog;
import gaia.cu9.ari.gaiaorbit.gui.swing.HelpDialog;
import gaia.cu9.ari.gaiaorbit.gui.swing.ScriptDialog;
import gaia.cu9.ari.gaiaorbit.interfce.KeyMappings;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import sandbox.script.JythonFactory;

import com.alee.laf.WebLookAndFeel;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

/**
 * Main class for the desktop launcher
 * @author Toni Sagrista
 *
 */
public class GaiaSandboxDesktop implements IObserver {
    private static GaiaSandboxDesktop gsd;

    public static void main(String[] args) {

	try {

	    UIManager.setLookAndFeel("com.alee.laf.WebLookAndFeel");
	    WebLookAndFeel.setAllowLinuxTransparency(false);
	    //UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
	    setUIFont(new javax.swing.plaf.FontUIResource("SansSerif", Font.PLAIN, 10));

	    File confFile = new File(System.getProperty("properties.file"));
	    FileInputStream fis = new FileInputStream(confFile);
	    // This should work for the normal execution
	    InputStream version = GaiaSandboxDesktop.class.getResourceAsStream("/version");
	    if (version == null) {
		// In case of running in 'developer' mode
		version = new FileInputStream(new File("../android/assets/data/dummyversion"));
	    }
	    GlobalConf.initialize(fis, version);
	    fis.close();

	    KeyMappings.initialize();
	    gsd = new GaiaSandboxDesktop();
	    gsd.init();
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	}

    }

    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
	java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
	while (keys.hasMoreElements()) {
	    Object key = keys.nextElement();
	    Object value = UIManager.get(key);
	    if (value != null && value instanceof javax.swing.plaf.FontUIResource && ((FontUIResource) value).getSize() > f.getSize()) {
		UIManager.put(key, f);
	    }
	}
    }

    public GaiaSandboxDesktop() {
	super();
	EventManager.getInstance().subscribe(this, Events.SHOW_PREFERENCES_ACTION, Events.SHOW_ABOUT_ACTION, Events.SHOW_RUNSCRIPT_ACTION, Events.JAVA_EXCEPTION);
    }

    private void init() {
	// Show configuration
	if (GlobalConf.instance.SHOW_CONFIG_DIALOG) {
	    new ConfigDialog(this, true);
	} else {
	    launchMainApp();
	}
    }

    public void launchMainApp() {
	LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
	LwjglApplicationConfiguration.disableAudio = true;
	cfg.title = GlobalConf.instance.getFullApplicationName();
	cfg.fullscreen = GlobalConf.instance.FULLSCREEN;
	cfg.resizable = GlobalConf.instance.RESIZABLE;
	cfg.width = GlobalConf.instance.getScreenWidth();
	cfg.height = GlobalConf.instance.getScreenHeight();
	cfg.samples = MathUtilsd.clamp(GlobalConf.instance.POSTPROCESS_ANTIALIAS, 0, 16);
	cfg.vSyncEnabled = GlobalConf.instance.VSYNC;
	cfg.foregroundFPS = 0;
	cfg.backgroundFPS = 30;
	cfg.useGL30 = false;
	cfg.addIcon("icon/ic_launcher.png", Files.FileType.Internal);

	System.out.println("Display mode set to " + cfg.width + "x" + cfg.height + ", fullscreen: " + cfg.fullscreen);

	// Init scripting
	JythonFactory.initialize();
	// Launch app
	new LwjglApplication(new GaiaSandbox(true), cfg);
    }

    @Override
    public void notify(Events event, Object... data) {
	switch (event) {
	case SHOW_RUNSCRIPT_ACTION:
	    // Exit fullscreen
	    EventManager.getInstance().post(Events.FULLSCREEN_CMD, false);
	    Gdx.app.postRunnable(new Runnable() {

		@Override
		public void run() {
		    JFrame frame = new ScriptDialog();
		    frame.toFront();
		}

	    });

	    break;
	case SHOW_PREFERENCES_ACTION:
	    // Exit fullscreen
	    EventManager.getInstance().post(Events.FULLSCREEN_CMD, false);
	    Gdx.app.postRunnable(new Runnable() {

		@Override
		public void run() {
		    JFrame frame = new ConfigDialog(gsd, false);
		    frame.toFront();
		}

	    });
	    break;
	case SHOW_ABOUT_ACTION:
	    // Exit fullscreen
	    EventManager.getInstance().post(Events.FULLSCREEN_CMD, false);
	    Gdx.app.postRunnable(new Runnable() {

		@Override
		public void run() {
		    JFrame frame = new HelpDialog();
		    frame.toFront();
		}

	    });
	    break;
	case JAVA_EXCEPTION:
	    ((Throwable) data[0]).printStackTrace(System.err);
	    break;
	}

    }

}
