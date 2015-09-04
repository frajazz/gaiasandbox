package gaia.cu9.ari.gaiaorbit.desktop.render;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import com.badlogic.gdx.Gdx;

public class FullscreenCmd implements IObserver {

    public static FullscreenCmd instance;

    public static void initialize() {
        FullscreenCmd.instance = new FullscreenCmd();
    }

    private FullscreenCmd() {
        EventManager.instance.subscribe(this, Events.FULLSCREEN_CMD);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case FULLSCREEN_CMD:
            boolean toFullscreen = data.length >= 1 ? (Boolean) data[0] : !Gdx.graphics.isFullscreen();
            int width;
            int height;
            if (toFullscreen) {
                width = GlobalConf.screen.FULLSCREEN_WIDTH;
                height = GlobalConf.screen.FULLSCREEN_HEIGHT;
                GlobalConf.screen.SCREEN_WIDTH = Gdx.graphics.getWidth();
                GlobalConf.screen.SCREEN_HEIGHT = Gdx.graphics.getHeight();
            } else {
                width = GlobalConf.screen.SCREEN_WIDTH;
                height = GlobalConf.screen.SCREEN_HEIGHT;
            }
            // Only switch if needed
            if (Gdx.graphics.isFullscreen() != toFullscreen) {
                Gdx.graphics.setDisplayMode(width, height, toFullscreen);
            }
            break;

        }
    }
}
