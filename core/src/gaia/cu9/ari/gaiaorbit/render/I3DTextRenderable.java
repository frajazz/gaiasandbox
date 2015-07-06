package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Interface to be implemented by all entities that can render a text in 3d space.
 * @author Toni Sagrista
 *
 */
public interface I3DTextRenderable extends IRenderable {

    /**
     * Tells whether the text must be rendered or not for this entity.
     * @return True if text must be rendered.
     */
    public boolean renderText();

    /**
     * Renders the text.
     * @param batch
     * @param shader
     * @param font
     * @param camera
     */
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font, ICamera camera);

    /**
     * Returns an array with the text colour in the fashion [r, g, b, a].
     * @return Array with the colour.
     */
    public float[] textColour();

    /**
     * Returns the text size.
     * @return The text size.
     */
    public float textSize();

    /**
     * Returns the text scale for the scale varying in the shader.
     * @return The scale.
     */
    public float textScale();

    /**
     * Sets the position of this text in the out vector.
     * @param out The out parameter with the result.
     */
    public void textPosition(Vector3d out);

    /**
     * Returns the text.
     * @return The text.
     */
    public String text();

    /**
     * Executes the blending for the text.
     */
    public void textDepthBuffer();

    /**
     * Is it a label or another kind of text?
     * @return
     */
    public boolean isLabel();

}
