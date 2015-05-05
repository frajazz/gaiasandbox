package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public abstract class ImmediateRenderSystem extends AbstractRenderSystem {

    protected ShaderProgram shaderProgram;

    protected int vertexIdx;
    protected Mesh mesh;
    protected int vertexSize;
    protected int colorOffset;
    protected float[] vertices;

    protected int maxVertices;
    protected int numVertices;

    protected ImmediateRenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super(rg, priority, alphas);
        initShaderProgram();
        initVertices();
    }

    protected abstract void initShaderProgram();

    protected abstract void initVertices();

    public void color(Color color) {
        vertices[vertexIdx + colorOffset] = color.toFloatBits();
    }

    public void color(float r, float g, float b, float a) {
        vertices[vertexIdx + colorOffset] = Color.toFloatBits(r, g, b, a);
    }

    public void color(float colorBits) {
        vertices[vertexIdx + colorOffset] = colorBits;
    }

    public void vertex(float x, float y, float z) {
        vertices[vertexIdx] = x;
        vertices[vertexIdx + 1] = y;
        vertices[vertexIdx + 2] = z;

        vertexIdx += vertexSize;
        numVertices++;
    }

}
