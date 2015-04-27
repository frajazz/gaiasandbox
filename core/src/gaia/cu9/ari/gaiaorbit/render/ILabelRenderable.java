package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Interface to be implemented by all entities that can render a label.
 * @author Toni Sagrista
 *
 */
public interface ILabelRenderable extends IRenderable {

    /**
     * Tells whether the label must be rendered or not for this entity.
     * @return True if label must be rendered.
     */
    public boolean renderLabel();

    /**
     * Renders the label.
     * @param batch
     * @param shader
     * @param font
     * @param camera
     */
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font, ICamera camera);

    /**
     * Returns an array with the label colour in the fashion [r, g, b, a].
     * @return Array with the colour.
     */
    public float[] labelColour();

    /**
     * Returns the label size.
     * @return The label size.
     */
    public float labelSize();

    /**
     * Returns the label scale for the scale varying in the shader.
     * @return The scale.
     */
    public float labelScale();

    /**
     * Sets the position of this label in the out vector.
     * @param pos The out parameter with the result.
     */
    public void labelPosition(Vector3d out);

    /**
     * Returns the label text.
     * @return The label text.
     */
    public String label();

    /**
     * Executes the blending for the label.
     */
    public void labelDepthBuffer();

}
