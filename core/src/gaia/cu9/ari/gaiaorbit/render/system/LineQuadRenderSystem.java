package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class LineQuadRenderSystem extends LineRenderSystem {

    int uvOffset;
    int indexIdx;
    int maxIndices;
    short[] indices;

    Vector3 line, camdir, campos, point;
    final static double widthAngle = Math.toRadians(0.2);
    final static double widthAngleTan = Math.tan(widthAngle);

    public LineQuadRenderSystem(RenderGroup rg, int priority, float[] alphas) {
	super(rg, priority, alphas);
	glType = GL20.GL_TRIANGLES;
	line = new Vector3();
	camdir = new Vector3();
	campos = new Vector3();
	point = new Vector3();
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
	maxVertices = 800000;
	maxIndices = maxVertices + maxVertices / 2;

	VertexAttribute[] attribs = buildVertexAttributes();
	mesh = new Mesh(VertexDataType.VertexArray, false, maxVertices, maxIndices, attribs);

	indices = new short[maxIndices];
	vertexSize = mesh.getVertexAttributes().vertexSize / 4;
	vertices = new float[maxVertices * vertexSize];

	colorOffset = mesh.getVertexAttribute(Usage.ColorPacked) != null ? mesh.getVertexAttribute(Usage.ColorPacked).offset / 4
		: 0;
	uvOffset = mesh.getVertexAttribute(Usage.TextureCoordinates) != null ? mesh.getVertexAttribute(Usage.TextureCoordinates).offset / 4
		: 0;

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
	vertices[vertexIdx + uvOffset] = u;
	vertices[vertexIdx + uvOffset + 1] = v;
    }

    public void addLine(float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float b, float a) {
	camera.getDirection().setVector3(camdir);
	line.set(x1 - x0, y1 - y0, z1 - z0);

	// Camdir will contain the perpendicular to camdir and line
	camdir.crs(line).nor();

	double distToLine = MathUtilsd.distancePointLine(x0, y0, z0, x1, y1, z1, 0, 0, 0);

	float dist = (float) Math.min(Math.sqrt(x0 * x0 + y0 * y0 + z0 * z0), distToLine);
	float width = ((float) (widthAngleTan * dist) / 2f) * camera.getFovFactor();
	camdir.scl(width);
	// P1
	point.set(x0, y0, z0).add(camdir);
	color(r, g, b, a);
	vertex(point.x, point.y, point.z);
	uv(0, 0);

	// P2
	point.set(x0, y0, z0).sub(camdir);
	color(r, g, b, a);
	vertex(point.x, point.y, point.z);
	uv(0, 1);

	// P3
	dist = (float) Math.min(Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1), distToLine);
	width = ((float) (widthAngleTan * dist) / 2f) * camera.getFovFactor();
	camdir.nor().scl(width);
	point.set(x1, y1, z1).add(camdir);
	color(r, g, b, a);
	vertex(point.x, point.y, point.z);
	uv(1, 0);

	// P4
	point.set(x1, y1, z1).sub(camdir);
	color(r, g, b, a);
	vertex(point.x, point.y, point.z);
	uv(1, 1);

	// Add indexes
	index((short) (numVertices - 4));
	index((short) (numVertices - 3));
	index((short) (numVertices - 2));
	index((short) (numVertices - 2));
	index((short) (numVertices - 1));
	index((short) (numVertices - 3));

    }

    private void index(short idx) {
	indices[indexIdx] = idx;
	indexIdx++;
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
	mesh.setVertices(vertices, 0, vertexIdx);
	mesh.setIndices(indices, 0, indexIdx);

	mesh.render(shaderProgram, glType);
	shaderProgram.end();

	vertexIdx = 0;
	indexIdx = 0;
	numVertices = 0;
    }

}
