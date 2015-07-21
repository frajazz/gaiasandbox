package gaia.cu9.ari.gaiaorbit.client;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;

public class HtmlLauncher extends GwtApplication {

    @Override
    public GwtApplicationConfiguration getConfig() {
        return new GwtApplicationConfiguration(1024, 600);
    }

    @Override
    public ApplicationListener getApplicationListener() {
        return new GaiaSandbox(true);
    }
}