package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

public class LineRenderSystem extends ImmediateRenderSystem {

    protected ICamera camera;
    protected int glType;

    public LineRenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super(rg, priority, alphas);
        glType = GL20.GL_LINES;
    }

    @Override
    protected void initShaderProgram() {
        shaderProgram = new ShaderProgram(Gdx.files.internal("shader/line.vertex.glsl"), Gdx.files.internal("shader/line.fragment.glsl"));
        if (!shaderProgram.isCompiled()) {
            Gdx.app.error(this.getClass().getName(), "Line shader compilation failed:\n" + shaderProgram.getLog());
        }
    }

    @Override
    protected void initVertices() {
        maxVertices = 400000;

        VertexAttribute[] attribs = buildVertexAttributes();
        mesh = new Mesh(false, maxVertices, 0, attribs);

        vertices = new float[maxVertices * (mesh.getVertexAttributes().vertexSize / 4)];
        vertexSize = mesh.getVertexAttributes().vertexSize / 4;
        colorOffset = mesh.getVertexAttribute(Usage.ColorPacked) != null ? mesh.getVertexAttribute(Usage.ColorPacked).offset / 4
                : 0;

    }

    protected VertexAttribute[] buildVertexAttributes() {
        Array<VertexAttribute> attribs = new Array<VertexAttribute>();
        attribs.add(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
        attribs.add(new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE));

        VertexAttribute[] array = new VertexAttribute[attribs.size];
        for (int i = 0; i < attribs.size; i++)
            array[i] = attribs.get(i);
        return array;
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
        Gdx.gl.glLineWidth(1.5f);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);

        this.camera = camera;
        int size = renderables.size();
        for (int i = 0; i < size; i++) {
            IRenderable l = renderables.get(i);
            l.render(this, camera, alphas[l.getComponentType().ordinal()]);
        }

        shaderProgram.begin();
        shaderProgram.setUniformMatrix("u_projModelView", camera.getCamera().combined);
        mesh.setVertices(vertices, 0, vertexIdx);
        mesh.render(shaderProgram, glType);
        shaderProgram.end();

        vertexIdx = 0;
        numVertices = 0;
    }

    public void addLine(double x0, double y0, double z0, double x1, double y1, double z1, Color col) {
        addLine(x0, y0, z0, x1, y1, z1, col.r, col.g, col.b, col.a);
    }

    public void addLine(double x0, double y0, double z0, double x1, double y1, double z1, float r, float g, float b, float a) {
        color(r, g, b, a);
        vertex((float) x0, (float) y0, (float) z0);
        color(r, g, b, a);
        vertex((float) x1, (float) y1, (float) z1);
    }

}
