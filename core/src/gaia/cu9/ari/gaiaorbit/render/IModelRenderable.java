package gaia.cu9.ari.gaiaorbit.render;

import com.badlogic.gdx.graphics.g3d.ModelBatch;

/**
 * Interface to implement by all the entities that can be rendered as a model.
 * @author Toni Sagrista
 *
 */
public interface IModelRenderable extends IRenderable {

    public void render(ModelBatch modelBatch, float alpha);

    public boolean hasAtmosphere();

}
