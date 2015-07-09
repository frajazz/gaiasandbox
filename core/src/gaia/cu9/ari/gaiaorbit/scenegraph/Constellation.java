package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.render.system.ImmediateRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

/**
 * Has the lines of a constellation
 * @author Toni Sagrista
 *
 */
public class Constellation extends LineObject implements I3DTextRenderable {
    float alpha = .8f;
    float constalpha;

    /** List of pairs of identifiers **/
    public List<long[]> ids;
    /** List of pairs of stars between which there are lines **/
    public List<AbstractPositionEntity[]> stars;

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
        stars = new ArrayList<AbstractPositionEntity[]>(ids.size());
        for (long[] pair : ids) {
            AbstractPositionEntity s1, s2;
            s1 = sg.getStarMap().get(pair[0]);
            s2 = sg.getStarMap().get(pair[1]);
            if (s1 != null && s2 != null)
                stars.add(new AbstractPositionEntity[] { s1, s2 });
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
        // This is so that the shape renderer does not mess up the z-buffer
        for (AbstractPositionEntity[] pair : stars) {
            double[] p1 = pair[0].transform.getTranslation();
            double[] p2 = pair[1].transform.getTranslation();

            renderer.addLine((float) p1[0], (float) p1[1], (float) p1[2], (float) p2[0], (float) p2[1], (float) p2[2], cc[0], cc[1], cc[2], alpha);

        }

    }

    /**
     * Label rendering.
     */
    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font, ICamera camera) {
        Vector3d pos = auxVector3d;
        textPosition(pos);
        shader.setUniformf("a_viewAngle", 90f);
        shader.setUniformf("a_thOverFactor", 1f);
        renderLabel(batch, shader, font, camera, text(), pos, textScale(), textSize(), textColour());
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
