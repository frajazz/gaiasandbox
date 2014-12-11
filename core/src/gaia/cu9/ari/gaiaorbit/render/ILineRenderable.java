package gaia.cu9.ari.gaiaorbit.render;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Interface to implement by all entities that are to be rendered as lines.
 * @author Toni Sagrista
 *
 */
public interface ILineRenderable extends IRenderable {

    public void render(ShapeRenderer shapeRenderer, float alpha);

}
