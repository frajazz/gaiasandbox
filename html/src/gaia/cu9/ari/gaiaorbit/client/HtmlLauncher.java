package gaia.cu9.ari.gaiaorbit.client;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.client.format.GwtDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.client.format.GwtNumberFormatFactory;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

public class HtmlLauncher extends GwtApplication {

    @Override
    public GwtApplicationConfiguration getConfig() {

        // Init date format
        GwtDateFormatFactory.initialize(new GwtDateFormatFactory());
        // Initialize number format
        GwtNumberFormatFactory.initialize(new GwtNumberFormatFactory());

        return new GwtApplicationConfiguration(480, 320);
    }

    @Override
    public ApplicationListener getApplicationListener() {
        return new GaiaSandbox(true);
    }
}