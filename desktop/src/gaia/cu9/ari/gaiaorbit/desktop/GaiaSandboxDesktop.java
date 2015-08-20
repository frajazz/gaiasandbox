package gaia.cu9.ari.gaiaorbit.desktop;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.util.DesktopDataFilesFactory;
import gaia.cu9.ari.gaiaorbit.util.DataFilesFactory;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class GaiaSandboxDesktop {

    public static void main(String[] args) {
        NumberFormatFactory.initialize(new DesktopNumberFormatFactory());
        DateFormatFactory.initialize(new DesktopDateFormatFactory());
        DataFilesFactory.initialize(new DesktopDataFilesFactory());
        try {
            GlobalConf.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }

        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        LwjglApplicationConfiguration.disableAudio = true;
        cfg.title = GlobalConf.getFullApplicationName();
        cfg.fullscreen = false;
        cfg.resizable = false;
        cfg.width = 1024;
        cfg.height = 600;
        cfg.samples = MathUtilsd.clamp(GlobalConf.postprocess.POSTPROCESS_ANTIALIAS, 0, 16);
        cfg.vSyncEnabled = false;
        cfg.foregroundFPS = 0;
        cfg.backgroundFPS = 0;
        cfg.addIcon("icon/ic_launcher.png", Files.FileType.Internal);

        // Launch app
        new LwjglApplication(new GaiaSandbox(true), cfg);
    }
}
