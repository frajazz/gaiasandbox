package gaia.cu9.ari.gaiaorbit.util.scene2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class CollapsiblePanel extends Table {
    private float ownwidth = 0f, ownheight = 0f;

    private Actor main;
    private boolean collapsed, isResizable;
    private float collapseHeight;
    private float expandHeight;
    private int titleAlignment = Align.center;

    private Vector2 vec2;

    private CollapsiblePanelStyle style;
    private ImageButtonStyle istyle;
    private LabelStyle lstyle;
    private String title;
    private OwnLabel titleLabel;
    private OwnImageButton titleKnob;
    private HorizontalGroup header;

    public CollapsiblePanel(String title, Skin skin) {
	this(title, skin.get(CollapsiblePanelStyle.class));
    }

    public CollapsiblePanel(String title, Skin skin, String styleName) {
	this(title, skin.get(styleName, CollapsiblePanelStyle.class));
    }

    public CollapsiblePanel(String title, CollapsiblePanelStyle style) {
	this.main = this;
	this.isResizable = true;
	if (title == null)
	    throw new IllegalArgumentException("title cannot be null.");
	this.title = title;

	// Create header
	header = new HorizontalGroup();
	header.align(titleAlignment);

	add(header).align(titleAlignment);
	row();

	setTouchable(Touchable.enabled);
	setStyle(style);
	setTitle(title);

	collapseHeight = header.getHeight();

	vec2 = new Vector2();
	titleKnob.addListener(new ClickListener() {
	    private float startx, starty;

	    @Override
	    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
		startx = x + main.getX();
		starty = y + main.getY();
		return super.touchDown(event, x, y, pointer, button);
	    }

	    @Override
	    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
		float endx = x + main.getX();
		float endy = y + main.getY();
		vec2.set(endx - startx, endy - starty);
		// pixels of margin
		if (vec2.len() < 3) {
		    toggleCollapsed();
		}
		super.touchUp(event, x, y, pointer, button);
	    }

	});

    }

    public void setStyle(CollapsiblePanelStyle style) {
	if (style == null)
	    throw new IllegalArgumentException("style cannot be null.");
	this.style = style;

	lstyle = new LabelStyle(style.titleFont, style.titleFontColor);
	istyle = new ImageButtonStyle(style.imageUp, style.imageDown, style.imageDown, style.imageUp, style.imageDown, style.imageDown);

	if (title != null)
	    setTitle(title);
	invalidateHierarchy();
    }

    public void setTitle(String title) {
	this.title = title;
	this.titleLabel = new OwnLabel(title, lstyle);
	this.titleKnob = new OwnImageButton(istyle);
	this.titleKnob.setChecked(true);

	header.clear();
	header.addActor(titleLabel);
	header.space(10);
	header.addActor(titleKnob);

	header.pack();
    }

    /** @param titleAlignment {@link Align} */
    public void setTitleAlignment(int titleAlignment) {
	this.titleAlignment = titleAlignment;
	header.align(titleAlignment);
    }

    public void expand() {
	if (!collapsed)
	    return;
	setHeight(expandHeight);
	setY(getY() - expandHeight + collapseHeight);
	collapsed = false;
    }

    public void collapse() {
	if (collapsed)
	    return;
	expandHeight = getHeight();
	setHeight(collapseHeight);
	setY(getY() + expandHeight - collapseHeight);
	collapsed = true;
    }

    public void toggleCollapsed() {
	if (collapsed)
	    expand();
	else
	    collapse();
    }

    public boolean isCollapsed() {
	return collapsed;
    }

    @Override
    public void pack() {
	collapsed = false;
	super.pack();
    }

    /** The style for a window, see {@link CollapsiblePanel}.
     * @author Nathan Sweet */
    static public class CollapsiblePanelStyle {
	/** Optional. */
	public BitmapFont titleFont;
	/** Optional. */
	public Color titleFontColor = new Color(1, 1, 1, 1);
	public Drawable imageUp, imageDown;

	public CollapsiblePanelStyle() {
	}

	public CollapsiblePanelStyle(BitmapFont titleFont, Color titleFontColor, Drawable imageUp, Drawable imageDown) {
	    this.titleFont = titleFont;
	    this.titleFontColor.set(titleFontColor);
	    this.imageUp = imageUp;
	    this.imageDown = imageDown;
	}

	public CollapsiblePanelStyle(CollapsiblePanelStyle style) {
	    this.titleFont = style.titleFont;
	    this.titleFontColor = new Color(style.titleFontColor);
	}
    }

    public void draw(Batch batch, float parentAlpha) {
	Stage stage = getStage();
	if (stage.getKeyboardFocus() == null)
	    stage.setKeyboardFocus(this);

	super.draw(batch, parentAlpha);
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

    public boolean isResizable() {
	return isResizable;
    }

    public void setResizable(boolean isResizable) {
	this.isResizable = isResizable;
    }
}
