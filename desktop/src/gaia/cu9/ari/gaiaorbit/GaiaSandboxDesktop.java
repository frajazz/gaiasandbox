package gaia.cu9.ari.gaiaorbit;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import gaia.cu9.ari.gaiaorbit.data.FileLocator;
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
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.SysUtils;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import sandbox.script.JythonFactory;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Main class for the desktop launcher
 * @author Toni Sagrista
 *
 */
public class GaiaSandboxDesktop implements IObserver {
    private static GaiaSandboxDesktop gsd;

    public static void main(String[] args) {

        try {
            gsd = new GaiaSandboxDesktop();

            //UIManager.setLookAndFeel("com.alee.laf.WebLookAndFeel");
            //WebLookAndFeel.setAllowLinuxTransparency(false);
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
            //UIManager.setLookAndFeel("com.jgoodies.looks.plastic.PlasticXPLookAndFeel");
            UIManager.setLookAndFeel("com.pagosoft.plaf.PgsLookAndFeel");
            //UIManager.setLookAndFeel(new SyntheticaStandardLookAndFeel());

            setUIFont(new javax.swing.plaf.FontUIResource("SansSerif", Font.PLAIN, 10));

            String props = System.getProperty("properties.file");
            if (props == null || props.isEmpty()) {
                props = initConfigFile(false);
            }

            File confFile = new File(props);
            FileInputStream fis = new FileInputStream(confFile);
            // This should work for the normal execution
            InputStream version = GaiaSandboxDesktop.class.getResourceAsStream("/version");
            if (version == null) {
                // In case of running in 'developer' mode
                version = new FileInputStream(new File("../android/assets/data/dummyversion"));
            }
            try {
                FileLocator.initialize();
                GlobalConf.initialize(fis, version);
            } catch (Exception e) {
                // Retry
                FileInputStream fis2 = null;
                try {
                    confFile = new File(initConfigFile(true));
                    fis2 = new FileInputStream(confFile);

                    GlobalConf.initialize(fis2, version);
                } catch (Exception e1) {
                    // Total error!
                    Logger.error(e1);
                    return;
                } finally {
                    if (fis2 != null)
                        fis2.close();

                }
                Logger.info("Overwritten outated configuration file in " + confFile.getAbsolutePath());
            } finally {
                fis.close();
            }

            // Initialize i18n
            I18n.initialize("./data/i18n/gsbundle");
            // Dev mode
            I18n.initialize("../android/assets/i18n/gsbundle");

            // Initialize icons
            IconManager.initialise(new File("./data/ui/"));

            // Initialize key mappings
            KeyMappings.initialize();

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
        EventManager.instance.subscribe(this, Events.SHOW_PREFERENCES_ACTION, Events.SHOW_ABOUT_ACTION, Events.SHOW_RUNSCRIPT_ACTION, Events.JAVA_EXCEPTION, Events.SHOW_PLAYCAMERA_ACTION, Events.POST_NOTIFICATION);
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
        cfg.foregroundFPS = 0;
        cfg.backgroundFPS = 0;
        cfg.addIcon("icon/ic_launcher.png", Files.FileType.Internal);

        System.out.println("Display mode set to " + cfg.width + "x" + cfg.height + ", fullscreen: " + cfg.fullscreen);

        // Init scripting
        JythonFactory.initialize();
        // Launch app
        new LwjglApplication(new GaiaSandbox(true), cfg);

        EventManager.instance.unsubscribe(this, Events.POST_NOTIFICATION, Events.JAVA_EXCEPTION);
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
                    SecurityManager sm = System.getSecurityManager();
                    System.setSecurityManager(null);
                    JFileChooser chooser = new JFileChooser();

                    chooser.setFileHidingEnabled(false);
                    chooser.setMultiSelectionEnabled(false);
                    chooser.setAcceptAllFileFilterUsed(false);
                    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    chooser.setCurrentDirectory(new File(System.getProperty("java.io.tmpdir")));

                    // Filter
                    FileFilter filter = new FileNameExtensionFilter("Camera data files", new String[] { "dat", "txt", "csv" });
                    chooser.addChoosableFileFilter(filter);
                    chooser.setFileFilter(filter);

                    int v = chooser.showOpenDialog(null);

                    switch (v) {
                    case JFileChooser.APPROVE_OPTION:
                        File choice = null;
                        if (chooser.getSelectedFile() != null) {
                            File file = chooser.getSelectedFile();
                            // Send command to play file
                            EventManager.instance.post(Events.PLAY_CAMERA_CMD, file.getAbsolutePath());
                        }

                        break;
                    case JFileChooser.CANCEL_OPTION:
                    case JFileChooser.ERROR_OPTION:
                    }
                    chooser.removeAll();
                    chooser = null;
                    System.setSecurityManager(sm);

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
        case POST_NOTIFICATION:
            System.out.println((String) data[0]);
            break;
        }

    }

    private static String initConfigFile(boolean ow) throws IOException {
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

    private static void copyFile(File sourceFile, File destFile, boolean ow) throws IOException {
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
}
