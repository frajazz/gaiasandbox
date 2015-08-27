package gaia.cu9.ari.gaiaorbit.client;

import gaia.cu9.ari.gaiaorbit.GaiaSandbox;
import gaia.cu9.ari.gaiaorbit.client.format.GwtDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.client.format.GwtNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.client.script.DummyFactory;
import gaia.cu9.ari.gaiaorbit.client.util.WebGLConfInitLite;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.script.ScriptingFactory;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.google.gwt.dom.client.Element;

public class GaiaSandboxWebGL extends GwtApplication implements IObserver {

    @Override
    public GwtApplicationConfiguration getConfig() {
        NumberFormatFactory.initialize(new GwtNumberFormatFactory());
        DateFormatFactory.initialize(new GwtDateFormatFactory());
        ScriptingFactory.initialize(new DummyFactory());

        GwtApplicationConfiguration config = new GwtApplicationConfiguration(1024, 600);

        try {
            ConfInit.initialize(new WebGLConfInitLite());
            ConfInit.instance.initGlobalConf();
            config.antialiasing = GlobalConf.postprocess.POSTPROCESS_ANTIALIAS != 0 ? true : false;
        } catch (Exception e) {
            System.err.println("Error initializing GlobalConf");
            e.printStackTrace(System.err);
        }

        return config;
    }

    @Override
    public ApplicationListener getApplicationListener() {
        EventManager.instance.subscribe(this, Events.FOCUS_CHANGED);
        return new GaiaSandbox(true);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case FOCUS_CHANGED:
            String name = "";
            if (data[0] instanceof String) {
                name = (String) data[0];
            } else {
                CelestialBody cb = (CelestialBody) data[0];
                name = cb.name;
            }

            //            Element iframe = DOM.getElementById("wikip-info-ifr");
            //            iframe.setAttribute("scr", "https://en.m.wikipedia.org/wiki/" + name);
            //            reloadIFrame(iframe);
        }

    }

    protected native void reloadIFrame(Element iframeEl) /*-{
                                                         iframeEl.contentWindow.location.reload(true);
                                                         }-*/;
}