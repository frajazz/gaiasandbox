package gaia.cu9.ari.gaiaorbit.render.system;

import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

public class SpriteBatchRenderSystem extends AbstractRenderSystem {
    private SpriteBatch batch;
    private ShaderProgram shaderProgram;
    private BitmapFont bitmapFont;

    public SpriteBatchRenderSystem(RenderGroup rg, int priority, float[] alphas, SpriteBatch batch) {
	super(rg, priority, alphas);
	this.batch = batch;
    }

    public SpriteBatchRenderSystem(RenderGroup rg, int priority, float[] alphas, SpriteBatch batch, ShaderProgram shaderProgram) {
	super(rg, priority, alphas);
	this.batch = batch;
	this.shaderProgram = shaderProgram;
	// Init font
	Texture texture = new Texture(Gdx.files.internal("font/dffont.png"), true);
	texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
	bitmapFont = new BitmapFont(Gdx.files.internal("font/dffont.fnt"), new TextureRegion(texture), false);
	bitmapFont.setScale(12f / 32f);
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
	batch.begin();
	float labelAlpha = alphas[ComponentType.Labels.ordinal()];
	for (IRenderable s : renderables) {
	    if (shaderProgram == null) {
		// Render sprite
		s.render(batch, camera, alphas[s.getComponentType().ordinal()]);
	    } else {
		// Render font
		s.render(batch, shaderProgram, bitmapFont, camera, labelAlpha);
	    }
	}
	batch.end();

    }

}
