package gaia.cu9.ari.gaiaorbit.client;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.client.format.GwtNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

public class HtmlLauncher extends GwtApplication {

    @Override
    public GwtApplicationConfiguration getConfig() {
        NumberFormatFactory.initialize(new GwtNumberFormatFactory());
        GwtApplicationConfiguration config = new GwtApplicationConfiguration(1024, 600);

        try {
            GlobalConf.initialize();
            config.antialiasing = GlobalConf.postprocess.POSTPROCESS_ANTIALIAS != 0 ? true : false;
        } catch (Exception e) {
            System.err.println("Error initializing GlobalConf");
            e.printStackTrace(System.err);
        }

        return config;
    }

    @Override
    public ApplicationListener getApplicationListener() {
        return new GaiaSandbox(true);
    }
}