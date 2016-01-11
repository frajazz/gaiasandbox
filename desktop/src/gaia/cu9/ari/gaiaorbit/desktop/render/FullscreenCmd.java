package gaia.cu9.ari.gaiaorbit.desktop.render;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;

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
			boolean toFullscreen = data.length >= 1 ? (Boolean) data[0]
					: !Gdx.graphics.isFullscreen();
			int width;
			int height;
			// get the current display mode of the monitor the window is on
			DisplayMode mode = Gdx.graphics.getDisplayMode();
			if (toFullscreen) {
				width = GlobalConf.screen.FULLSCREEN_WIDTH;
				height = GlobalConf.screen.FULLSCREEN_HEIGHT;
				GlobalConf.screen.SCREEN_WIDTH = Gdx.graphics.getWidth();
				GlobalConf.screen.SCREEN_HEIGHT = Gdx.graphics.getHeight();

				// set the window to fullscreen mode
				Gdx.graphics.setFullscreenMode(mode);

			} else {
				width = GlobalConf.screen.SCREEN_WIDTH;
				height = GlobalConf.screen.SCREEN_HEIGHT;

				// set the window to fullscreen mode
				Gdx.graphics.setWindowedMode(width, height);
			}
			break;

		}
	}
}
