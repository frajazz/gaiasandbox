package gaia.cu9.ari.gaiaorbit.render;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;

public interface IQuadRenderable extends IRenderable {

    /**
     * Renders the renderable as a quad using the star shader.
     * @param shader
     * @param alpha
     * @param camera
     */
    public void render(ShaderProgram shader, float alpha, boolean colorTransit, Mesh mesh, ICamera camera);
}
