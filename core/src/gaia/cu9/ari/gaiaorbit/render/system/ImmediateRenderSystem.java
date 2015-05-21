package gaia.cu9.ari.gaiaorbit.render.system;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

public abstract class ImmediateRenderSystem extends AbstractRenderSystem {

    protected ShaderProgram shaderProgram;

    protected int meshIdx;
    protected MeshData[] meshes;
    protected MeshData curr;

    protected class MeshData {
        protected int vertexIdx;
        protected Mesh mesh;
        protected int vertexSize;
        protected int colorOffset;
        protected float[] vertices;
        protected int numVertices;

        public void clear(){
            vertexIdx = 0;
            numVertices = 0;
        }
    }
    protected int maxVertices;

    protected ImmediateRenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super(rg, priority, alphas);
        initShaderProgram();
        initVertices();
        meshIdx = 0;
    }

    protected abstract void initShaderProgram();

    protected abstract void initVertices();

    public void color(Color color) {
        curr.vertices[curr.vertexIdx + curr.colorOffset] = color.toFloatBits();
    }

    public void color(float r, float g, float b, float a) {
        curr.vertices[curr.vertexIdx + curr.colorOffset] = Color.toFloatBits(r, g, b, a);
    }

    public void color(float colorBits) {
        curr.vertices[curr.vertexIdx + curr.colorOffset] = colorBits;
    }

    public void vertex(float x, float y, float z) {
        curr.vertices[curr.vertexIdx] = x;
        curr.vertices[curr.vertexIdx + 1] = y;
        curr.vertices[curr.vertexIdx + 2] = z;

        curr.vertexIdx += curr.vertexSize;
        curr.numVertices++;
    }

}
