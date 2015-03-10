package gaia.cu9.ari.gaiaorbit.render;

import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer20;

/**
 * Interface to implement by all entities that are to be rendered as lines.
 * @author Toni Sagrista
 *
 */
public interface ILineRenderable extends IRenderable {

    public void render(ImmediateModeRenderer20 renderer, float alpha);

}
