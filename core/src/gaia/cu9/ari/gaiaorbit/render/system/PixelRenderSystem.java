package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class PixelRenderSystem extends ImmediateRenderSystem implements IObserver {
    private final float BRIGHTNESS_FACTOR;
    private final float POINT_SIZE;

    boolean starColorTransit = false;
    Vector3 aux;
    int additionalOffset;

    public PixelRenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super(rg, priority, alphas);
        EventManager.instance.subscribe(this, Events.TRANSIT_COLOUR_CMD, Events.ONLY_OBSERVED_STARS_CMD);
        BRIGHTNESS_FACTOR = Constants.webgl ? 15f : 2f;
        POINT_SIZE = GlobalConf.runtime.STRIPPED_FOV_MODE ? 2 : 1;
    }

    @Override
    protected void initShaderProgram() {
        // Initialise renderer
        shaderProgram = new ShaderProgram(Gdx.files.internal("shader/point.vertex.glsl"), Gdx.files.internal("shader/point.fragment.glsl"));
        if (!shaderProgram.isCompiled()) {
            Gdx.app.error(this.getClass().getName(), "Point shader compilation failed:\n" + shaderProgram.getLog());
        }
        shaderProgram.begin();
        shaderProgram.setUniformf("u_pointAlphaMin", GlobalConf.scene.POINT_ALPHA_MIN);
        shaderProgram.setUniformf("u_pointAlphaMax", GlobalConf.scene.POINT_ALPHA_MAX);
        shaderProgram.end();

    }

    @Override
    protected void initVertices() {
        meshes = new MeshData[1];
        curr = new MeshData();
        meshes[0] = curr;

        aux = new Vector3();

        /** Init renderer **/
        maxVertices = 3000000;

        VertexAttribute[] attribs = buildVertexAttributes();
        curr.mesh = new Mesh(false, maxVertices, 0, attribs);

        curr.vertices = new float[maxVertices * (curr.mesh.getVertexAttributes().vertexSize / 4)];
        curr.vertexSize = curr.mesh.getVertexAttributes().vertexSize / 4;
        curr.colorOffset = curr.mesh.getVertexAttribute(Usage.ColorPacked) != null ? curr.mesh.getVertexAttribute(Usage.ColorPacked).offset / 4 : 0;
        additionalOffset = curr.mesh.getVertexAttribute(Usage.Generic) != null ? curr.mesh.getVertexAttribute(Usage.Generic).offset / 4 : 0;
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
        if (POINT_UPDATE_FLAG) {
            // Reset variables
            curr.clear();

            int size = renderables.size();
            for (int i = 0; i < size; i++) {
                // 2 FPS gain
                CelestialBody cb = (CelestialBody) renderables.get(i);
                float[] col = starColorTransit ? cb.ccTransit : cb.ccPale;
                // COLOR
                curr.vertices[curr.vertexIdx + curr.colorOffset] = Color.toFloatBits(col[0], col[1], col[2], 1.0f);

                // SIZE
                curr.vertices[curr.vertexIdx + additionalOffset] = cb.getRadius();
                curr.vertices[curr.vertexIdx + additionalOffset + 1] = (float) cb.THRESHOLD_ANGLE_POINT();

                // VERTEX
                aux.set((float) cb.pos.x, (float) cb.pos.y, (float) cb.pos.z);
                //cb.transform.getTranslationf(aux);
                final int idx = curr.vertexIdx;
                curr.vertices[idx] = aux.x;
                curr.vertices[idx + 1] = aux.y;
                curr.vertices[idx + 2] = aux.z;

                curr.vertexIdx += curr.vertexSize;
            }
            // Put flag down
            POINT_UPDATE_FLAG = false;
        }
        shaderProgram.begin();
        shaderProgram.setUniformMatrix("u_projModelView", camera.getCamera().combined);
        shaderProgram.setUniformf("u_camPos", camera.getCurrent().getPos().setVector3(aux));
        shaderProgram.setUniformf("u_fovFactor", camera.getFovFactor());
        shaderProgram.setUniformf("u_alpha", alphas[0]);
        shaderProgram.setUniformf("u_starBrightness", GlobalConf.scene.STAR_BRIGHTNESS * BRIGHTNESS_FACTOR);
        shaderProgram.setUniformf("u_pointSize", POINT_SIZE);
        curr.mesh.setVertices(curr.vertices, 0, curr.vertexIdx);
        curr.mesh.render(shaderProgram, ShapeType.Point.getGlType());
        shaderProgram.end();

    }

    protected VertexAttribute[] buildVertexAttributes() {
        Array<VertexAttribute> attribs = new Array<VertexAttribute>();
        attribs.add(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
        attribs.add(new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE));
        attribs.add(new VertexAttribute(Usage.Generic, 4, "a_additional"));

        VertexAttribute[] array = new VertexAttribute[attribs.size];
        for (int i = 0; i < attribs.size; i++)
            array[i] = attribs.get(i);
        return array;
    }

    @Override
    public void notify(Events event, Object... data) {
        if (event == Events.TRANSIT_COLOUR_CMD) {
            starColorTransit = (boolean) data[1];
            POINT_UPDATE_FLAG = true;
        } else if (event == Events.ONLY_OBSERVED_STARS_CMD) {
            POINT_UPDATE_FLAG = true;
        }
    }
}
