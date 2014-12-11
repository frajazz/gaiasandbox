package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public interface IGui {

    public void initialize(AssetManager assetManager);

    public void doneLoading(AssetManager assetManager);

    public void dispose();

    public void update(float dt);

    public void render();

    public void resize(int width, int height);

    public boolean cancelTouchFocus();

    public Stage getGuiStage();

    public void setSceneGraph(ISceneGraph sg);

    public void setVisibilityToggles(ComponentType[] entities, boolean[] visible);

    /** 
     * Returns the first actor found with the specified name. Note this recursively compares the name of every actor in the GUI. 
     * @return The actor if it exists, null otherwise.
     **/
    public Actor findActor(String name);

}