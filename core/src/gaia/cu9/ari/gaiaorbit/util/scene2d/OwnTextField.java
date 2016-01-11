package gaia.cu9.ari.gaiaorbit.util.scene2d;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;

/**
 * TextButton in which the cursor changes when the mouse rolls over. It also
 * fixes the size issue.
 * 
 * @author Toni Sagrista
 *
 */
public class OwnTextField extends TextField {

	private float ownwidth = 0f, ownheight = 0f;

	public OwnTextField(String text, Skin skin) {
		super(text, skin);
	}

	public OwnTextField(String text, Skin skin, String styleName) {
		super(text, skin, styleName);
	}

	public OwnTextField(String text, TextFieldStyle style) {
		super(text, style);
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