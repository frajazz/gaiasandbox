package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

/**
 * Only for frame output mode, it displays the current time.
 * @author Toni Sagrista
 *
 */
public class RenderGui implements IGui, IObserver {
    private Skin skin;
    /**
     * The user interface stage	    
     */
    protected Stage ui;
    protected Label time;
    protected Table mainTable;

    protected MessagesInterface messagesInterface;

    protected DateFormat df;
    /** Lock object for synchronization **/
    private Object lock;

    @Override
    public void initialize(AssetManager assetManager) {
        ui = new Stage(new ScreenViewport(), GlobalResources.spriteBatch);
        df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        lock = new Object();
    }

    @Override
    public void doneLoading(AssetManager assetManager) {
        skin = GlobalResources.skin;

        mainTable = new Table(skin);
        time = new OwnLabel("", skin, "ui-13");
        mainTable.add(time);
        mainTable.setFillParent(true);
        mainTable.right().bottom();
        mainTable.pad(5);

        // MESSAGES INTERFACE - LOW CENTER
        messagesInterface = new MessagesInterface(skin, lock);
        messagesInterface.setFillParent(true);
        messagesInterface.left().bottom();
        messagesInterface.pad(0, 300, 150, 0);

        // Add to GUI
        rebuildGui();

        EventManager.instance.subscribe(this, Events.TIME_CHANGE_INFO);
    }

    private void rebuildGui() {
        if (ui != null) {
            ui.clear();
            ui.addActor(mainTable);
            ui.addActor(messagesInterface);
        }
    }

    @Override
    public void dispose() {
        ui.dispose();
    }

    @Override
    public void update(float dt) {
        ui.act();
    }

    @Override
    public void render() {
        synchronized (lock) {
            try {
                ui.draw();
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }

    @Override
    public void resize(final int width, final int height) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                ui.getViewport().update(width, height, true);
                rebuildGui();
            }
        });
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
            case TIME_CHANGE_INFO:
                time.setText(df.format((Date) data[0]));
                break;
            }
        }
    }

    @Override
    public Actor findActor(String name) {
        return ui.getRoot().findActor(name);
    }
}
