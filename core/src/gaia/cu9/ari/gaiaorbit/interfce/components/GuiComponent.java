package gaia.cu9.ari.gaiaorbit.interfce.components;

import gaia.cu9.ari.gaiaorbit.util.I18n;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

/** 
 * A GUI component
 * @author Toni Sagrista
 *
 */
public abstract class GuiComponent {

    protected static List<Actor> tooltips = new ArrayList<Actor>(30);
    protected static final int HPADDING = 10;
    protected Actor component;
    protected Skin skin;
    protected Stage stage;

    public GuiComponent(Skin skin, Stage stage) {
	this.skin = skin;
	this.stage = stage;
    }

    /**
     * Initializes the component
     */
    public abstract void initialize();

    public Actor getActor() {
	return component;
    }

    protected String txt(String key) {
	return I18n.bundle.get(key);
    }

    protected String txt(String key, Object... params) {
	return I18n.bundle.format(key, params);
    }
}
