package gaia.cu9.ari.gaiaorbit.util.scene2d;

import gaia.cu9.ari.gaiaorbit.util.GlobalResources;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cursor.SystemCursor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;

/**
 * TextButton in which the cursor changes when the mouse rolls over. It also
 * fixes the size issue.
 * 
 * @author Toni Sagrista
 *
 */
public class OwnTextButton extends TextButton {

	private float ownwidth = 0f, ownheight = 0f;
	OwnTextButton me;

	public OwnTextButton(String text, Skin skin) {
		super(text, skin);
		this.me = this;
		initialize();
	}

	public OwnTextButton(String text, Skin skin, String styleName) {
		super(text, skin, styleName);
		this.me = this;
		initialize();
	}

	public OwnTextButton(String text, TextButtonStyle style) {
		super(text, style);
		this.me = this;
		initialize();
	}

	private void initialize() {
		this.addListener(new EventListener() {
			@Override
			public boolean handle(Event event) {
				if (event instanceof InputEvent) {
					Type type = ((InputEvent) event).getType();
					if (type == Type.enter) {
						if (!me.isDisabled())
							Gdx.graphics.setCursor(Gdx.graphics.newCursor(
									GlobalResources.linkCursor, 4, 0));
						return true;
					} else if (type == Type.exit) {
						Gdx.graphics.setSystemCursor(SystemCursor.Arrow);
						return true;
					}

				}
				return false;
			}
		});
	}

	@Override
	public void setWidth(float width) {
		ownwidth = width;
		super.setWidth(width);
	}

	@Override
	public void setHeight(float height) {
		ownheight = height;
		super.setHeight(height);
	}

	@Override
	public void setSize(float width, float height) {
		ownwidth = width;
		ownheight = height;
		super.setSize(width, height);
	}

	@Override
	public float getPrefWidth() {
		if (ownwidth != 0) {
			return ownwidth;
		} else {
			return super.getPrefWidth();
		}
	}

	@Override
	public float getPrefHeight() {
		if (ownheight != 0) {
			return ownheight;
		} else {
			return super.getPrefHeight();
		}
	}

}
