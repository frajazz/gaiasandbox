package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.comp.DistToCameraComparator;
import gaia.cu9.ari.gaiaorbit.util.time.TimeUtils;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

public class ShaderQuadRenderSystem extends AbstractRenderSystem implements IObserver {

    private ShaderProgram shaderProgram;
    private Mesh mesh;
    private boolean useStarColorTransit;
    private boolean starColorTransit = false;
    private Texture noise;
    private Vector3 aux;

    /**
     * Creates a new shader quad render component.
     * @param rg The render group.
     * @param priority The priority of the component.
     * @param alphas The alphas list.
     * @param shaderProgram The shader program to render the quad with.
     * @param mesh The mesh.
     * @param useStarColorTransit Whether to use the star color transit or not.
     */
    public ShaderQuadRenderSystem(RenderGroup rg, int priority, float[] alphas, ShaderProgram shaderProgram, boolean useStarColorTransit) {
	super(rg, priority, alphas);
	this.shaderProgram = shaderProgram;
	this.useStarColorTransit = useStarColorTransit;
	init();
	if (this.useStarColorTransit)
	    EventManager.instance.subscribe(this, Events.TRANSIT_COLOUR_CMD);
    }

    private void init() {
	// Init comparator
	comp = new DistToCameraComparator<IRenderable>();
	// Init vertices
	float[] vertices = new float[20];
	fillVertices(vertices);

	noise = new Texture(Gdx.files.internal(GlobalConf.TEXTURES_FOLDER + "static.jpg"));
	noise.setFilter(TextureFilter.Linear, TextureFilter.Linear);

	// We wont need indices if we use GL_TRIANGLE_FAN to draw our quad
	// TRIANGLE_FAN will draw the verts in this order: 0, 1, 2; 0, 2, 3
	mesh = new Mesh(VertexDataType.VertexArray, false, 4, 6, new VertexAttribute(Usage.Position, 2,
		ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
		new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

	mesh.setVertices(vertices, 0, vertices.length);
	mesh.getIndicesBuffer().position(0);
	mesh.getIndicesBuffer().limit(6);

	short[] indices = new short[6];
	short j = 0;
	for (int i = 0; i < 6; i += 6, j += 4) {
	    indices[i] = j;
	    indices[i + 1] = (short) (j + 1);
	    indices[i + 2] = (short) (j + 2);
	    indices[i + 3] = (short) (j + 2);
	    indices[i + 4] = (short) (j + 3);
	    indices[i + 5] = j;
	}
	mesh.setIndices(indices);

	aux = new Vector3();
    }

    private void fillVertices(float[] vertices) {
	float x = 1;
	float y = 1;
	float width = -2;
	float height = -2;
	final float fx2 = x + width;
	final float fy2 = y + height;
	final float u = 0;
	final float v = 1;
	final float u2 = 1;
	final float v2 = 0;

	float color = Color.WHITE.toFloatBits();
	;
	int idx = 0;
	vertices[idx++] = x;
	vertices[idx++] = y;
	vertices[idx++] = color;
	vertices[idx++] = u;
	vertices[idx++] = v;

	vertices[idx++] = x;
	vertices[idx++] = fy2;
	vertices[idx++] = color;
	vertices[idx++] = u;
	vertices[idx++] = v2;

	vertices[idx++] = fx2;
	vertices[idx++] = fy2;
	vertices[idx++] = color;
	vertices[idx++] = u2;
	vertices[idx++] = v2;

	vertices[idx++] = fx2;
	vertices[idx++] = y;
	vertices[idx++] = color;
	vertices[idx++] = u2;
	vertices[idx++] = v;
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
	Collections.sort(renderables, comp);
	shaderProgram.begin();
	if (!Constants.mobile) {
	    // Global uniforms
	    shaderProgram.setUniformf("u_time", TimeUtils.getRunningTimeSecs());
	    // Bind
	    noise.bind(0);
	    shaderProgram.setUniformi("u_noiseTexture", 0);
	}
	int size = renderables.size();
	for (int i = 0; i < size; i++) {
	    IRenderable s = renderables.get(i);
	    s.render(shaderProgram, alphas[s.getComponentType().ordinal()], starColorTransit, mesh, camera);
	}
	shaderProgram.end();

    }

    @Override
    public void notify(Events event, Object... data) {
	if (event == Events.TRANSIT_COLOUR_CMD) {
	    starColorTransit = (boolean) data[1];
	}

    }

}
