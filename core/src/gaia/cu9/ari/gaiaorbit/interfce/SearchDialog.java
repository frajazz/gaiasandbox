package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.I18n;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

public class SearchDialog extends Window {
    private final SearchDialog me;
    private final IGui gui;
    private final TextField searchInput;

    public SearchDialog(IGui gui, Skin skin, final ISceneGraph sg) {
	super("Search", skin);
	this.me = this;
	this.gui = gui;
	searchInput = new TextField("", skin);
	searchInput.setMessageText(I18n.bundle.get("gui.objects.search"));
	searchInput.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof InputEvent) {
		    InputEvent ie = (InputEvent) event;
		    if (ie.getType() == Type.keyUp) {
			if (ie.getKeyCode() == Keys.ESCAPE || ie.getKeyCode() == Keys.ENTER) {
			    me.remove();
			} else {
			    String text = searchInput.getText();
			    if (sg.containsNode(text.toLowerCase())) {
				SceneGraphNode node = sg.getNode(text.toLowerCase());
				if (node instanceof CelestialBody) {
				    EventManager.getInstance().post(Events.FOCUS_CHANGE_CMD, node, true);
				    searchInput.selectAll();
				}
			    }
			}
		    }
		    return true;
		}
		return false;
	    }
	});

	HorizontalGroup buttonGroup = new HorizontalGroup();
	TextButton cls = new OwnTextButton(I18n.bundle.get("gui.close"), skin, "default");
	cls.setName("close");
	cls.addListener(new EventListener() {
	    @Override
	    public boolean handle(Event event) {
		if (event instanceof ChangeEvent) {
		    me.remove();
		    return true;
		}

		return false;
	    }

	});
	buttonGroup.addActor(cls);
	cls.setHeight(20);
	buttonGroup.align(Align.right).space(10);

	add(searchInput).top().left().expand().row();
	add(buttonGroup).pad(5, 0, 0, 0).bottom().right().expand();
	setTitleAlignment(Align.left);
	setModal(true);
	pack();

	this.setPosition(gui.getGuiStage().getWidth() / 2f - this.getWidth() / 2f, gui.getGuiStage().getHeight() / 2f - this.getHeight() / 2f);

    }

    public void clearText() {
	searchInput.setText("");
    }

    public void display() {
	if (!gui.getGuiStage().getActors().contains(me, true))
	    gui.getGuiStage().addActor(this);
	gui.getGuiStage().setKeyboardFocus(searchInput);
    }
}
