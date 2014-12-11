package gaia.cu9.ari.gaiaorbit.render;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.system.AbstractRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.IRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.ModelBatchRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.PixelRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.ShaderQuadRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.SpriteBatchRenderSystem;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.override.AtmosphereGroundShaderProvider;
import gaia.cu9.ari.gaiaorbit.util.override.AtmosphereShaderProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;

/**
 * Renders a scenegraph.
 * @author Toni Sagrista
 *
 */
public class SceneGraphRenderer extends AbstractRenderer implements IProcessRenderer, IObserver {

    public enum ComponentType {
	Stars("Stars"),
	Planets("Planets"),
	Moons("Moons"),
	Satellites("Satellites"),
	Asteroids("Asteroids"),
	Labels("Labels"),
	Equatorial("Equatorial grid"),
	Ecliptic("Ecliptic grid"),
	Galactic("Galactic grid"),
	Orbits("Orbits"),
	Atmospheres("Atmospheres"),
	Constellations("Constellations"),
	Boundaries("Boundaries"),
	MilkyWay("Milky way"),
	Others("Others");

	private static Map<String, ComponentType> namesMap = new HashMap<String, ComponentType>();
	static {
	    for (ComponentType ct : ComponentType.values()) {
		namesMap.put(ct.name, ct);
	    }
	}

	public String name;

	private ComponentType(String name) {
	    this.name = name;
	}

	public String getName() {
	    return name;
	}

	@Override
	public String toString() {
	    return name;
	}

	public static ComponentType getFromName(String name) {
	    ComponentType ct = null;
	    try {
		ct = ComponentType.valueOf(name);
	    } catch (Exception e) {
		// Look for name
		ct = namesMap.get(name);
	    }
	    return ct;
	}
    }

    /** Contains the flags representing each type's visibility **/
    public static boolean[] visible;
    /** Contains the last update time of each of the flags **/
    public static long[] times;
    /** Alpha values for each type **/
    public static float[] alphas;

    private ShaderProgram starShader, fontShader;

    /** Render lists for all render groups **/
    public static Map<RenderGroup, List<IRenderable>> render_lists;

    // Line renderer
    private ShapeRenderer shapeRenderer;

    // Two model batches, for front (models), back and atmospheres
    private SpriteBatch spriteBatch, fontBatch;

    private NavigableSet<IRenderSystem> renderProcesses;

    public SceneGraphRenderer() {
	super();
    }

    @Override
    public void initialize(AssetManager manager) {
	ShaderProgram.pedantic = false;
	starShader = new ShaderProgram(Gdx.files.internal("shader/star.vertex.glsl"), Gdx.files.internal("shader/star.rays.fragment.glsl"));
	if (!starShader.isCompiled()) {
	    Gdx.app.error(this.getClass().getName(), "Star shader compilation failed:\n" + starShader.getLog());
	}

	fontShader = new ShaderProgram(Gdx.files.internal("shader/simple.vertex.glsl"), Gdx.files.internal("shader/font.fragment.glsl"));
	if (!fontShader.isCompiled()) {
	    Gdx.app.error(this.getClass().getName(), "Font shader compilation failed:\n" + fontShader.getLog());
	}

	RenderGroup[] renderGroups = RenderGroup.values();
	render_lists = new HashMap<RenderGroup, List<IRenderable>>(renderGroups.length);
	for (RenderGroup rg : renderGroups) {
	    render_lists.put(rg, Collections.synchronizedList(new ArrayList<IRenderable>()));
	}

	// Shape renderers
	shapeRenderer = new ShapeRenderer();

	ShaderProvider spnormal = new AtmosphereGroundShaderProvider(Gdx.files.internal("shader/normal.vertex.glsl"), Gdx.files.internal("shader/normal.fragment.glsl"));
	ShaderProvider sp = new AtmosphereGroundShaderProvider(Gdx.files.internal("shader/default.vertex.glsl"), Gdx.files.internal("shader/default.fragment.glsl"));
	ShaderProvider spatm = new AtmosphereShaderProvider(Gdx.files.internal("shader/atm.vertex.glsl"), Gdx.files.internal("shader/atm.fragment.glsl"));
	ShaderProvider spsurface = new DefaultShaderProvider(Gdx.files.internal("shader/default.vertex.glsl"), Gdx.files.internal("shader/starsurface.fragment.glsl"));

	RenderableSorter noSorter = new RenderableSorter() {
	    @Override
	    public void sort(Camera camera, Array<Renderable> renderables) {
		// Does nothing
	    }
	};

	ModelBatch modelBatchB = new ModelBatch(sp, noSorter);
	ModelBatch modelBatchF = new ModelBatch(spnormal, noSorter);
	ModelBatch modelBatchAtm = new ModelBatch(spatm, noSorter);
	ModelBatch modelBatchS = new ModelBatch(spsurface, noSorter);

	// Sprites
	spriteBatch = GlobalResources.spriteBatch;
	spriteBatch.enableBlending();

	// Font batch
	fontBatch = new SpriteBatch(1000, fontShader);
	fontBatch.enableBlending();

	ComponentType[] comps = ComponentType.values();

	// Set reference
	visible = GlobalConf.instance.VISIBILITY;

	times = new long[comps.length];
	alphas = new float[comps.length];
	for (int i = 0; i < comps.length; i++) {
	    alphas[i] = 1f;
	}

	/** 
	 * 
	 * =======  INITIALIZE RENDER COMPONENTS  =======
	 * 
	 **/

	renderProcesses = new TreeSet<IRenderSystem>();

	Runnable blendNoDepthRunnable = new Runnable() {
	    @Override
	    public void run() {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthMask(false);
	    }
	};
	Runnable blendDepthRunnable = new Runnable() {
	    @Override
	    public void run() {
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthMask(true);
	    }
	};

	int i = 1;

	// POINTS
	AbstractRenderSystem pixelProc = new PixelRenderSystem(RenderGroup.POINT, i++, alphas);
	pixelProc.setPreRunnable(blendNoDepthRunnable);

	// MODEL BACK
	AbstractRenderSystem modelBackProc = new ModelBatchRenderSystem(RenderGroup.MODEL_B, i++, alphas, modelBatchB, false);
	modelBackProc.setPreRunnable(blendNoDepthRunnable);
	modelBackProc.setPostRunnable(new Runnable() {
	    @Override
	    public void run() {
		// This always goes at the back, clear depth buffer
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
	    }
	});

	// ANNOTATIONS
	AbstractRenderSystem annotationsProc = new SpriteBatchRenderSystem(RenderGroup.MODEL_B_ANNOT, i++, alphas, spriteBatch);
	annotationsProc.setPreRunnable(blendNoDepthRunnable);
	annotationsProc.setPostRunnable(new Runnable() {
	    @Override
	    public void run() {
		// This always goes at the back, clear depth buffer
		Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
	    }
	});

	// SHADER BACK
	AbstractRenderSystem shaderBackProc = new ShaderQuadRenderSystem(RenderGroup.SHADER, i++, alphas, starShader, true);
	shaderBackProc.setPreRunnable(blendNoDepthRunnable);

	// LINES
	AbstractRenderSystem lineProc = new LineRenderSystem(RenderGroup.LINE, i++, alphas, shapeRenderer);
	lineProc.setPreRunnable(blendDepthRunnable);

	// MODEL FRONT
	AbstractRenderSystem modelFrontProc = new ModelBatchRenderSystem(RenderGroup.MODEL_F, i++, alphas, modelBatchF, false);
	modelFrontProc.setPreRunnable(blendDepthRunnable);

	// MODEL STARS
	AbstractRenderSystem modelStarsProc = new ModelBatchRenderSystem(RenderGroup.MODEL_S, i++, alphas, modelBatchS, false);
	modelStarsProc.setPreRunnable(blendDepthRunnable);

	// LABELS
	AbstractRenderSystem labelsProc = new SpriteBatchRenderSystem(RenderGroup.LABEL, i++, alphas, fontBatch, fontShader);
	labelsProc.setPreRunnable(blendDepthRunnable);

	// MODEL ATMOSPHERE
	AbstractRenderSystem modelAtmProc = new ModelBatchRenderSystem(RenderGroup.MODEL_F_ATM, i++, alphas, modelBatchAtm, true) {
	    @Override
	    protected float getAlpha(IRenderable s) {
		return alphas[ComponentType.Atmospheres.ordinal()] * (float) Math.pow(alphas[s.getComponentType().ordinal()], 2);
	    }

	    @Override
	    protected boolean mustRender() {
		return alphas[ComponentType.Atmospheres.ordinal()] * alphas[ComponentType.Planets.ordinal()] > 0;
	    }
	};
	modelAtmProc.setPreRunnable(blendDepthRunnable);

	// SHADER FRONT
	AbstractRenderSystem shaderFrontProc = new ShaderQuadRenderSystem(RenderGroup.SHADER_F, i++, alphas, starShader, false);
	shaderFrontProc.setPreRunnable(blendDepthRunnable);

	// Add components to set
	renderProcesses.add(modelBackProc);
	renderProcesses.add(annotationsProc);
	renderProcesses.add(pixelProc);
	renderProcesses.add(shaderBackProc);
	renderProcesses.add(modelAtmProc);
	renderProcesses.add(modelFrontProc);
	renderProcesses.add(modelStarsProc);
	renderProcesses.add(lineProc);
	renderProcesses.add(labelsProc);
	renderProcesses.add(shaderFrontProc);

	EventManager.getInstance().subscribe(this, Events.TOGGLE_VISIBILITY_CMD);

    }

    @Override
    public void render(ICamera camera, FrameBuffer fb, float alpha) {
	render(camera, fb);
    }

    @Override
    public void render(ICamera camera, FrameBuffer fb) {
	render(camera, fb, true);
    }

    public void render(ICamera camera, FrameBuffer fb, boolean clearlists) {
	// Update time difference since last update
	long now = new Date().getTime();
	for (ComponentType ct : ComponentType.values()) {
	    alphas[ct.ordinal()] = calculateAlpha(ct, now);
	}

	EventManager.getInstance().post(Events.DEBUG1, "quad: " + (render_lists.get(RenderGroup.SHADER).size() + render_lists.get(RenderGroup.SHADER_F).size()) + ", point: " + render_lists.get(RenderGroup.POINT).size());

	Iterator<IRenderSystem> it = renderProcesses.iterator();
	while (it.hasNext()) {
	    IRenderSystem process = it.next();

	    process.render(render_lists.get(process.getRenderGroup()), camera, fb);
	}
	if (clearlists)
	    clearLists();
    }

    public void clearLists() {
	for (RenderGroup rg : RenderGroup.values()) {
	    render_lists.get(rg).clear();
	}
    }

    public String[] getRenderComponents() {
	ComponentType[] comps = ComponentType.values();
	String[] res = new String[comps.length];
	int i = 0;
	for (ComponentType comp : comps) {
	    res[i++] = comp.getName();
	}
	return res;
    }

    public boolean isOn(ComponentType comp) {
	return visible[comp.ordinal()];
    }

    @Override
    public void notify(Events event, final Object... data) {
	switch (event) {
	case TOGGLE_VISIBILITY_CMD:
	    int idx = ComponentType.getFromName((String) data[0]).ordinal();
	    if (data.length == 3) {
		// We have the boolean
		visible[idx] = (boolean) data[2];
		times[idx] = new Date().getTime();
	    } else {
		// Only toggle
		visible[idx] = !visible[idx];
		times[idx] = new Date().getTime();
	    }
	    break;
	}
    }

    private float calculateAlpha(ComponentType type, long now) {
	long diff = now - times[type.ordinal()];
	if (diff > GlobalConf.instance.OBJECT_FADE_MS) {
	    if (visible[type.ordinal()]) {
		alphas[type.ordinal()] = 1;
	    } else {
		alphas[type.ordinal()] = 0;
	    }
	    return alphas[type.ordinal()];
	} else {
	    return visible[type.ordinal()] ? MathUtilsd.lint(diff, 0, GlobalConf.instance.OBJECT_FADE_MS, 0, 1) : MathUtilsd.lint(diff, 0, GlobalConf.instance.OBJECT_FADE_MS, 1, 0);
	}
    }

}
