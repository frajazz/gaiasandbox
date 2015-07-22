package gaia.cu9.ari.gaiaorbit.client;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.client.format.GwtNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

public class HtmlLauncher extends GwtApplication {

    @Override
    public GwtApplicationConfiguration getConfig() {
        NumberFormatFactory.initialize(new GwtNumberFormatFactory());
        return new GwtApplicationConfiguration(1024, 600);
    }

    @Override
    public ApplicationListener getApplicationListener() {
        return new GaiaSandbox(true);
    }
}