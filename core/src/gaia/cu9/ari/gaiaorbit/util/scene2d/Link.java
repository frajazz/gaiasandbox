package gaia.cu9.ari.gaiaorbit.util.scene2d;

import gaia.cu9.ari.gaiaorbit.util.GlobalResources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * Link widget.
 * @author Toni Sagrista
 *
 */
public class Link extends Label {

    private final String linkto;

    public Link(CharSequence text, LabelStyle style, String linkto) {
	super(text, style);
	this.linkto = linkto;
	initialize();
    }

    public Link(CharSequence text, Skin skin, String fontName, Color color, String linkto) {
	super(text, skin, fontName, color);
	this.linkto = linkto;
	initialize();
    }

    public Link(CharSequence text, Skin skin, String fontName, String colorName, String linkto) {
	super(text, skin, fontName, colorName);
	this.linkto = linkto;
	initialize();
    }

    public Link(CharSequence text, Skin skin, String styleName, String linkto) {
	super(text, skin, styleName);
	this.linkto = linkto;
	initialize();
    }

    public Link(CharSequence text, Skin skin, String linkto) {
	super(text, skin);
	this.linkto = linkto;
	initialize();
    }

    private void initialize() {
	this.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof InputEvent) {
		    Type type = ((InputEvent) event).getType();
		    // Click
		    if (type == Type.touchUp && ((InputEvent) event).getButton() == Buttons.LEFT) {
			Gdx.net.openURI(linkto);
		    } else if (type == Type.enter) {
			Gdx.input.setCursorImage(GlobalResources.linkCursor, 4, 0);
		    } else if (type == Type.exit) {
			Gdx.input.setCursorImage(null, 0, 0);
		    }
		    return true;
		}
		return false;
	    }
	});
    }

}
