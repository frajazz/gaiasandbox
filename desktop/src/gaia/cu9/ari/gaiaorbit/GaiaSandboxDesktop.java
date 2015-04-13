package gaia.cu9.ari.gaiaorbit;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.gui.swing.ConfigDialog;
import gaia.cu9.ari.gaiaorbit.gui.swing.HelpDialog;
import gaia.cu9.ari.gaiaorbit.gui.swing.IconManager;
import gaia.cu9.ari.gaiaorbit.gui.swing.ScriptDialog;
import gaia.cu9.ari.gaiaorbit.interfce.KeyMappings;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import sandbox.script.JythonFactory;

import com.alee.laf.WebLookAndFeel;
import com.alee.laf.filechooser.WebFileChooser;
import com.alee.laf.filechooser.WebFileChooserPanel;
import com.alee.laf.scroll.WebScrollPane;
import com.alee.laf.splitpane.WebSplitPane;
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

	    // Initialize i18n
	    I18n.initialize("./data/i18n/gsbundle");
	    // Dev mode
	    I18n.initialize("../android/assets/i18n/gsbundle");

	    // Initialize icons
	    IconManager.initialise(new File("data/ui/"));

	    // Initialize key mappings
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
	EventManager.instance.subscribe(this, Events.SHOW_PREFERENCES_ACTION, Events.SHOW_ABOUT_ACTION, Events.SHOW_RUNSCRIPT_ACTION, Events.JAVA_EXCEPTION, Events.SHOW_PLAYCAMERA_ACTION);
    }

    private void init() {
	// Show configuration
	if (GlobalConf.program.SHOW_CONFIG_DIALOG) {
	    new ConfigDialog(this, true);
	} else {
	    launchMainApp();
	}
    }

    public void launchMainApp() {
	LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
	LwjglApplicationConfiguration.disableAudio = true;
	cfg.title = GlobalConf.getFullApplicationName();
	cfg.fullscreen = GlobalConf.screen.FULLSCREEN;
	cfg.resizable = GlobalConf.screen.RESIZABLE;
	cfg.width = GlobalConf.screen.getScreenWidth();
	cfg.height = GlobalConf.screen.getScreenHeight();
	cfg.samples = MathUtilsd.clamp(GlobalConf.postprocess.POSTPROCESS_ANTIALIAS, 0, 16);
	cfg.vSyncEnabled = GlobalConf.screen.VSYNC;
	cfg.foregroundFPS = 400;
	cfg.backgroundFPS = 30;
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
	case SHOW_PLAYCAMERA_ACTION:
	    // Exit fullscreen
	    EventManager.instance.post(Events.FULLSCREEN_CMD, false);
	    Gdx.app.postRunnable(new Runnable() {

		@Override
		public void run() {
		    // Show file dialog
		    WebFileChooser fc = new WebFileChooser();
		    fc.setCurrentDirectory(new File(System.getProperty("java.io.tmpdir")));
		    WebSplitPane wsp = ((WebSplitPane) ((WebFileChooserPanel) fc.getComponents()[0]).getComponent(1));
		    ((WebScrollPane) wsp.getComponent(1)).getVerticalScrollBar().setUnitIncrement(50);
		    ((WebScrollPane) wsp.getComponent(2)).getVerticalScrollBar().setUnitIncrement(50);
		    int returnVal = fc.showOpenDialog(fc);

		    if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			// Send command to play file
			EventManager.instance.post(Events.PLAY_CAMERA_CMD, file.getAbsolutePath());
		    } else {
			// Cancelled
		    }
		}

	    });
	    break;
	case SHOW_RUNSCRIPT_ACTION:
	    // Exit fullscreen
	    EventManager.instance.post(Events.FULLSCREEN_CMD, false);
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
	    EventManager.instance.post(Events.FULLSCREEN_CMD, false);
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
	    EventManager.instance.post(Events.FULLSCREEN_CMD, false);
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
