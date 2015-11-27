package gaia.cu9.ari.gaiaorbit.render.system;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

public class LineQuadRenderSystem extends LineRenderSystem {
    /** 100 pc of bin **/
    private static double bin = 100 * Constants.PC_TO_U;

    private static final int shortLimit = (int) Math.pow(2, 2 * 8);
    private MeshDataExt currext;

    private class MeshDataExt extends MeshData {
        int uvOffset;
        int indexIdx;
        int maxIndices;
        short[] indices;

        public void clear() {
            super.clear();
            indexIdx = 0;
        }
    }

    Vector3d line, camdir, point, vec, aux, aux2;
    final static double widthAngle = Math.toRadians(0.08);
    final static double widthAngleTan = Math.tan(widthAngle);

    public LineQuadRenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super(rg, priority, alphas);
        glType = GL20.GL_TRIANGLES;
        line = new Vector3d();
        camdir = new Vector3d();
        point = new Vector3d();
        vec = new Vector3d();
        aux = new Vector3d();
        aux2 = new Vector3d();
    }

    @Override
    protected void initShaderProgram() {
        shaderProgram = new ShaderProgram(Gdx.files.internal("shader/line.quad.vertex.glsl"), Gdx.files.internal("shader/line.quad.fragment.glsl"));
        if (!shaderProgram.isCompiled()) {
            Gdx.app.error(this.getClass().getName(), "Line shader compilation failed:\n" + shaderProgram.getLog());
        }
    }

    @Override
    protected void initVertices() {
        meshes = new MeshDataExt[100];
        initVertices(meshIdx++);
    }

    private void initVertices(int index) {
        if (meshes[index] == null) {
            currext = new MeshDataExt();
            meshes[index] = currext;
            curr = currext;

            maxVertices = 800000;
            currext.maxIndices = maxVertices + maxVertices / 2;

            VertexAttribute[] attribs = buildVertexAttributes();
            currext.mesh = new Mesh(Mesh.VertexDataType.VertexArray, false, maxVertices, currext.maxIndices, attribs);

            currext.indices = new short[currext.maxIndices];
            currext.vertexSize = currext.mesh.getVertexAttributes().vertexSize / 4;
            currext.vertices = new float[maxVertices * currext.vertexSize];

            currext.colorOffset = currext.mesh.getVertexAttribute(Usage.ColorPacked) != null ? currext.mesh.getVertexAttribute(Usage.ColorPacked).offset / 4 : 0;
            currext.uvOffset = currext.mesh.getVertexAttribute(Usage.TextureCoordinates) != null ? currext.mesh.getVertexAttribute(Usage.TextureCoordinates).offset / 4 : 0;
        } else {
            currext = (MeshDataExt) meshes[index];
            curr = currext;
        }
    }

    protected VertexAttribute[] buildVertexAttributes() {
        Array<VertexAttribute> attribs = new Array<VertexAttribute>();
        attribs.add(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
        attribs.add(new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE));
        attribs.add(new VertexAttribute(Usage.TextureCoordinates, 2, "a_uv"));

        VertexAttribute[] array = new VertexAttribute[attribs.size];
        for (int i = 0; i < attribs.size; i++)
            array[i] = attribs.get(i);
        return array;
    }

    public void uv(float u, float v) {
        currext.vertices[currext.vertexIdx + currext.uvOffset] = u;
        currext.vertices[currext.vertexIdx + currext.uvOffset + 1] = v;
    }

    public void addLine(double x0, double y0, double z0, double x1, double y1, double z1, float r, float g, float b, float a) {
        // We bin long lines
        vec.set(x1 - x0, y1 - y0, z1 - z0);
        double realLength = vec.len();
        double currLength = 0;
        aux.set(x0, y0, z0);
        if (realLength > bin) {
            while (currLength < realLength) {
                double vecLength = bin;
                if (currLength + vecLength > realLength) {
                    vecLength = realLength - currLength;
                }
                vec.setLength(vecLength);
                aux2.set(aux).add(vec);
                addLineInternal(aux.x, aux.y, aux.z, aux2.x, aux2.y, aux2.z, r, g, b, a);
                aux.set(aux2);
                currLength += bin;
            }
        } else {
            addLineInternal(x0, y0, z0, x1, y1, z1, r, g, b, a);
        }

    }

    public void addLineInternal(double x0, double y0, double z0, double x1, double y1, double z1, float r, float g, float b, float a) {

        // Check if 6 more indices fit
        if (currext.numVertices + 3 >= shortLimit) {
            // We need to open a new MeshDataExt!
            initVertices(meshIdx++);
        }

        camdir.set(camera.getDirection());
        line.set(x1 - x0, y1 - y0, z1 - z0);

        // Camdir will contain the perpendicular to camdir and line
        camdir.crs(line);

        double distToSegment = MathUtilsd.distancePointSegment(x0, y0, z0, x1, y1, z1, 0, 0, 0);

        double width0, width1;
        double dist0 = Math.sqrt(x0 * x0 + y0 * y0 + z0 * z0);
        double dist1 = Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);

        if (distToSegment < dist0 && distToSegment < dist1) {
            // Projection falls in line
            double widthInProj = widthAngleTan * distToSegment * camera.getFovFactor();
            width0 = widthInProj;
            width1 = widthInProj;
        } else {
            // Projection falls outside line
            width0 = widthAngleTan * dist0 * camera.getFovFactor();
            width1 = widthAngleTan * dist1 * camera.getFovFactor();
        }

        camdir.setLength(width0);
        // P1
        point.set(x0, y0, z0).add(camdir);
        color(r, g, b, a);
        uv(0, 0);
        vertex((float) point.x, (float) point.y, (float) point.z);

        // P2
        point.set(x0, y0, z0).sub(camdir);
        color(r, g, b, a);
        uv(0, 1);
        vertex((float) point.x, (float) point.y, (float) point.z);

        camdir.setLength(width1);

        // P3
        point.set(x1, y1, z1).add(camdir);
        color(r, g, b, a);
        uv(1, 0);
        vertex((float) point.x, (float) point.y, (float) point.z);

        // P4
        point.set(x1, y1, z1).sub(camdir);
        color(r, g, b, a);
        uv(1, 1);
        vertex((float) point.x, (float) point.y, (float) point.z);

        // Add indexes
        index((short) (currext.numVertices - 4));
        index((short) (currext.numVertices - 3));
        index((short) (currext.numVertices - 2));

        index((short) (currext.numVertices - 2));
        index((short) (currext.numVertices - 1));
        index((short) (currext.numVertices - 3));
    }

    private void index(short idx) {
        currext.indices[currext.indexIdx] = idx;
        currext.indexIdx++;
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
        this.camera = camera;
        int size = renderables.size();
        for (int i = 0; i < size; i++) {
            IRenderable l = renderables.get(i);
            l.render(this, camera, alphas[l.getComponentType().ordinal()]);
        }

        shaderProgram.begin();
        shaderProgram.setUniformMatrix("u_projModelView", camera.getCamera().combined);

        for (int i = 0; i < meshIdx; i++) {
            MeshDataExt md = (MeshDataExt) meshes[i];
            md.mesh.setVertices(md.vertices, 0, md.vertexIdx);
            md.mesh.setIndices(md.indices, 0, md.indexIdx);
            md.mesh.render(shaderProgram, GL20.GL_TRIANGLES);

            md.clear();
        }

        shaderProgram.end();

        // Reset mesh index and current
        meshIdx = 1;
        currext = (MeshDataExt) meshes[0];
        curr = currext;
    }

}
