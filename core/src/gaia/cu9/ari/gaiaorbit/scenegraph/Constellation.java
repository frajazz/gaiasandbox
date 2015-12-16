package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.render.system.ImmediateRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * Represents a constellation object.
 * @author Toni Sagrista
 *
 */
public class Constellation extends LineObject implements I3DTextRenderable {
    float alpha = .8f;
    float constalpha;

    /** List of pairs of identifiers **/
    public List<int[]> ids;
    /** List of pairs of stars between which there are lines **/
    public List<AbstractPositionEntity[]> stars;
    /** The positions themselves, in case the stars are not there (i.e. octrees) **/
    public List<Vector3[]> positions;

    public Constellation() {
        super();
        cc = new float[] { .9f, 1f, .9f, alpha };
    }

    public Constellation(String name, String parentName) {
        this();
        this.name = name;
        this.parentName = parentName;
    }

    @Override
    public void initialize() {

    }

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera) {
        update(time, parentTransform, camera, 1f);
    }

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera, float opacity) {
        pos.scl(0);
        for (AbstractPositionEntity[] pair : stars) {
            pos.add(pair[0].transform.getTranslation());
        }
        pos.scl((1d / stars.size()));
        pos.nor().scl(100 * Constants.PC_TO_U);
        addToRenderLists(camera);
    }

    @Override
    public void setUp() {
        stars = new ArrayList<AbstractPositionEntity[]>();
        positions = new ArrayList<Vector3[]>();
        for (int[] pair : ids) {
            AbstractPositionEntity s1, s2;
            s1 = sg.getStarMap().get(pair[0]);
            s2 = sg.getStarMap().get(pair[1]);
            if (s1 != null && s2 != null) {
                stars.add(new AbstractPositionEntity[] { s1, s2 });
                positions.add(new Vector3[] { s1.pos.toVector3(), s2.pos.toVector3() });
            } else {
                String wtf = "";
                if (s1 == null)
                    wtf += pair[0];

                if (s2 == null)
                    wtf += (wtf.length() > 0 ? ", " : "") + pair[1];
                Logger.info(this.getClass().getSimpleName(), "Constellations stars not found (HIP): " + wtf);
            }
        }
    }

    @Override
    public void render(Object... params) {
        if (params[0] instanceof ImmediateRenderSystem) {
            super.render(params);
        } else if (params[0] instanceof SpriteBatch) {
            render((SpriteBatch) params[0], (ShaderProgram) params[1], (BitmapFont) params[2], (ICamera) params[3]);
        }
    }

    /**
     * Line rendering.
     */
    @Override
    public void render(LineRenderSystem renderer, ICamera camera, float alpha) {
        constalpha = alpha;
        alpha *= this.alpha;

        Vector3 campos = v3fpool.obtain();
        Vector3 p1 = v3fpool.obtain();
        Vector3 p2 = v3fpool.obtain();
        camera.getPos().setVector3(campos);
        // Fix, using positions directly
        for (Vector3[] pair : positions) {
            p1.set(pair[0]).sub(campos);
            p2.set(pair[1]).sub(campos);

            renderer.addLine(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, cc[0], cc[1], cc[2], alpha);

        }
        v3fpool.free(campos);
        v3fpool.free(p1);
        v3fpool.free(p2);

    }

    /**
     * Label rendering.
     */
    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font, ICamera camera) {
        Vector3d pos = v3dpool.obtain();
        textPosition(pos);
        shader.setUniformf("a_viewAngle", 90f);
        shader.setUniformf("a_thOverFactor", 1f);
        render3DLabel(batch, shader, font, camera, text(), pos, textScale(), textSize(), textColour());
        v3dpool.free(pos);
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        addToRender(this, RenderGroup.LINE);
        if (renderText()) {
            addToRender(this, RenderGroup.LABEL);

        }
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

    @Override
    public float[] textColour() {
        return cc;
    }

    @Override
    public float textSize() {
        return .6e7f;
    }

    @Override
    public float textScale() {
        return 1f;
    }

    @Override
    public void textPosition(Vector3d out) {
        out.set(pos);
    }

    @Override
    public String text() {
        return name;
    }

    @Override
    public boolean renderText() {
        return true;
    }

    @Override
    public void textDepthBuffer() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);
    }

    @Override
    public boolean isLabel() {
        return true;
    }

}
