package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;

/**
 * Class that wraps a modal window adding some actors to transform it into a tutorial window.
 * @author Toni Sagrista
 *
 */
public class TutorialWindow extends Window {
    public enum LayoutType {
	L_HORIZONTAL, L_VERTICAL, L_SCROLL
    };

    private Skin skin;
    private String titleClass, buttonClass;
    private Label contentTitle;
    private Table mainLayout;
    private WidgetGroup contentLayout;
    private HorizontalGroup buttonGroup;

    public TutorialWindow(String w_title, Skin skin, String titleClass, String buttonClass) {
	super(w_title, skin);
	setModal(true);
	this.skin = skin;
	this.titleClass = titleClass;
	this.buttonClass = buttonClass;
	this.padRight(10);
	this.padBottom(10);
	this.padTop(20);
    }

    public void initialize(String contentTitle, LayoutType layoutType) {
	this.contentTitle = new Label(contentTitle, skin, titleClass);
	WidgetGroup holder = null;
	switch (layoutType) {
	case L_HORIZONTAL:
	    HorizontalGroup hg = new HorizontalGroup().top().space(10);
	    contentLayout = hg;
	    holder = contentLayout;
	    break;
	case L_VERTICAL:
	    VerticalGroup vg = new VerticalGroup().left().space(10);
	    contentLayout = vg;
	    holder = contentLayout;
	    break;
	case L_SCROLL:
	    contentLayout = new VerticalGroup().align(Align.top).space(10);
	    holder = new OwnScrollPane(contentLayout, skin);
	    break;
	}

	// Create button group
	buttonGroup = new HorizontalGroup();
	buttonGroup.align(Align.right).space(10);

	// Main layout
	mainLayout = new Table();
	mainLayout.setFillParent(true);
	mainLayout.pad(30, 10, 10, 10);
	// Align to top left
	mainLayout.top().left();

	mainLayout.add(this.contentTitle).left().space(5);
	mainLayout.row();
	mainLayout.add(holder).left().space(5);

	this.add(mainLayout).top().left().expand();
	this.add(buttonGroup).bottom().right().expand();
	setTitleAlignment(Align.left);
    }

    public void addButtons(boolean prev, boolean next, boolean close) {
	if (prev) {
	    TextButton prv = new OwnTextButton("<< Prev", skin, buttonClass);
	    prv.setName("prev");
	    buttonGroup.addActor(prv);
	    prv.setHeight(20);
	}
	if (close) {
	    TextButton cls = new OwnTextButton("Close", skin, buttonClass);
	    cls.setName("close");
	    buttonGroup.addActor(cls);
	    cls.setHeight(20);
	}
	if (next) {
	    TextButton nxt = new OwnTextButton("Next >>", skin, buttonClass);
	    nxt.setName("next");
	    buttonGroup.addActor(nxt);
	    nxt.setHeight(20);
	}
    }

    public float getContentHeight() {
	return mainLayout.getHeight();
    }

    /**
     * Adds a new actor to the tutorial. The TutorialWindow must have been initialized to call this.
     */
    public void addTutorialActor(Actor actor) {
	contentLayout.addActor(actor);
    }

}
