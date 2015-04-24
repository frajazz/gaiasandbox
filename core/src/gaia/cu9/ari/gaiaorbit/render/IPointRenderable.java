package gaia.cu9.ari.gaiaorbit.render;

import com.badlogic.gdx.graphics.glutils.ImmediateModeRenderer;

/**
 * Interface to be implemented by those entities that can be rendered
 * as a single point, being it a pixel or a fuzzy point.
 * @author Toni Sagrista
 *
 */
public interface IPointRenderable extends IRenderable {

    /**
     * Renders the renderable as a single point or pixel.
     * @param renderer
     * @param alpha
     * @param colorTransit
     */
    public void render(ImmediateModeRenderer renderer, float alpha, boolean colorTransit);

}
