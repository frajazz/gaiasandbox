package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.FovCamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.comp.DistToCameraComparator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class FontRenderSystem extends AbstractRenderSystem {
    private SpriteBatch batch;
    private ShaderProgram shaderProgram;
    private BitmapFont distanceFont;
    private Comparator<IRenderable> comp;

    public FontRenderSystem(RenderGroup rg, int priority, float[] alphas, SpriteBatch batch) {
        super(rg, priority, alphas);
        this.batch = batch;

        // Init comparator
        comp = new DistToCameraComparator<IRenderable>();
    }

    public FontRenderSystem(RenderGroup rg, int priority, float[] alphas, SpriteBatch batch, ShaderProgram shaderProgram) {
        this(rg, priority, alphas, batch);
        this.shaderProgram = shaderProgram;
        // Init distance field font
        Texture texture = new Texture(Gdx.files.internal("font/main-font.png"), true);
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        distanceFont = new BitmapFont(Gdx.files.internal("font/main-font.fnt"), new TextureRegion(texture), false);
        distanceFont.getData().setScale(12f / 32f);
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
        Collections.sort(renderables, comp);
        batch.begin();
        int size = renderables.size();
        for (int i = 0; i < size; i++) {
            IRenderable s = renderables.get(i);
            if (shaderProgram == null) {
                // Render sprite
                s.render(batch, camera, alphas[s.getComponentType().ordinal()]);
            } else {

                // Regular mode, we use 3D distance field font
                I3DTextRenderable lr = (I3DTextRenderable) s;
                shaderProgram.setUniformf("a_labelAlpha", (lr.isLabel() || camera.getCurrent() instanceof FovCamera ? alphas[ComponentType.Labels.ordinal()] : 1f));
                shaderProgram.setUniformf("a_componentAlpha", alphas[s.getComponentType().ordinal()]);
                // Font opacity multiplier
                shaderProgram.setUniformf("u_opacity", 0.8f);
                s.render(batch, shaderProgram, distanceFont, camera);
            }
        }
        batch.end();

    }

}
