package gaia.cu9.ari.gaiaorbit.util.scene2d;

import gaia.cu9.ari.gaiaorbit.util.GlobalResources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/**
 * ImageButton in which the cursor changes when the mouse rolls over.
 * @author Toni Sagrista
 *
 */
public class OwnImageButton extends ImageButton {

    public OwnImageButton(Skin skin) {
	super(skin);
	initialize();
    }

    public OwnImageButton(Skin skin, String styleName) {
	super(skin, styleName);
	initialize();
    }

    public OwnImageButton(ImageButtonStyle style) {
	super(style);
	initialize();
    }

    private void initialize() {
	this.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof InputEvent) {
		    Type type = ((InputEvent) event).getType();
		    if (type == Type.enter) {
			Gdx.input.setCursorImage(GlobalResources.linkCursor, 0, 0);
			return true;
		    } else if (type == Type.exit) {
			Gdx.input.setCursorImage(null, 0, 0);
			return true;
		    }

		}
		return false;
	    }
	});
    }

}
