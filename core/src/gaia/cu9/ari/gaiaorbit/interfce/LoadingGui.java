package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnProgressBar;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Displays the loading screen.
 * @author Toni Sagrista
 *
 */
public class LoadingGui implements IGui, IObserver {
    private Skin skin;
    /**
     * The user interface stage	    
     */
    protected Stage ui;
    protected Table center;

    protected boolean progress;
    protected OwnProgressBar progressBar;
    protected NotificationsInterface notificationsInterface;
    /** Lock object for synchronization **/
    private Object lock;

    private int steps;

    public LoadingGui(boolean progress, int steps) {
	this.progress = progress;
	lock = new Object();
	this.steps = steps;
    }

    @Override
    public void initialize(AssetManager assetManager) {
	// User interface
	ui = new Stage(new ScreenViewport(), GlobalResources.spriteBatch);
	skin = GlobalResources.skin;

	// PROGRESS BAR
	center = new Table();
	center.setFillParent(true);
	center.center();

	Image logo = new Image(new Texture(Gdx.files.internal("img/gaiasandboxlogo.png")));

	center.add(logo).center();
	center.row();

	if (progress) {
	    progressBar = new OwnProgressBar(0, steps, 1, false, skin);
	    progressBar.setPrefWidth(Gdx.graphics.getWidth() / 2);
	    progressBar.setValue(0);
	    center.add(progressBar).center();
	    center.row();

	    // MESSAGE INTERFACE - BOTTOM LEFT
	    notificationsInterface = new NotificationsInterface(skin, lock);
	    center.add(notificationsInterface);
	}

	rebuildGui();
	if (progress) {
	    EventManager.getInstance().subscribe(this, Events.POST_NOTIFICATION, Events.TOGGLE_TIME_CMD, Events.TOGGLE_VISIBILITY_CMD, Events.CAMERA_MODE_CMD, Events.PACE_CHANGE_CMD, Events.FOCUS_LOCK_CMD, Events.TOGGLE_AMBIENT_LIGHT, Events.FOV_CHANGE_NOTIFICATION, Events.JAVA_EXCEPTION, Events.ORBIT_DATA_LOADED, Events.SCREENSHOT_INFO);
	}
    }

    @Override
    public void doneLoading(AssetManager assetManager) {
    }

    public void rebuildGui() {
	if (ui != null) {
	    ui.clear();
	    ui.addActor(center);

	}
    }

    @Override
    public void dispose() {
	EventManager.getInstance().removeAllSubscriptions(this, notificationsInterface);
	ui.dispose();
    }

    @Override
    public void update(float dt) {
	ui.act(dt);
    }

    @Override
    public void render() {
	synchronized (lock) {
	    try {
		ui.draw();
	    } catch (Exception e) {
		EventManager.getInstance().post(Events.JAVA_EXCEPTION, e);
	    }
	}
    }

    @Override
    public void resize(int width, int height) {
	ui.getViewport().update(width, height, true);
	rebuildGui();
    }

    @Override
    public boolean cancelTouchFocus() {
	if (ui.getKeyboardFocus() != null || ui.getScrollFocus() != null) {
	    ui.setScrollFocus(null);
	    ui.setKeyboardFocus(null);
	    return true;
	}
	return false;
    }

    @Override
    public Stage getGuiStage() {
	return ui;
    }

    @Override
    public void setSceneGraph(ISceneGraph sg) {
    }

    @Override
    public void setVisibilityToggles(ComponentType[] entities, boolean[] visible) {
    }

    @Override
    public void notify(Events event, Object... data) {
	synchronized (lock) {
	    switch (event) {
	    case POST_NOTIFICATION:
	    case TOGGLE_TIME_CMD:
	    case TOGGLE_VISIBILITY_CMD:
	    case FOCUS_LOCK_CMD:
	    case TOGGLE_AMBIENT_LIGHT:
	    case CAMERA_MODE_CMD:
	    case PACE_CHANGE_CMD:
	    case FOV_CHANGE_NOTIFICATION:
	    case JAVA_EXCEPTION:
	    case ORBIT_DATA_LOADED:
	    case SCREENSHOT_INFO:
		progressBar.setValue(notificationsInterface.getNumberMessages());
		break;
	    }
	}

    }

    @Override
    public Actor findActor(String name) {
	return ui.getRoot().findActor(name);
    }

}
