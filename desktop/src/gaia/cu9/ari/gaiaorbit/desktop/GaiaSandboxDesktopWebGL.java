package gaia.cu9.ari.gaiaorbit.desktop;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.data.SceneGraphImplementationProvider;
import gaia.cu9.ari.gaiaorbit.data.WebGLSceneGraphImplementationProvider;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.render.DesktopPostProcessorFactory;
import gaia.cu9.ari.gaiaorbit.desktop.util.WebGLConfInit;
import gaia.cu9.ari.gaiaorbit.render.PostProcessorFactory;
import gaia.cu9.ari.gaiaorbit.script.DummyFactory;
import gaia.cu9.ari.gaiaorbit.script.ScriptingFactory;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.concurrent.SingleThreadIndexer;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadIndexer;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class GaiaSandboxDesktopWebGL {

	public static void main(String[] args) throws Exception {
        NumberFormatFactory.initialize(new DesktopNumberFormatFactory());
        DateFormatFactory.initialize(new DesktopDateFormatFactory());
        ScriptingFactory.initialize(new DummyFactory());
        ConfInit.initialize(new WebGLConfInit());
        PostProcessorFactory.initialize(new DesktopPostProcessorFactory());
        ThreadIndexer.initialize(new SingleThreadIndexer());
        SceneGraphImplementationProvider.initialize(new WebGLSceneGraphImplementationProvider());

        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.disableAudio(true);
        cfg.setTitle(GlobalConf.getFullApplicationName());
        cfg.setWindowedMode(1024, 600);
        cfg.setResizable(false);
        int samples = MathUtilsd.clamp(GlobalConf.postprocess.POSTPROCESS_ANTIALIAS, 0, 16);
        cfg.setBackbufferConfig(8, 8, 8, 8, 16, 0, samples);
        
        cfg.useVsync(false);
//        cfg.addIcon("icon/ic_launcher.png", Files.FileType.Internal);

        // Launch app
        new Lwjgl3Application(new GaiaSandbox(), cfg);
    }
}
