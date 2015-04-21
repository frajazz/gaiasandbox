package gaia.cu9.ari.gaiaorbit.util.scene2d;

import java.util.Timer;
import java.util.TimerTask;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;

public class Tooltip<T extends Actor> implements EventListener {
    private static final long TOOLTIP_DELAY_MS = 750;
    private static final Timer timer = new Timer(true);

    final T tooltipObject;
    TimerTask tt;

    public Tooltip(T tooltipObject) {
	this.tooltipObject = tooltipObject;
	this.tooltipObject.setVisible(false);
    }

    @Override
    public boolean handle(Event event) {
	if (event instanceof InputEvent) {
	    InputEvent ie = (InputEvent) event;
	    if (ie.getType().equals(Type.enter)) {
		tt = new TimerTask() {
		    @Override
		    public void run() {
			tooltipObject.setPosition(Gdx.input.getX() + 10, Gdx.graphics.getHeight() - Gdx.input.getY());
			tooltipObject.setVisible(true);
			tooltipObject.setZIndex(100);
		    }
		};
		// Set timer
		timer.schedule(tt, TOOLTIP_DELAY_MS);
	    } else if (ie.getType().equals(Type.exit) || ie.getType().equals(Type.touchDown) || ie.getType().equals(Type.touchUp)) {
		// Remove
		if (tt != null) {
		    tt.cancel();
		    tt = null;
		}
		tooltipObject.setVisible(false);
	    } else if (ie.getType().equals(Type.mouseMoved)) {
		if (tooltipObject.isVisible()) {
		    tooltipObject.setPosition(Gdx.input.getX() + 10, Gdx.graphics.getHeight() - Gdx.input.getY());
		}
	    }
	}
	return false;
    }
}
